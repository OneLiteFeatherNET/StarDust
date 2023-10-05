package net.onelitefeather.stardust.util

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
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

    /**
     * Convert the display name of a player into mini message string.
     */
    fun coloredDisplayName(player: Player): String {
        return  MiniMessage.miniMessage().serialize(
            LegacyComponentSerializer.legacyAmpersand()
                .deserialize(LegacyComponentSerializer.legacyAmpersand().serialize(player.displayName()))
        )
    }
}