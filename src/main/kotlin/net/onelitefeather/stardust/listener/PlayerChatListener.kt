package net.onelitefeather.stardust.listener

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.onelitefeather.stardust.StardustPlugin
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PlayerChatListener(private val stardustPlugin: StardustPlugin) : Listener {

    private val chatConfirmationKey = NamespacedKey(stardustPlugin, "chat_confirmation")

    @EventHandler
    fun onAsyncChat(event: AsyncChatEvent) {
        val player = event.player
        val user = stardustPlugin.userService.getUser(player.uniqueId)

        if (user != null && user.isVanished()) {

            if (!user.hasChatConfirmation(chatConfirmationKey)) {
                user.confirmChatMessage(chatConfirmationKey, true)
                event.isCancelled = true
                player.sendMessage(Component.translatable("vanish.confirm-chat-message").arguments(stardustPlugin.getPluginPrefix()))
            } else {
                user.confirmChatMessage(chatConfirmationKey, false)
            }

            return
        }
    }
}