package net.onelitefeather.stardust.listener

import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.extenstions.miniMessage
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerConnectionListener(private val stardustPlugin: StardustPlugin) : Listener {

    @EventHandler
    fun handlePlayerJoin(event: PlayerJoinEvent) {

        val player = event.player
        player.displayName(stardustPlugin.luckPermsService.getPlayerDisplayName(player))

        val user = stardustPlugin.userService.getUser(player.uniqueId)
        if (user == null) {

            //Register a new User
            stardustPlugin.userService.registerUser(player) {
                player.sendMessage(miniMessage {
                    stardustPlugin.i18nService.getMessage(
                        "plugin.first-join",
                        stardustPlugin.i18nService.getPluginPrefix(),
                        stardustPlugin.i18nService.translateLegacyString(player.displayName())
                    )
                })
            }
            return
        }

        user.checkCanFly()
        if (!player.hasPermission("stardust.join.gamemode")) {
            player.gameMode = player.server.defaultGameMode
        }

        event.joinMessage(
            if (user.isVanished()) null else miniMessage {
                stardustPlugin.i18nService.getMessage(
                    "listener.join-message",
                    stardustPlugin.i18nService.translateLegacyString(player.displayName())
                )
            }
        )
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        val user = stardustPlugin.userService.getUser(player.uniqueId)
        event.quitMessage(
            if (user?.isVanished() == true) null else miniMessage {
                stardustPlugin.i18nService.getMessage(
                    "listener.quit-message",
                    stardustPlugin.i18nService.translateLegacyString(player.displayName())
                )
            }
        )
    }
}