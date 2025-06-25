package net.onelitefeather.stardust.service;

import net.onelitefeather.stardust.DatabaseConnectionService;
import net.onelitefeather.stardust.StardustPlugin;
import net.onelitefeather.stardust.api.PlayerVanishService;
import net.onelitefeather.stardust.task.UserTask;
import net.onelitefeather.stardust.user.User;
import net.onelitefeather.stardust.user.UserProperty;
import net.onelitefeather.stardust.user.UserPropertyType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.hibernate.SessionFactory;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class UserService {

    private final StardustPlugin plugin;
    private final BukkitTask userTask;
    private final PlayerVanishService<Player> vanishService;
    private final DatabaseConnectionService databaseService;

    public UserService(StardustPlugin plugin) {
        this.plugin = plugin;
        this.userTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new UserTask(plugin), 20L, 20L);
        this.vanishService = new BukkitPlayerVanishService(this, plugin);
        this.databaseService = plugin.getDatabaseService();
    }

    public PlayerVanishService<Player> getVanishService() {
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
        return this.databaseService.getSessionFactory().map(SessionFactory::openSession).map(session -> {
            try (session) {
                var query = session.createQuery("SELECT u FROM User u JOIN FETCH u.properties WHERE u.uuid = :uuid", User.class);
                query.setParameter("uuid", uuid.toString());
                return query.uniqueResult();
            } catch (Exception e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to retrieve user with UUID: %s".formatted(uuid.toString()), e);
                return null;
            }
        }).orElse(null);
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
            } catch (Exception e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to set user property: %s".formatted(propertyType.getName()), e);
                if (transaction != null) transaction.rollback();
            }
        });
    }
}
