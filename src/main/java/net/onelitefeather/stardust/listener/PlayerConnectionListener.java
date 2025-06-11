package net.onelitefeather.stardust.listener;

import org.bukkit.event.Listener;

import net.kyori.adventure.text.Component;
import net.onelitefeather.stardust.StardustPlugin;
import net.onelitefeather.stardust.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.logging.Level;

public class PlayerConnectionListener implements Listener {

    private final StardustPlugin stardustPlugin;

    public PlayerConnectionListener(StardustPlugin stardustPlugin) {
        this.stardustPlugin = stardustPlugin;
    }

    @EventHandler
    public void handlePlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        try {
            User user = stardustPlugin.getUserService().getUser(player.getUniqueId());
            if (user == null) {

                stardustPlugin.getUserService().registerUser(player, userResult -> player.sendMessage(Component.translatable("plugin.first-join")
                        .arguments(stardustPlugin.getPrefix(), player.displayName())));

                joinMessage(event);
                return;
            }

            if (!player.getName().equalsIgnoreCase(user.getName())) {
                stardustPlugin.getLogger().log(Level.INFO, String.format("Updating Username from %s to %s", user.getName(), player.getName()));
                stardustPlugin.getUserService().updateUser(user.withName(player.getName()));
            }

            joinMessage(event);
        } catch (Exception e) {
            stardustPlugin.getLogger().log(Level.SEVERE, "Something went wrong during the join process", e);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        try {
            User user = stardustPlugin.getUserService().getUser(player.getUniqueId());
            if (user != null && user.isVanished()) {
                event.quitMessage(null);
            } else {
                event.quitMessage(Component.translatable("listener.quit-message").arguments(player.displayName()));
            }
            stardustPlugin.getUserService().getVanishService().handlePlayerQuit(player);
        } catch (Exception e) {
            stardustPlugin.getLogger().log(Level.SEVERE, "Something went wrong during the quit process", e);
        }
    }

    private void joinMessage(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        boolean isVanished = stardustPlugin.getUserService().getVanishService().handlePlayerJoin(player);
        if (isVanished) {
            event.joinMessage(null);
        } else {
            event.joinMessage(Component.translatable("listener.join-message").arguments(player.displayName()));
        }
    }
}
