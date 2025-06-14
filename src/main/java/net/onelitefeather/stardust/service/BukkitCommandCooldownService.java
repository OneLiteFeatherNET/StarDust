package net.onelitefeather.stardust.service;

import net.onelitefeather.stardust.DatabaseConnectionService;
import net.onelitefeather.stardust.StardustPlugin;
import net.onelitefeather.stardust.api.CommandCooldownService;
import net.onelitefeather.stardust.command.CommandCooldown;
import net.onelitefeather.stardust.command.CooldownData;
import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BukkitCommandCooldownService implements CommandCooldownService {

    private final StardustPlugin plugin;
    private final DatabaseConnectionService databaseService;

    public BukkitCommandCooldownService(StardustPlugin plugin, DatabaseConnectionService databaseService) {
        this.plugin = plugin;
        this.databaseService = databaseService;
    }

    @Override
    public CommandCooldown getCommandCooldown(UUID commandSender, String command) {
        return this.databaseService.getSessionFactory().map(SessionFactory::openSession).map(session -> {
            try (session) {
                var query = session.createQuery("SELECT cc FROM CommandCooldown cc WHERE cc.commandSender = :commandSender AND cc.command = :command", CommandCooldown.class);
                query.setParameter("commandSender", commandSender.toString());
                query.setParameter("command", command);
                return query.uniqueResult();
            }
        }).orElse(null);
    }

    @Override
    public void addCommandCooldown(UUID commandSender, String command, long time, TimeUnit timeUnit) {
        this.databaseService.getSessionFactory().map(SessionFactory::openSession).ifPresent(session -> {
            var transaction = session.beginTransaction();

            var cooldown = System.currentTimeMillis() + timeUnit.toMillis(time);

            try (session) {
                session.persist(new CommandCooldown(null, commandSender, command, cooldown));
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                this.plugin.getLogger().log(Level.SEVERE, "Failed to add command cooldown for " + commandSender + " on command " + command, e);
            }
        });

    }

    @Override
    public void removeCommandCooldown(UUID commandSender, String command) {

        var cooldown = this.getCommandCooldown(commandSender, command);
        if (cooldown == null) return;

        this.databaseService.getSessionFactory().map(SessionFactory::openSession).ifPresent(session -> {
            var transaction = session.beginTransaction();
            try (session) {
                session.remove(cooldown);
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                this.plugin.getLogger().log(Level.SEVERE, "Failed to remove command cooldown for " + commandSender + " on command " + command, e);
            }
        });
    }

    @Override
    public List<CooldownData> getCooldownDataList() {
        var section = plugin.getConfig().getConfigurationSection("command-cooldowns");
        if (section == null) return List.of();

        List<CooldownData> cooldownDataList = new ArrayList<>();
        for (String key : section.getKeys(false)) {
            String timeunitName = section.getString(key + ".timeunit", "SECONDS");
            long time = section.getLong(key + ".time");
            cooldownDataList.add(new CooldownData(key, TimeUnit.valueOf(timeunitName), time));
        }
        return cooldownDataList;
    }
}
