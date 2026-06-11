package net.onelitefeather.stardust.service;

import net.onelitefeather.stardust.DatabaseConnectionService;
import net.onelitefeather.stardust.StardustPlugin;
import net.onelitefeather.stardust.api.PlayerVanishService;
import net.onelitefeather.stardust.task.UserTask;
import net.onelitefeather.stardust.user.User;
import net.onelitefeather.stardust.user.UserProperty;
import net.onelitefeather.stardust.user.UserPropertyType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.hibernate.SessionFactory;
import org.jetbrains.annotations.Nullable;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class UserService {

    private static final String PLUGIN_BLUEMAP_ENABLED = "BlueMap";

    private final StardustPlugin plugin;
    private final BukkitTask userTask;
    private final PlayerVanishService vanishService;
    private final DatabaseConnectionService databaseService;

    /**
     * In-memory cache of online players' {@link User}s. Reads from here never touch the database,
     * so event handlers running on the server thread never block on JDBC I/O. Populated
     * asynchronously on login (see {@code AsyncPlayerPreLoginEvent}) and evicted on quit; the
     * access-based expiry is a safety net that drops orphaned entries (e.g. a login that is
     * allowed but never completes the join).
     */
    private final Cache<UUID, User> userCache = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(30))
            .build();

    public UserService(StardustPlugin plugin) {
        this.plugin = plugin;
        this.userTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new UserTask(plugin), 20L, 20L);
        if (Bukkit.getPluginManager().isPluginEnabled(PLUGIN_BLUEMAP_ENABLED)) {
            this.vanishService = new DelegatedBlueMapVanishService(new BukkitPlayerVanishService(this, plugin));
        } else {
            this.vanishService = new BukkitPlayerVanishService(this, plugin);
        }
        this.databaseService = plugin.getDatabaseService();
    }

    public PlayerVanishService getVanishService() {
        return vanishService;
    }

    public void stopUserTask() {
        if (userTask == null) return;
        userTask.cancel();
    }

    public List<User> getUsers() {
        return Collections.emptyList();
    }

    @Nullable
    public User getUser(UUID uuid) {
        var cached = userCache.getIfPresent(uuid);
        if (cached != null) return cached;

        var user = loadUser(uuid);
        // Keep online players warm so subsequent per-event lookups stay off the database thread.
        if (user != null && plugin.getServer().getPlayer(uuid) != null) {
            userCache.put(uuid, user);
        }
        return user;
    }

    /**
     * Loads a user from the database by its natural id. This consults Hibernate's
     * {@code @NaturalIdCache} (unlike an HQL query, which always issues SQL), but still performs
     * blocking JDBC I/O on a cache miss — never call this directly from the server thread.
     */
    @Nullable
    private User loadUser(UUID uuid) {
        return this.databaseService.getSessionFactory().map(SessionFactory::openSession).map(session -> {
            try (session) {
                return session.byNaturalId(User.class).using("uuid", uuid.toString()).load();
            } catch (Exception e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to retrieve user with UUID: %s".formatted(uuid.toString()), e);
                return null;
            }
        }).orElse(null);
    }

    /**
     * Loads a user into the online cache. Performs blocking I/O, so it must be called from an
     * asynchronous context (e.g. {@code AsyncPlayerPreLoginEvent}).
     */
    public void loadIntoCache(UUID uuid) {
        var user = loadUser(uuid);
        if (user != null) userCache.put(uuid, user);
    }

    /**
     * Removes a user from the online cache. Called when a player disconnects.
     */
    public void invalidateUser(UUID uuid) {
        userCache.invalidate(uuid);
    }

    /**
     * Reloads a cached user off the server thread after a write, so cached reads stay coherent
     * with the database. No-op for users that are not currently cached.
     */
    private void refreshCache(UUID uuid) {
        if (userCache.getIfPresent(uuid) == null) return;
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> loadIntoCache(uuid));
    }

    @Nullable
    public User getUser(String name) {
        return this.databaseService.getSessionFactory().map(SessionFactory::openSession).map(session -> {
            try (session) {
                var query = session.createQuery("SELECT u FROM User u WHERE u.name = :name", User.class);
                query.setParameter("name", name);
                return query.uniqueResult();
            } catch (Exception e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to retrieve user with name: %s".formatted(name), e);
                return null;
            }
        }).orElse(null);
    }

    public boolean isUserCreated(UUID uuid) {
        return getUser(uuid) != null;
    }

    public void registerUser(Player player, Consumer<User> consumer) {
        if (isUserCreated(player.getUniqueId())) return;
        this.databaseService.getSessionFactory().map(SessionFactory::openSession).ifPresent(session -> {

            var transaction = session.beginTransaction();
            try (session) {

                var user = new User(null, player.getUniqueId(), player.getName(), UserPropertyType.getDefaultUserProperties());
                user.getProperties().forEach(property -> session.persist(UserProperty.of(property.propertyType()).withUser(user)));

                session.persist(user);
                transaction.commit();
                consumer.accept(user);
                // Pull the canonical, fully-persisted user into the cache off-thread.
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> loadIntoCache(player.getUniqueId()));

            } catch (Exception e) {
                consumer.accept(null);
                this.plugin.getLogger().log(Level.SEVERE, "Failed to check existing user for player: %s".formatted(player.getName()), e);
                if (transaction != null) transaction.rollback();
            }
        });
    }

    public void updateUser(User user) {
        this.databaseService.getSessionFactory().map(SessionFactory::openSession).ifPresent(session -> {

            var transaction = session.beginTransaction();
            try (session) {
                var existUser = isUserCreated(user.getUniqueId());
                if (!existUser) {
                    session.persist(user);
                } else {
                    session.merge(user);
                }

                transaction.commit();
                refreshCache(user.getUniqueId());
            } catch (Exception e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to update user: %s".formatted(user.getName()), e);
                if (transaction != null) transaction.rollback();
            }
        });
    }

    public void deleteUser(UUID uuid, Consumer<Boolean> consumer) {

        var user = getUser(uuid);
        if (user == null) {
            consumer.accept(false);
            return;
        }

        this.databaseService.getSessionFactory().map(SessionFactory::openSession).ifPresent(session -> {

            var transaction = session.beginTransaction();
            try (session) {

                user.getProperties().forEach(session::remove);
                session.remove(user);

                consumer.accept(true);
                transaction.commit();
                invalidateUser(uuid);

            } catch (Exception e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to delete user with UUID: %s".formatted(uuid.toString()), e);
                if (transaction != null) transaction.rollback();
                consumer.accept(false);
            }
        });
    }

    public void setUserProperty(User user, UserPropertyType propertyType, Object value) {

        this.databaseService.getSessionFactory().map(SessionFactory::openSession).ifPresent(session -> {

            var transaction = session.beginTransaction();
            try (session) {

                var property = user.getProperty(propertyType);
                if (property != null) {
                    session.merge(property.withValue(value.toString()));
                } else {
                    session.persist(UserProperty.of(propertyType).withUser(user));
                }

                transaction.commit();
                refreshCache(user.getUniqueId());
            } catch (Exception e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to set user property: %s".formatted(propertyType.getName()), e);
                if (transaction != null) transaction.rollback();
            }
        });
    }
}
