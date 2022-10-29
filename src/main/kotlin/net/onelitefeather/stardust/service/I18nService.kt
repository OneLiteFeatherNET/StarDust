package net.onelitefeather.stardust.service

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.util.UTF8ResourceBundleControl
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.util.NOT_AVAILABLE_CONFIG_FALLBACK
import java.text.MessageFormat
import java.util.ResourceBundle
import kotlin.math.abs

class I18nService(val stardustPlugin: StardustPlugin) {

    val defaultMessages: ResourceBundle = ResourceBundle.getBundle("essentials", UTF8ResourceBundleControl())

    fun getPluginPrefix(): String {
        return MessageFormat.format(defaultMessages.getString("plugin.prefix"), stardustPlugin.name)
    }

    fun getMessage(key: String, vararg variables: Any): String {
        return if (defaultMessages.containsKey(key)) MessageFormat.format(defaultMessages.getString(key), variables) else NOT_AVAILABLE_CONFIG_FALLBACK.format(key)
    }

    fun translateLegacyString(message: Component): String {
        return MiniMessage.miniMessage().serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(LegacyComponentSerializer.legacyAmpersand().serialize(message)))
    }

    fun getRemainingTime(time: Long): String {
        val diff = abs(time - System.currentTimeMillis())
        val seconds = diff / 1000 % 60
        val minutes = diff / (1000 * 60) % 60
        val hours = diff / (1000 * 60 * 60) % 24
        val days = diff / (1000 * 60 * 60 * 24) % 365
        val remainingTime: String = if (diff > 60 * 60 * 24) {
            getMessage("remaining-time.days", days, hours, minutes, seconds)
        } else if (diff > 60 * 60) {
            getMessage("remaining-time.hours", hours, minutes, seconds)
        } else if (diff > 60) {
            getMessage("remaining-time.minutes", minutes, seconds)
        } else {
            getMessage("remaining-time.seconds", seconds)
        }
        return remainingTime
    }
}