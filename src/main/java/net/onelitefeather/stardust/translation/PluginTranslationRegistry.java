package net.onelitefeather.stardust.translation;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.translation.Translator;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PluginTranslationRegistry implements Translator {
    private final TranslationRegistry translator;

    public PluginTranslationRegistry(TranslationRegistry translator) {
        this.translator = translator;
    }

    @Override
    public @NotNull Key name() {
        return this.translator.name();
    }

    @Override
    public MessageFormat translate(@NotNull String key, @NotNull Locale locale) {
        return this.translator.translate(key, locale);
    }

    @Override
    public Component translate(TranslatableComponent component, @NotNull Locale locale) {
        MessageFormat miniMessageResult = this.translate(component.key(), locale);
        if (miniMessageResult == null) {
            return null;
        }

        List<String> values = new ArrayList<>();
        for (TranslationArgument argumentComponent : component.arguments()) {
            String serialized = MiniMessage.miniMessage()
                    .serialize(GlobalTranslator.render(argumentComponent.asComponent(), locale));

            values.add(serialized);
        }

        Component resultComponent = MiniMessage.miniMessage().deserialize(
                miniMessageResult.format(values.toArray(new String[0]))
        );

        List<Component> children = new ArrayList<>();
        for (Component child : resultComponent.children()) {
            children.add(GlobalTranslator.render(child, locale));
        }

        return GlobalTranslator.render(resultComponent, locale).children(children);
    }
}
