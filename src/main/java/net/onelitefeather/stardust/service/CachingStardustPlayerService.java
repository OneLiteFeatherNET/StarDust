package net.onelitefeather.stardust.service;

import net.onelitefeather.stardust.api.StardustPlayerService;
import net.onelitefeather.stardust.api.player.StardustPlayer;
import net.onelitefeather.stardust.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Decorator that keeps player data in memory so hot event handlers do not query the database on the main thread.
 * Reads are served from the cache, writes are applied to the cache immediately and persisted asynchronously
 * on the given executor.
 */
@SuppressWarnings("removal")
public class CachingStardustPlayerService implements StardustPlayerService, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CachingStardustPlayerService.class);

    private final StardustPlayerService delegate;
    private final ExecutorService writeExecutor;
    private final Predicate<UUID> isOnline;
    private final Supplier<Collection<UUID>> onlinePlayerIds;
    private final ConcurrentHashMap<UUID, Optional<StardustPlayer>> cache = new ConcurrentHashMap<>();

    public CachingStardustPlayerService(StardustPlayerService delegate,
                                        ExecutorService writeExecutor,
                                        Predicate<UUID> isOnline,
                                        Supplier<Collection<UUID>> onlinePlayerIds) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.writeExecutor = Objects.requireNonNull(writeExecutor, "writeExecutor");
        this.isOnline = Objects.requireNonNull(isOnline, "isOnline");
        this.onlinePlayerIds = Objects.requireNonNull(onlinePlayerIds, "onlinePlayerIds");
    }

    /**
     * Loads the player from the delegate and stores the result, including absence. Intended for async threads.
     * The read runs on the write executor, so it is ordered after writes that are still queued for this player.
     *
     * @param playerUuid the player to load
     * @return the loaded player, or empty if the delegate has none
     */
    public Optional<StardustPlayer> load(UUID playerUuid) {
        Optional<StardustPlayer> value = Optional.ofNullable(readAfterPendingWrites(playerUuid));
        cache.put(playerUuid, value);
        return value;
    }

    private StardustPlayer readAfterPendingWrites(UUID playerUuid) {
        try {
            return CompletableFuture.supplyAsync(() -> delegate.findById(playerUuid), writeExecutor).join();
        } catch (RejectedExecutionException e) {
            return delegate.findById(playerUuid);
        }
    }

    /**
     * Removes the cached entry so the next lookup queries the delegate again.
     *
     * @param playerUuid the player to forget
     */
    public void invalidate(UUID playerUuid) {
        cache.remove(playerUuid);
    }

    @Override
    public List<StardustPlayer> findAll(int limit) {
        return delegate.findAll(limit);
    }

    @Override
    public List<StardustPlayer> findOnlinePlayers() {
        return onlinePlayerIds.get().stream().map(this::findById).filter(Objects::nonNull).toList();
    }

    @Override
    public StardustPlayer findById(UUID playerUuid) {
        Optional<StardustPlayer> cached = cache.get(playerUuid);
        if (cached != null) return cached.orElse(null);

        StardustPlayer found = delegate.findById(playerUuid);
        if (isOnline.test(playerUuid)) {
            cache.put(playerUuid, Optional.ofNullable(found));
        }
        return found;
    }

    @Override
    public void saveOrUpdate(StardustPlayer player) {
        UUID uuid = player.getUniqueId();
        if (isOnline.test(uuid) || cache.containsKey(uuid)) {
            cache.put(uuid, Optional.of(player));
        }
        submit(() -> persist(player));
    }

    @Override
    public void delete(StardustPlayer player) {
        cache.remove(player.getUniqueId());
        submit(() -> {
            try {
                delegate.delete(player);
            } catch (RuntimeException e) {
                LOGGER.error("Failed to delete player {}", player.getUniqueId(), e);
            }
        });
    }

    @Override
    public boolean playerExists(UUID playerUuid) {
        Optional<StardustPlayer> cached = cache.get(playerUuid);
        if (cached != null) return cached.isPresent();
        return delegate.playerExists(playerUuid);
    }

    @Override
    public void migrateUser(User user, UserService userService) {
        delegate.migrateUser(user, userService);
        cache.remove(user.getUniqueId());
    }

    /**
     * Waits for queued writes to finish and clears the cache. Safe to call more than once.
     */
    @Override
    public void close() {
        writeExecutor.close();
        cache.clear();
    }

    private void persist(StardustPlayer player) {
        UUID uuid = player.getUniqueId();
        try {
            delegate.saveOrUpdate(player);
            if (player.getId() == null) {
                StardustPlayer persisted = delegate.findById(uuid);
                if (persisted != null) {
                    cache.computeIfPresent(uuid, (key, value) -> Optional.of(persisted));
                }
            }
        } catch (StalePlayerDataException e) {
            LOGGER.warn("Dropped stale write of player {}: copy version {}, stored version {}",
                    uuid, e.getModelVersion(), e.getStoredVersion());
            StardustPlayer current = delegate.findById(uuid);
            cache.computeIfPresent(uuid, (key, value) -> Optional.ofNullable(current));
        } catch (RuntimeException e) {
            LOGGER.error("Failed to persist player {}", uuid, e);
        }
    }

    private void submit(Runnable task) {
        try {
            writeExecutor.execute(task);
        } catch (RejectedExecutionException e) {
            LOGGER.warn("Write executor is shut down, persisting synchronously");
            task.run();
        }
    }
}
