package net.onelitefeather.stardust.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

interface StringUtils {
    /**
     * This method converts a text into a minimessage (legacy format) and colors it
     */
    fun colorText(text: String): String {
        return convertComponentToString(
            MiniMessage.miniMessage().deserialize(text)
        )
    }

    fun convertComponentToString(message: Component): String {
        return MiniMessage.miniMessage().serialize(
            LegacyComponentSerializer.legacyAmpersand()
                .deserialize(LegacyComponentSerializer.legacyAmpersand().serialize(message))
        )
    }
}