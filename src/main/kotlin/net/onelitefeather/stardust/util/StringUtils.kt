package net.onelitefeather.stardust.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

interface StringUtils {
    /**
     * This method converts a text into a mini message string and extract (Convert it into mini tags) the legacy color codes out of it.
     */
    fun colorText(text: String): String {
        return convertComponentToString(
            MiniMessage.miniMessage().deserialize(text)
        )
    }

    /**
     * Translate a component and the content into mini message string.
     */
    fun convertComponentToString(message: Component): String {
        return MiniMessage.miniMessage().serialize(
            LegacyComponentSerializer.legacyAmpersand()
                .deserialize(LegacyComponentSerializer.legacyAmpersand().serialize(message))
        )
    }
}