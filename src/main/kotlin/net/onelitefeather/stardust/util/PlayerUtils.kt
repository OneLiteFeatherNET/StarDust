package net.onelitefeather.stardust.util

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.onelitefeather.stardust.StardustPlugin
import org.bukkit.GameMode
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

interface PlayerUtils {

    fun canEnterFlyMode(player: Player) = player.gameMode == GameMode.CREATIVE || player.gameMode == GameMode.SPECTATOR

    /**
     * Remove the player as target for attack/look from the mobs in definied radius.
     * Prevents looks from entities to the player.
     */
    fun removeEnemies(player: Player, radius: Double) {
        val plugin = JavaPlugin.getPlugin(StardustPlugin::class.java)
        player.server.scheduler.getMainThreadExecutor(plugin).execute {
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