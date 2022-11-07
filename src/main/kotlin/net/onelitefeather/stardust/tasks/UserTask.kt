package net.onelitefeather.stardust.tasks

import io.sentry.Sentry
import net.kyori.adventure.text.minimessage.MiniMessage
import net.onelitefeather.stardust.StardustPlugin
import org.bukkit.entity.Player

class UserTask(val stardustPlugin: StardustPlugin) : Runnable {

    override fun run() {
        try {
            // Rework later
            stardustPlugin.server.onlinePlayers.forEach { player: Player ->
                val user = stardustPlugin.userService.getUser(player.uniqueId)
                if (user != null && user.properties.isVanished()) {
                    player.sendActionBar(
                        MiniMessage.miniMessage().deserialize(stardustPlugin.i18nService.getMessage("plugin.vanish-actionbar"))
                    )
                }
            }
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
    }
}