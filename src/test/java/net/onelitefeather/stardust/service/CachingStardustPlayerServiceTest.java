package net.onelitefeather.stardust.service;

import net.onelitefeather.stardust.api.StardustPlayerService;
import net.onelitefeather.stardust.api.player.StardustPlayer;
import net.onelitefeather.stardust.user.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("removal")
class CachingStardustPlayerServiceTest {

    private static final UUID PLAYER_ID = UUID.fromString("df7c66a6-7876-44c2-9abd-9bd33f7d3d9e");
    private static final UUID OTHER_ID = UUID.fromString("1b6d3c0e-2f3a-4c5d-8e9f-0a1b2c3d4e5f");

    private static final Instant FIXED_TIME = Instant.EPOCH;

    private FakeDelegate delegate;
    private ManualExecutor writer;
    private Set<UUID> online;
    private CachingStardustPlayerService service;

    private void givenService() {
        delegate = new FakeDelegate();
        writer = new ManualExecutor();
        online = new HashSet<>();
        service = new CachingStardustPlayerService(delegate, writer, online::contains, () -> List.copyOf(online));
    }

    /** Like givenService, but the write executor runs each task as soon as it is submitted. */
    private void givenLoadingService() {
        givenService();
        writer.runOnExecute = true;
    }

    private static StardustPlayer player(UUID uuid, String name, UUID id) {
        return new StardustPlayer(id, uuid, name, new HashMap<>(), FIXED_TIME, FIXED_TIME);
    }

    @Test
    void cacheHitAfterLoadDoesNotQueryDelegateAgain() {
        givenLoadingService();
        delegate.store(player(PLAYER_ID, "Steve", UUID.randomUUID()));

        service.load(PLAYER_ID);
        assertNotNull(service.findById(PLAYER_ID), "loaded player must be returned");

        assertEquals(1, delegate.findByIdCalls, "a cache hit must not query the delegate again");
    }

    @Test
    void knownAbsentPlayerReturnsNullWithoutQueryingDelegate() {
        givenService();
        online.add(PLAYER_ID);

        assertNull(service.findById(PLAYER_ID), "unknown player must resolve to null");
        assertNull(service.findById(PLAYER_ID), "known-absent player must still resolve to null");

        assertEquals(1, delegate.findByIdCalls, "a cached absence must not query the delegate again");
    }

    @Test
    void offlineMissIsNotCached() {
        givenService();
        delegate.store(player(PLAYER_ID, "Steve", UUID.randomUUID()));

        service.findById(PLAYER_ID);
        service.findById(PLAYER_ID);

        assertEquals(2, delegate.findByIdCalls, "a lookup for an offline player must not be cached");
    }

    @Test
    void onlineMissIsCached() {
        givenService();
        online.add(PLAYER_ID);

        service.findById(PLAYER_ID);
        service.findById(PLAYER_ID);

        assertEquals(1, delegate.findByIdCalls, "a miss for an online player must be cached");
    }

    @Test
    void loadedPlayerIsServedFromCacheWhileOffline() {
        givenLoadingService();
        delegate.store(player(PLAYER_ID, "Steve", UUID.randomUUID()));

        service.load(PLAYER_ID);
        service.findById(PLAYER_ID);

        assertEquals(1, delegate.findByIdCalls, "an explicitly loaded entry must serve offline lookups from cache");
    }

    @Test
    void saveOrUpdateUpdatesCacheImmediately() {
        givenService();
        online.add(PLAYER_ID);
        var player = player(PLAYER_ID, "Steve", UUID.randomUUID());

        service.saveOrUpdate(player);

        assertSame(player, service.findById(PLAYER_ID), "saved player must be visible in the cache before the write runs");
        assertEquals(0, delegate.findByIdCalls, "the cache must answer without querying the delegate");
    }

    @Test
    void saveOrUpdatePersistsOnlyWhenExecutorRuns() {
        givenService();
        online.add(PLAYER_ID);

        service.saveOrUpdate(player(PLAYER_ID, "Steve", UUID.randomUUID()));
        assertEquals(0, delegate.saveCalls, "the write must not happen on the calling thread");

        writer.runAll();
        assertEquals(1, delegate.saveCalls, "the write must happen once the executor runs");
    }

    @Test
    void newPlayerIsReloadedWithIdAfterWrite() {
        givenService();
        online.add(PLAYER_ID);

        service.saveOrUpdate(player(PLAYER_ID, "Steve", null));
        writer.runAll();

        var cached = service.findById(PLAYER_ID);
        assertNotNull(cached, "new player must remain in the cache after the write");
        assertNotNull(cached.getId(), "cached new player must carry the database id after the write");
    }

    @Test
    void newPlayerReloadIsSkippedWhenNotCachedAnymore() {
        givenService();
        online.add(PLAYER_ID);

        service.saveOrUpdate(player(PLAYER_ID, "Steve", null));
        service.invalidate(PLAYER_ID);
        writer.runAll();
        int callsBeforeLookup = delegate.findByIdCalls;
        service.findById(PLAYER_ID);

        assertEquals(callsBeforeLookup + 1, delegate.findByIdCalls,
                "the reload must not re-populate an entry that was invalidated meanwhile");
    }

    @Test
    void invalidateForcesNextLookupToQueryDelegate() {
        givenLoadingService();
        delegate.store(player(PLAYER_ID, "Steve", UUID.randomUUID()));
        service.load(PLAYER_ID);

        service.invalidate(PLAYER_ID);
        service.findById(PLAYER_ID);

        assertEquals(2, delegate.findByIdCalls, "after invalidation the next lookup must hit the delegate");
    }

    @Test
    void deleteRemovesPlayerFromCache() {
        givenService();
        online.add(PLAYER_ID);
        service.saveOrUpdate(player(PLAYER_ID, "Steve", UUID.randomUUID()));
        writer.runAll();
        int callsBeforeDelete = delegate.findByIdCalls;

        service.delete(player(PLAYER_ID, "Steve", UUID.randomUUID()));

        service.findById(PLAYER_ID);
        assertEquals(callsBeforeDelete + 1, delegate.findByIdCalls, "a deleted player must no longer be served from cache");
    }

    @Test
    void deleteRunsDelegateDeleteAsynchronously() {
        givenService();
        var player = player(PLAYER_ID, "Steve", UUID.randomUUID());
        delegate.store(player);

        service.delete(player);
        assertEquals(0, delegate.deleteCalls, "the delete must not happen on the calling thread");

        writer.runAll();
        assertEquals(1, delegate.deleteCalls, "the delete must happen once the executor runs");
    }

    @Test
    void closeFlushesPendingWritesAndClearsCache() {
        givenService();
        online.add(PLAYER_ID);
        service.saveOrUpdate(player(PLAYER_ID, "Steve", UUID.randomUUID()));

        service.close();

        assertEquals(1, delegate.saveCalls, "close must flush pending writes before returning");
        int callsAfterClose = delegate.findByIdCalls;
        service.findById(PLAYER_ID);
        assertEquals(callsAfterClose + 1, delegate.findByIdCalls, "close must clear the cache");
    }

    @Test
    void closeIsIdempotent() {
        givenService();

        service.close();

        assertDoesNotThrow(service::close, "a second close must not fail");
    }

    @Test
    void throwingWriteDoesNotBreakLaterWrites() {
        givenService();
        online.add(PLAYER_ID);
        online.add(OTHER_ID);
        delegate.failNextSave = true;

        service.saveOrUpdate(player(PLAYER_ID, "Steve", UUID.randomUUID()));
        service.saveOrUpdate(player(OTHER_ID, "Alex", UUID.randomUUID()));
        writer.runAll();

        assertEquals(2, delegate.saveCalls, "a later write must still run after an earlier write failed");
        assertNotNull(delegate.stored.get(OTHER_ID), "the later write must be persisted");
    }

    @Test
    void writeAfterCloseIsPersistedSynchronously() {
        givenService();
        online.add(PLAYER_ID);
        service.close();

        service.saveOrUpdate(player(PLAYER_ID, "Steve", UUID.randomUUID()));

        assertEquals(1, delegate.saveCalls, "a write after close must not be dropped");
    }

    @Test
    void migrateUserInvalidatesCachedEntry() {
        givenLoadingService();
        delegate.store(player(PLAYER_ID, "Steve", UUID.randomUUID()));
        service.load(PLAYER_ID);
        int callsBeforeMigration = delegate.findByIdCalls;

        service.migrateUser(new User(1L, PLAYER_ID, "Steve", List.of()), null);
        service.findById(PLAYER_ID);

        assertEquals(1, delegate.migrateCalls, "migration must be passed through to the delegate");
        assertEquals(callsBeforeMigration + 1, delegate.findByIdCalls, "migration must invalidate the cached entry");
    }

    @Test
    void findOnlinePlayersResolvesPlayersThroughCache() {
        givenService();
        online.add(PLAYER_ID);
        delegate.store(player(PLAYER_ID, "Steve", UUID.randomUUID()));

        var players = service.findOnlinePlayers();
        service.findOnlinePlayers();

        assertEquals(1, players.size(), "online players must be resolved");
        assertEquals(1, delegate.findByIdCalls, "online players must be served from the cache on repeated calls");
    }

    @Test
    void loadObservesWriteQueuedBeforeIt() {
        givenService();
        online.add(PLAYER_ID);
        delegate.store(player(PLAYER_ID, "Steve", UUID.randomUUID()));
        service.saveOrUpdate(player(PLAYER_ID, "Alex", UUID.randomUUID()));

        writer.runOnExecute = true;
        var loaded = service.load(PLAYER_ID);

        assertTrue(loaded.isPresent(), "loaded player must be present");
        assertEquals("Alex", loaded.get().getName(), "load must read after the queued write so a fast rejoin sees the new name");
        assertEquals(1, delegate.saveCalls, "the queued write must have run before the read");
    }

    @Test
    void loadReadsDirectlyWhenExecutorIsClosed() {
        givenService();
        delegate.store(player(PLAYER_ID, "Steve", UUID.randomUUID()));
        service.close();

        var loaded = service.load(PLAYER_ID);

        assertTrue(loaded.isPresent(), "load must still read from the delegate when the executor is closed");
    }

    /** Hand-written delegate backed by a map; counts calls. */
    static final class FakeDelegate implements StardustPlayerService {
        final Map<UUID, StardustPlayer> stored = new HashMap<>();
        int findByIdCalls;
        int saveCalls;
        int deleteCalls;
        int migrateCalls;
        boolean failNextSave;

        void store(StardustPlayer player) {
            stored.put(player.getUniqueId(), player);
        }

        @Override
        public List<StardustPlayer> findAll(int limit) {
            return List.copyOf(stored.values());
        }

        @Override
        public List<StardustPlayer> findOnlinePlayers() {
            return List.copyOf(stored.values());
        }

        @Override
        public StardustPlayer findById(UUID playerUuid) {
            findByIdCalls++;
            return stored.get(playerUuid);
        }

        @Override
        public void saveOrUpdate(StardustPlayer player) {
            saveCalls++;
            if (failNextSave) {
                failNextSave = false;
                throw new IllegalStateException("simulated database failure");
            }
            var id = player.getId() == null ? UUID.randomUUID() : player.getId();
            stored.put(player.getUniqueId(), new StardustPlayer(id, player.getUniqueId(), player.getName(),
                    new HashMap<>(player.getSettings()), player.getCreatedAt(), player.getUpdatedAt()));
        }

        @Override
        public void delete(StardustPlayer player) {
            deleteCalls++;
            stored.remove(player.getUniqueId());
        }

        @Override
        public boolean playerExists(UUID playerUuid) {
            return stored.containsKey(playerUuid);
        }

        @Override
        public void migrateUser(User user, net.onelitefeather.stardust.service.UserService userService) {
            migrateCalls++;
        }
    }

    /** Queues tasks and runs them only when the test asks; shutdown runs what is pending. */
    static final class ManualExecutor extends AbstractExecutorService {
        private final Deque<Runnable> queue = new ArrayDeque<>();
        private boolean shutdown;
        boolean runOnExecute;

        @Override
        public void execute(Runnable command) {
            if (shutdown) throw new RejectedExecutionException("executor is shut down");
            queue.add(command);
            if (runOnExecute) runAll();
        }

        void runAll() {
            Runnable task;
            while ((task = queue.poll()) != null) {
                task.run();
            }
        }

        @Override
        public void shutdown() {
            runAll();
            shutdown = true;
        }

        @Override
        public List<Runnable> shutdownNow() {
            shutdown();
            return Collections.emptyList();
        }

        @Override
        public boolean isShutdown() {
            return shutdown;
        }

        @Override
        public boolean isTerminated() {
            return shutdown && queue.isEmpty();
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return isTerminated();
        }
    }
}
