package net.onelitefeather.stardust.listener

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.onelitefeather.stardust.StardustPlugin
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PlayerChatListener(private val stardustPlugin: StardustPlugin) : Listener {

    @EventHandler
    fun onAsyncChat(event: AsyncChatEvent) {
        if (event.isCancelled) return
        event.renderer { _: Player, sourceDisplayName: Component, _: Component, _: Audience ->
            Component.text()
                .append(sourceDisplayName)
                .append(Component.text(": "))
                .append(event.message())
                .build()
        }
    }
}