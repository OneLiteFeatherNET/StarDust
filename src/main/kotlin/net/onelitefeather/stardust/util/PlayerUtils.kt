package net.onelitefeather.stardust.util

import org.bukkit.entity.Mob
import org.bukkit.entity.Player

interface PlayerUtils {

    /**
     * Remove the player as target for attack/look from the mobs in definied radius.
     * Prevents looks from entities to the player.
     */
    fun removeEnemies(player: Player, radius: Double) {
        player.location.getNearbyLivingEntities(radius).forEach { livingEntity ->
            if (livingEntity is Mob) {
                val target = livingEntity.target ?: return@forEach
                if (target == player) {
                    livingEntity.target = null
                }
            }
        }
    }
}