package net.onelitefeather.stardust.service;

import net.onelitefeather.stardust.api.StardustPlayerService;
import net.onelitefeather.stardust.api.database.StardustPlayerEntity;
import net.onelitefeather.stardust.api.player.StardustPlayer;
import net.onelitefeather.stardust.user.User;
import net.onelitefeather.stardust.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DefaultStardustPlayerService implements StardustPlayerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultStardustPlayerService.class);
    private final SessionFactory sessionFactory;

    public DefaultStardustPlayerService(SessionFactory sessionFactory) {
        if (sessionFactory == null) throw new IllegalStateException("The session factory cannot be null!");
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<StardustPlayer> findAll(int limit) {
        try (Session session = sessionFactory.openSession()) {
            var query = session.createQuery("SELECT stp FROM StardustPlayerEntity stp", StardustPlayerEntity.class);
            query.setMaxResults(limit);
            return query.list().stream().map(this::mapToModel).toList();
        } catch (HibernateException e) {
            LOGGER.warn("Failed to receive stardust players with an limit of {}", limit);
            LOGGER.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<StardustPlayer> findOnlinePlayers() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).map(this::findById).filter(Objects::nonNull).toList();
    }

    @Override
    public StardustPlayer findById(UUID playerUuid) {
        try (Session session = this.sessionFactory.openSession()) {
            var query = session.createQuery("SELECT stp FROM StardustPlayerEntity stp WHERE stp.uuid = :uuid", StardustPlayerEntity.class);
            query.setParameter("uuid", playerUuid);
            var result = query.uniqueResult();
            return result == null ? null : mapToModel(result);
        } catch (Exception e) {
            LOGGER.atError().setMessage("Failed to retrieve user with UUID: {}").addArgument(playerUuid).setCause(e).log();
            return null;
        }
    }

    @Override
    public void saveOrUpdate(StardustPlayer player) {
        Transaction transaction;
        try (Session session = this.sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            var query = session.createQuery("SELECT stp FROM StardustPlayerEntity stp WHERE stp.uuid = :uuid", StardustPlayerEntity.class);
            query.setParameter("uuid", player.getUniqueId());
            var entity = query.uniqueResult();

            if (player.getId() == null) {
                session.persist(mapToEntity(player));
            } else {
                keepPlayerNameUpdated(player, entity);
                entity.setSettings(player.getSettings());
                session.merge(entity);
            }

            transaction.commit();
        } catch (Exception e) {
            LOGGER.atError().setMessage("Failed to open a session to save or update player: {}").addArgument(player.getName()).setCause(e).log();
        }
    }

    @Override
    public void delete(StardustPlayer player) {

        Transaction transaction;
        try (Session session = this.sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            var query = session.createQuery("SELECT stp FROM StardustPlayerEntity stp WHERE stp.uuid = :uuid", StardustPlayerEntity.class);
            query.setParameter("uuid", player.getUniqueId());
            session.remove(query.uniqueResult());
            transaction.commit();
        } catch (Exception e) {
            LOGGER.atError().setMessage("Failed to open a session to delete player: {}").addArgument(player.getName()).setCause(e).log();
        }
    }

    @Override
    public boolean playerExists(UUID playerUuid) {
        try (Session session = this.sessionFactory.openSession()) {
            var query = session.createQuery("SELECT stp.uuid FROM StardustPlayerEntity stp WHERE stp.uuid = :uuid", UUID.class);
            query.setParameter("uuid", playerUuid);
            return query.uniqueResult() != null;
        } catch (Exception e) {
            LOGGER.atError().setMessage("Failed to open a session to check if player exists: {}").addArgument(playerUuid).setCause(e).log();
            return false;
        }
    }

    @Override
    public void migrateUser(User user, UserService userService) {

        var stardustPlayer = StardustPlayer.create(user.getUniqueId(), user.getName(), PlayerUtil.findSettingsForPlayer(user.getBase()))
                .withAllowFlight(user.isFlying());

        if (userService.getPlugin().getVanishService().isVanishPermitted(user.getUniqueId())) {
            stardustPlayer = stardustPlayer.withVanished(user.isVanished())
                    .withKeepInventory(user.isItemCollectDisabled() && user.isItemDropDisabled())
                    .withCanBuild(user.isInBuildMode())
                    .withCanAttack(user.isPvPAllowed());
        }

        saveOrUpdate(stardustPlayer);
        userService.deleteUser(user.getUniqueId(), _ -> LOGGER.info("User {} was successfully migrated!", user.getUniqueId()));
    }

    private void keepPlayerNameUpdated(StardustPlayer player, StardustPlayerEntity entity) {
        if (!player.getName().equalsIgnoreCase(entity.getName())) {
            entity.setName(player.getName());
        }
    }

    private StardustPlayer mapToModel(StardustPlayerEntity entity) {
        return new StardustPlayer(entity.getId(),
                entity.getUniqueId(),
                entity.getName(),
                entity.getSettings(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private StardustPlayerEntity mapToEntity(StardustPlayer model) {
        return new StardustPlayerEntity(model.getId(),
                model.getUniqueId(),
                model.getName(),
                model.getSettings(),
                model.getCreatedAt(),
                model.getUpdatedAt());
    }
}
