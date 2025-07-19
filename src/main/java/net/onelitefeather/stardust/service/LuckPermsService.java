package net.onelitefeather.stardust.service;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.user.UserLoadEvent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.onelitefeather.stardust.StardustPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class LuckPermsService {

    private final StardustPlugin plugin;
    private LuckPerms luckPerms;
    private final List<EventSubscription<?>> luckPermsEvents = new ArrayList<>();

    public LuckPermsService(StardustPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        try {
            var server = plugin.getServer();
            if (server.getPluginManager().isPluginEnabled("LuckPerms")) {
                var provider = server.getServicesManager().getRegistration(LuckPerms.class);
                if (provider != null) {
                    luckPerms = provider.getProvider();
                    plugin.getLogger().log(Level.INFO, "Using " + provider.getPlugin().getName() + " as Permission provider.");
                    subscribeToEvents();
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Cannot hook into luckperms", e);
        }
    }

    private void subscribeToEvents() {
        luckPermsEvents.add(luckPerms.getEventBus().subscribe(UserLoadEvent.class, event -> disableVanish(event.getUser())));
    }

    private void disableVanish(User user) {
        Player player = Bukkit.getPlayer(user.getUniqueId());
        if (player == null) return;
        if (!plugin.getUserService().getVanishService().isVanished(player)) return;
        plugin.getUserService().getVanishService().toggle(player);
    }

    public void unsubscribeEvents() {
        for (EventSubscription<?> sub : luckPermsEvents) {
            sub.close();
        }
        luckPermsEvents.clear();
    }

    public boolean isEnabled() {
        return plugin.getServer().getPluginManager().isPluginEnabled("LuckPerms");
    }

    public Group getPrimaryGroup(Player player) {
        var user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return getDefaultGroup();
        var group = luckPerms.getGroupManager().getGroup(user.getPrimaryGroup());
        return group != null ? group : getDefaultGroup();
    }

    public int getGroupPriority(Player player) {
        if (!isEnabled()) return 0;
        Group group = getPrimaryGroup(player);
        return group.getWeight().orElse(0);
    }

    public Group getDefaultGroup() {
        return luckPerms.getGroupManager().getGroup("default");
    }
}