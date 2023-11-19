package net.onelitefeather.stardust.service

import net.kyori.adventure.util.UTF8ResourceBundleControl
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.util.NOT_AVAILABLE_CONFIG_FALLBACK
import java.text.MessageFormat
import java.util.*
import kotlin.math.abs

class I18nService(val stardustPlugin: StardustPlugin) {

    val defaultMessages: ResourceBundle = ResourceBundle.getBundle("stardust", Locale.US, UTF8ResourceBundleControl())

    fun getPluginPrefix(): String {
        return "<lang:plugin.prefix:${stardustPlugin.name}"
    }

    fun getMessage(key: String, vararg variables: Any): String {
        return if (defaultMessages.containsKey(key)) MessageFormat(defaultMessages.getString(key)).format(variables)
        else NOT_AVAILABLE_CONFIG_FALLBACK.format(key)
    }


}