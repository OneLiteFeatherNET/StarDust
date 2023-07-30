package net.onelitefeather.stardust.api.utils

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.translation.TranslationRegistry
import net.kyori.adventure.translation.Translator
import java.text.MessageFormat
import java.util.*

class DoubleParsingI18nMiniMessage(private val translationRegistry: TranslationRegistry) : Translator {
    override fun name(): Key {
        return translationRegistry.name()
    }

    override fun translate(key: String, locale: Locale): MessageFormat? {
        return translationRegistry.translate(key, locale)
    }

    override fun translate(component: TranslatableComponent, locale: Locale): Component? {
        val fallback = component.fallback() ?: "<red>Unknown translation ${component.key()}"
        val message = this.translate(component.key(), locale) ?: return super.translate(component, locale) ?: return null
        component.args().forEach {
            println(it.toString())
            println(MiniMessage.miniMessage().serialize(it).replace("'","\""))
        }
        println(MiniMessage.miniMessage().serialize(MiniMessage.miniMessage().deserialize(message.format(component.args().map { MiniMessage.miniMessage().serialize(it).replace("'","\'") }.toTypedArray()))))
        return MiniMessage.miniMessage().deserialize(message.format(component.args().map { MiniMessage.miniMessage().serialize(it) }.toTypedArray()))
    }

}