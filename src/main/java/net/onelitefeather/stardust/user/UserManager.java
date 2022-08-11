package net.onelitefeather.stardust.user;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.onelitefeather.stardust.api.user.IUser;
import net.onelitefeather.stardust.FeatherEssentials;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.logging.Level;

public class UserManager {

    private final FeatherEssentials featherEssentials;
    private final List<IUser> users;

    public UserManager(FeatherEssentials featherEssentials) {
        this.featherEssentials = featherEssentials;
        this.users = getUsersFromDatabase();
    }

    public List<IUser> getUsers() {
        return users;
    }

    public IUser getUser(UUID uuid) {
        return this.users.stream().filter(user -> user.getUniqueId().equals(uuid)).findFirst().orElse(null);
    }

    public IUser getUser(String name) {
        return this.users.stream().filter(user -> user.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public int getLastUserId() {
        List<IUser> userList = getUsersFromDatabase();
        if(userList.isEmpty())
            return 0;
        return userList.get(userList.size() - 1).getId();
    }

    public void load() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            loadUser(player, user -> {
                if(user == null)
                    registerUser(player, null);
            });
        });
    }

    public void loadUser(Player player, Consumer<IUser> consumer) {
        IUser user = getFromDatabase(player.getUniqueId());
        if(!this.users.contains(user)) {
            if(user != null) {
                this.users.add(user);
                this.featherEssentials.getLogger().log(Level.INFO, "Successfully loaded User " + user.getUniqueId() + "(" + user.getName() + ")");
            }
        }
        if(consumer != null)
            consumer.accept(user);
    }

    public void registerUser(Player player, Consumer<IUser> consumer) {
        UUID uuid = player.getUniqueId();
        String name = player.getName();
        String displayName = LegacyComponentSerializer.legacyAmpersand().serialize(player.displayName());

        if(!isUserCreated(uuid)) {
            long currentTimeMillis = System.currentTimeMillis();
            User user = new User();
            user.setUniqueId(uuid);
            user.setName(name);
            user.setVanished(false);
            user.setFlying(false);
            user.setTeleportIgnoring(false);
            user.setLastSeen(currentTimeMillis);
            user.setFirstJoin(currentTimeMillis);
            user.setDisplayName(displayName);
            user.setIgnoredPlayers(List.of());
            if(!this.users.contains(user))
                this.users.add(user);

            updateUser(user, false);

            if(consumer != null)
                consumer.accept(user);
        }
    }

    public boolean isUserCreated(UUID uuid) {
        try (Session session = this.featherEssentials.getSessionFactory().openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);

            Root<User> root = query.from(User.class);
            var sessionOneQuery = session.createQuery(query.where(criteriaBuilder.equal(root.get("uuid"), uuid.toString())));

            return sessionOneQuery.list().size() > 0;
        } catch (HibernateException e) {
            this.featherEssentials.getLogger().log(Level.SEVERE, "Something went wrong!", e);
        }
        return false;
    }

    public void updateUser(IUser user, boolean cleanup) {
        this.featherEssentials.getServer().getScheduler().runTaskLaterAsynchronously(this.featherEssentials, () -> {
            Transaction transaction = null;
            try(Session session = this.featherEssentials.getSessionFactory().openSession()) {
                transaction = session.beginTransaction();
                User storedUser = (User) user;
                boolean existUser = isUserCreated(user.getUniqueId());
                if(!existUser)
                    session.persist(storedUser);
                else
                    session.merge(storedUser);

                transaction.commit();
                if(cleanup)
                    this.users.remove(user);
                this.featherEssentials.getLogger().info(String.format("Successfully %s User %s (%s)", (!existUser ? "created" : "updated"), user.getUniqueId().toString(), user.getName()));
            } catch (HibernateException e) {
                this.featherEssentials.getLogger().log(Level.SEVERE, "Something went wrong!", e);
                if(transaction != null)
                    transaction.rollback();
            }
        }, 20L);
    }

    @Nullable
    public IUser getFromDatabase(UUID uuid) {
        IUser user = null;
        if(!isUserCreated(uuid))
            return null;

        Future<IUser> future = CompletableFuture.supplyAsync(() -> {
            try(Session session = this.featherEssentials.getSessionFactory().openSession()) {
                CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
                CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);

                Root<User> root = query.from(User.class);
                var sessionOneQuery = session.createQuery(query.where(criteriaBuilder.equal(root.get("uuid"), uuid.toString())));
                return sessionOneQuery.getSingleResult();
            } catch (HibernateException e) {
                this.featherEssentials.getLogger().log(Level.SEVERE, "Something went wrong!", e);
            }
            return null;
        });

        try {
            user = future.get();
        } catch (InterruptedException | ExecutionException e) {
            this.featherEssentials.getLogger().log(Level.SEVERE, "Something went wrong!", e);
        }
        return user;
    }

    public List<IUser> getUsersFromDatabase() {
        try (Session sessionOne = this.featherEssentials.getSessionFactory().openSession()) {

            CriteriaBuilder criteriaBuilder = sessionOne.getCriteriaBuilder();
            CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
            query.from(User.class);

            var sessionOneQuery = sessionOne.createQuery(query);

            return new ArrayList<>(sessionOneQuery.list());
        } catch (Exception e) {
            this.featherEssentials.getLogger().log(Level.SEVERE, "Something went wrong!", e);
        }

        return List.of();
    }

    public void deleteUser(UUID uuid, Consumer<Boolean> consumer) {

        IUser cachedUser = getUser(uuid);

        boolean success;

        if (cachedUser != null) {
            this.users.remove(cachedUser);
        }

        Transaction transaction = null;
        try (Session session = this.featherEssentials.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(cachedUser);
            transaction.commit();
            success = true;
        } catch (HibernateException e) {
            success = false;
            this.featherEssentials.getLogger().log(Level.SEVERE, "Something went wrong!", e);
            if (transaction != null) {
                transaction.rollback();
            }
        }

        if (null != consumer) {
            consumer.accept(success);
        }
    }


}
