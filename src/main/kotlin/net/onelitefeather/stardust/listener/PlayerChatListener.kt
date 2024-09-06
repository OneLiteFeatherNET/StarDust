package net.onelitefeather.stardust.listener

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.user.UserPropertyType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PlayerChatListener(private val stardustPlugin: StardustPlugin) : Listener {

    @EventHandler
    fun onAsyncChat(event: AsyncChatEvent) {
        val player = event.player
        val user = stardustPlugin.userService.getUser(player.uniqueId)

        if (user != null && user.properties.getProperty(UserPropertyType.VANISHED).getValue<Boolean>() == true) {

            if (!user.hasChatConfirmation(stardustPlugin.chatConfirmationKey)) {
                user.confirmChatMessage(stardustPlugin.chatConfirmationKey, true)
                event.isCancelled = true
                player.sendMessage(Component.translatable("vanish.confirm-chat-message").arguments(stardustPlugin.getPluginPrefix()))
            } else {
                user.confirmChatMessage(stardustPlugin.chatConfirmationKey, false)
            }

            return
        }
    }
}