package net.onelitefeather.stardust.util

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.translation.GlobalTranslator
import net.kyori.adventure.translation.TranslationRegistry
import net.kyori.adventure.translation.Translator
import java.text.MessageFormat
import java.util.*

/**
 * Just a simple wrapper that allows us to use '<lang:key:value> recursive and more useful'
 * Thanks to @author Lynx from Paper
 */
class LynxWrapper(private val translator: TranslationRegistry) : Translator {
    override fun name(): Key {
        return this.translator.name();
    }

    override fun translate(key: String, locale: Locale): MessageFormat? {
        return this.translator.translate(key, locale)
    }

    override fun translate(component: TranslatableComponent, locale: Locale): Component? {
        val miniMessageResult = this.translate(component.key(), locale) ?: return null
        val values = arrayOfNulls<String>(component.args().size)
        component.args().forEachIndexed { index, argumentComponent ->
            values[index] = MiniMessage.miniMessage().serialize(GlobalTranslator.render(argumentComponent, locale))
        }
        val resultComponent =
            MiniMessage.miniMessage().deserialize(miniMessageResult.format(values.filterNotNull().toTypedArray()))
        val children = resultComponent.children().toTypedArray().map { GlobalTranslator.render(it, locale) }
        return GlobalTranslator.render(resultComponent, locale).children(children)
    }

}