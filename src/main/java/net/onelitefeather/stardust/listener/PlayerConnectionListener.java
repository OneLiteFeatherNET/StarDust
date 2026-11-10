package net.onelitefeather.stardust.listener;

import net.kyori.adventure.text.Component;
import net.onelitefeather.stardust.StardustPlugin;
import net.onelitefeather.stardust.api.player.StardustPlayer;
import net.onelitefeather.stardust.util.PlayerUtil;
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

        var userService = this.stardustPlugin.getUserService();
        var user = userService.getUser(player.getUniqueId());
        if(user != null) this.stardustPlugin.getPlayerService().migrateUser(user, userService);

        if (!stardustPlugin.getPlayerService().playerExists(player.getUniqueId())) {
            var stardustPlayer = StardustPlayer.create(player.getUniqueId(), player.getName(), PlayerUtil.findSettingsForPlayer(player));
            stardustPlugin.getPlayerService().saveOrUpdate(stardustPlayer);
            player.sendMessage(Component.translatable("plugin.new-player").arguments(Component.translatable("plugin.prefix"), player.displayName()));
            joinMessage(event);
            return;
        }

        var stardustPlayer = stardustPlugin.getPlayerService().findById(player.getUniqueId());
        if (!player.getName().equalsIgnoreCase(stardustPlayer.getName())) {
            stardustPlugin.getLogger().log(Level.INFO, String.format("Updating Username from %s to %s", stardustPlayer.getName(), player.getName()));
            stardustPlugin.getPlayerService().saveOrUpdate(stardustPlayer.withName(player.getName()));
        }

        joinMessage(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        try {
            if (this.stardustPlugin.getVanishService().isVanished(player.getUniqueId())) {
                event.quitMessage(null);
            } else {
                event.quitMessage(Component.translatable("listener.quit-message").arguments(player.displayName()));
            }
            stardustPlugin.getVanishService().handlePlayerQuit(player.getUniqueId());
        } catch (Exception e) {
            stardustPlugin.getLogger().log(Level.SEVERE, "Something went wrong during the quit process", e);
        }
    }

    private void joinMessage(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        boolean isVanished = stardustPlugin.getVanishService().handlePlayerJoin(player.getUniqueId());
        if (isVanished) {
            event.joinMessage(null);
        } else {
            event.joinMessage(Component.translatable("listener.join-message").arguments(player.displayName()));
        }
    }
}
