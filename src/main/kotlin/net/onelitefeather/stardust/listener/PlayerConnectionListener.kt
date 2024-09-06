package net.onelitefeather.stardust.listener

import net.kyori.adventure.text.Component
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.util.PlayerUtils
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.logging.Level

class PlayerConnectionListener(private val stardustPlugin: StardustPlugin) : Listener, PlayerUtils {

    @EventHandler
    fun handlePlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        try {

            val user = stardustPlugin.userService.getUser(player.uniqueId)
            if (user == null) {

                //Register a new User
                stardustPlugin.userService.registerUser(player) {
                    player.sendMessage(Component.translatable("plugin.first-join").arguments(stardustPlugin.getPluginPrefix(), player.displayName()))
                }
                return
            }

            if (!player.name.equals(user.name, true)) {
                stardustPlugin.logger.log(Level.INFO, "Updating Username from %s to %s".format(user.name, player.name))
                stardustPlugin.userService.updateUser(user.copy(name = player.name))
            }

            stardustPlugin.server.onlinePlayers.forEach { stardustPlugin.userService.playerVanishService.handlePlayerJoin(it) }

            player.allowFlight =
                user.properties.isFlying() && !player.allowFlight || player.gameMode == GameMode.CREATIVE || player.gameMode == GameMode.SPECTATOR

            if (!player.hasPermission("stardust.join.gamemode")) {
                player.gameMode = player.server.defaultGameMode
            }

            event.joinMessage(
                if (user.properties.isVanished()) null else
                Component.translatable("listener.join-message").arguments(player.displayName()))

        } catch (e: Exception) {
            this.stardustPlugin.getLogger()
                .throwing(PlayerConnectionListener::class.java.simpleName, "handlePlayerJoin", e)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        try {
            val user = stardustPlugin.userService.getUser(player.uniqueId)
            event.quitMessage(
                if (user?.properties?.isVanished() == true) null else
                    Component.translatable("listener.quit-message").arguments(player.displayName())

            )
        } catch (e: Exception) {
            this.stardustPlugin.logger
                .throwing(PlayerConnectionListener::class.java.simpleName, "onPlayerQuit", e)
        }
    }
}