package net.onelitefeather.stardust.command;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import net.onelitefeather.stardust.FeatherEssentials;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class CommandCooldownManager {

    private final FeatherEssentials featherEssentials;
    private final List<CommandCooldown> commandCooldowns;

    public CommandCooldownManager(FeatherEssentials featherEssentials) {
        this.featherEssentials = featherEssentials;
        this.commandCooldowns = new ArrayList<>();
    }

    public CommandCooldown getCommandCooldown(UUID uuid, String command) {
        return this.commandCooldowns.stream().filter(commandCooldown -> commandCooldown.getUuid().equals(uuid) && commandCooldown.getCommand().equalsIgnoreCase(command)).findFirst().orElse(null);
    }

    public void addCommandCooldown(UUID uuid, String command, TimeUnit timeUnit, long time, boolean bypass) {

        if (!bypass) {
            Transaction transaction = null;

            try (Session session = this.featherEssentials.getSessionFactory().openSession()) {
                transaction = session.beginTransaction();

                CommandCooldown commandCooldown = new CommandCooldown();
                commandCooldown.setUniqueId(uuid);
                commandCooldown.setCommand(command);
                commandCooldown.setExecutedAt(System.currentTimeMillis() + getCooldownTime(timeUnit, time));

                if (!this.commandCooldowns.contains(commandCooldown)) this.commandCooldowns.add(commandCooldown);
                if (!isCommandInDatabase(uuid, command)) {
                    session.persist(commandCooldown);
                } else {
                    session.merge(commandCooldown);
                }

            } catch (HibernateException e) {
                this.featherEssentials.getLogger().log(Level.SEVERE, "Something went wrong!", e);
                if(transaction != null) {
                    transaction.rollback();
                }
            }
        }
    }

    private long getCooldownTime(TimeUnit timeUnit, long time) {
        return switch (timeUnit) {
            case DAYS -> (1000 * 60 * 60 * 24) * time;
            case HOURS -> (1000 * 60 * 60) * time;
            case MINUTES -> (1000 * 60) * time;
            default -> time;
        };
    }

    public void load() {

        List<CommandCooldown> toDelete = new ArrayList<>();
        try(Session session = this.featherEssentials.getSessionFactory().openSession()) {

            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<CommandCooldown> query = criteriaBuilder.createQuery(CommandCooldown.class);
            query.from(CommandCooldown.class);
            var sessionQuery = session.createQuery(query);

            sessionQuery.list().forEach(commandCooldown -> {
                if (!commandCooldown.canExecute()) {
                    this.commandCooldowns.add(commandCooldown);
                } else {
                    toDelete.add(commandCooldown);
                }
            });

        } catch (HibernateException e) {
            this.featherEssentials.getLogger().log(Level.SEVERE, "Something went wrong!", e);
        }

        toDelete.forEach(commandCooldown -> removeCommandCooldown(commandCooldown.getUuid(), commandCooldown.getCommand()));
        toDelete.clear();
    }

    public void removeCommandCooldown(UUID uuid, String command) {

        CommandCooldown commandCooldown = getCommandCooldown(uuid, command);
        if (commandCooldown == null) return;
        this.commandCooldowns.remove(commandCooldown);

        Transaction transaction = null;

        try (Session session = this.featherEssentials.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            if (!this.commandCooldowns.contains(commandCooldown)) this.commandCooldowns.add(commandCooldown);
            if (!isCommandInDatabase(uuid, command)) {
                session.persist(commandCooldown);
            } else {
                session.merge(commandCooldown);
            }

        } catch (HibernateException e) {
            this.featherEssentials.getLogger().log(Level.SEVERE, "Something went wrong!", e);
            if(transaction != null) {
                transaction.rollback();
            }
        }
    }

    @Nullable
    public CommandCooldown getCooldown(UUID uuid, String command) {

        try(Session session = this.featherEssentials.getSessionFactory().openSession()) {

            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<CommandCooldown> query = criteriaBuilder.createQuery(CommandCooldown.class);

            Root<CommandCooldown> root = query.from(CommandCooldown.class);
            var sessionQuery = session.createQuery(query.where(criteriaBuilder.and(criteriaBuilder.equal(root.get("uuid"), uuid.toString()), criteriaBuilder.equal(root.get("command"), command))));

            return sessionQuery.getSingleResult();
        } catch (HibernateException e) {
            this.featherEssentials.getLogger().log(Level.SEVERE, "Something went wrong!", e);
        }

        return null;
    }

    public boolean isCommandInDatabase(UUID uuid, String command) {
        try(Session session = this.featherEssentials.getSessionFactory().openSession()) {

            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<CommandCooldown> query = criteriaBuilder.createQuery(CommandCooldown.class);

            Root<CommandCooldown> root = query.from(CommandCooldown.class);
            var sessionQuery = session.createQuery(query.where(criteriaBuilder.and(criteriaBuilder.equal(root.get("uuid"), uuid.toString()), criteriaBuilder.equal(root.get("command"), command))));

            return sessionQuery.list().size() > 0;
        } catch (HibernateException e) {
            this.featherEssentials.getLogger().log(Level.SEVERE, "Something went wrong!", e);
        }

        return false;
    }

    public boolean isCommandCooldownOver(UUID uuid, String command) {
        CommandCooldown commandCooldown = getCommandCooldown(uuid, command);
        return commandCooldown != null && commandCooldown.canExecute();
    }

    public boolean hasCommandCooldown(String name) {
        return this.featherEssentials.getConfig().getConfigurationSection("command-cooldowns." + name.toLowerCase()) != null;
    }


}
