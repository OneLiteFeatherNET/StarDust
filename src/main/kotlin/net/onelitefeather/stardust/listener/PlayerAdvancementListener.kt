package net.onelitefeather.stardust.listener

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent
import net.onelitefeather.stardust.StardustPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PlayerAdvancementListener(private val stardustPlugin: StardustPlugin) : Listener {

    @EventHandler
    private fun onPlayerAdvancement(event: PlayerAdvancementCriterionGrantEvent) {
        val player = event.player;
        if (stardustPlugin.userService.playerVanishService.isVanished(player)) {
            event.isCancelled = true;
        }
    }
}