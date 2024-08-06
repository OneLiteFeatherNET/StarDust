package net.onelitefeather.stardust.tasks

import net.kyori.adventure.text.Component
import net.onelitefeather.stardust.StardustPlugin
import java.util.logging.Level

class UserTask(val stardustPlugin: StardustPlugin) : Runnable {

    override fun run() {
        try {
            stardustPlugin.server.onlinePlayers.forEach {
                val user = stardustPlugin.userService.getUser(it.uniqueId) ?: return
                if (user.properties.isVanished()) {
                    it.sendActionBar(Component.translatable("plugin.vanish-actionbar"))
                }
            }
        } catch (e: Exception) {
            stardustPlugin.logger.log(Level.SEVERE, "Could not run for loop", e)
        }
    }
}