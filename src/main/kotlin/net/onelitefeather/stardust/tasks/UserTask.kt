package net.onelitefeather.stardust.tasks

import net.kyori.adventure.text.Component
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.util.PlayerUtils
import java.util.logging.Level

class UserTask(val stardustPlugin: StardustPlugin) : Runnable, PlayerUtils {

    override fun run() {
        try {

            stardustPlugin.server.onlinePlayers.forEach { player ->

                val user = stardustPlugin.userService.getUser(player.uniqueId) ?: return
                if (user.isVanished()) {
                    player.sendActionBar(Component.translatable("plugin.vanish-actionbar"))

                    //Keep fly mode if the player's gamemode was changed to survival or adventure
                    if(!player.allowFlight && !canEnterFlyMode(player)) {
                        player.allowFlight = true
                    }

                    return
                }

                if(!canEnterFlyMode(player)) {
                    player.allowFlight = user.isFlying()
                }
            }
        } catch (e: Exception) {
            stardustPlugin.logger.log(Level.SEVERE, "Could not run for loop", e)
        }
    }
}