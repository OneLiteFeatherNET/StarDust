package net.onelitefeather.stardust.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player

interface StringUtils {
    /**
     * This method converts a text into a mini message string.
     */
    fun colorText(text: String): Component {
        return MiniMessage.miniMessage().deserialize(text)
    }

    fun secureComponent(player: Player, origin: Component) = secureComponent(player, PERMISSION_SECURE_MESSAGE, origin)

    fun secureComponent(player: Player, permission: String, origin: Component): Component {
        val hasPerm = player.hasPermission(permission)
        return origin.clickEvent(if (hasPerm) origin.clickEvent() else null)
            .hoverEvent(if (hasPerm) origin.hoverEvent() else null)
    }

    /**
     * Translate a component and the content into mini message string.
     */
    fun convertComponentToString(message: Component): String {
        return MiniMessage.miniMessage().serialize(message)
    }
}