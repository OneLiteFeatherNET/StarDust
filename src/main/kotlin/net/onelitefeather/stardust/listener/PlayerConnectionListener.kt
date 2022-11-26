package net.onelitefeather.stardust.listener

import io.sentry.Sentry
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.extenstions.addClient
import net.onelitefeather.stardust.extenstions.coloredDisplayName
import net.onelitefeather.stardust.extenstions.miniMessage
import net.onelitefeather.stardust.extenstions.toSentryUser
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerConnectionListener(private val stardustPlugin: StardustPlugin) : Listener {

    @EventHandler
    fun handlePlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        try {

            if (stardustPlugin.luckPermsService.isEnabled()) {
                stardustPlugin.playerNameTagService.updateNameTag(player)
            }

            val user = stardustPlugin.userService.getUser(player.uniqueId)
            if (user == null) {

                //Register a new User
                stardustPlugin.userService.registerUser(player) {
                    player.sendMessage(miniMessage {
                        stardustPlugin.i18nService.getMessage(
                            "plugin.first-join", *arrayOf(
                                stardustPlugin.i18nService.getPluginPrefix(), player.coloredDisplayName()
                            )
                        )
                    })
                }
                return
            }

            stardustPlugin.userService.playerVanishService.onPlayerJoin(player)

            player.allowFlight =
                user.properties.isFlying() && !player.allowFlight || player.gameMode == GameMode.CREATIVE || player.gameMode == GameMode.SPECTATOR

            if (!player.hasPermission("stardust.join.gamemode")) {
                player.gameMode = player.server.defaultGameMode
            }

            event.joinMessage(if (user.properties.isVanished()) null else miniMessage {
                stardustPlugin.i18nService.getMessage(
                    "listener.join-message", *arrayOf(player.coloredDisplayName())
                )
            })
        } catch (e: Exception) {
            Sentry.captureException(e) {
                it.user = player.toSentryUser()
                player.addClient(it)
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        try {
            val user = stardustPlugin.userService.getUser(player.uniqueId)
            event.quitMessage(if (user?.properties?.isVanished() == true) null else miniMessage {
                stardustPlugin.i18nService.getMessage(
                    "listener.quit-message", *arrayOf(player.coloredDisplayName())
                )
            })
        } catch (e: Exception) {
            Sentry.captureException(e) {
                it.user = player.toSentryUser()
                player.addClient(it)
            }
        }
    }
}