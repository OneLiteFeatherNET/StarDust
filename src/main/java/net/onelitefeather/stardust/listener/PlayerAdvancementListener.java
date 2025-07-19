package net.onelitefeather.stardust.listener;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import net.onelitefeather.stardust.StardustPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerAdvancementListener implements Listener {
    private final StardustPlugin plugin;

    public PlayerAdvancementListener(StardustPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerAdvancement(PlayerAdvancementCriterionGrantEvent event) {
        var player = event.getPlayer();
        if (plugin.getUserService().getVanishService().isVanished(player)) {
            event.setCancelled(true);
        }
    }
}