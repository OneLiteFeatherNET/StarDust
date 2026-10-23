package net.onelitefeather.stardust.api.player.setting;

import java.util.Objects;

/**
 * Minimal string conversion for the value types a {@link SettingKey} may use.
 */
public final class SettingValueCodec {

    private SettingValueCodec() {
    }

    public static String encode(Object value) {
        return String.valueOf(Objects.requireNonNull(value, "value"));
    }

    /**
     * @throws IllegalArgumentException if the type is unsupported or the text cannot be parsed
     */
    public static <T> T decode(Class<T> type, String text) {
        Objects.requireNonNull(text, "text");
        Object result;
        if (type == Boolean.class) {
            if (!text.equalsIgnoreCase("true") && !text.equalsIgnoreCase("false")) {
                throw new IllegalArgumentException("Not a boolean: " + text);
            }
            result = Boolean.parseBoolean(text);
        } else if (type == Integer.class) {
            result = Integer.valueOf(text);
        } else if (type == String.class) {
            result = text;
        } else {
            throw new IllegalArgumentException("Unsupported setting type: " + type.getName());
        }
        return type.cast(result);
    }
}
