package net.onelitefeather.stardust.api.player.setting;

import java.util.Objects;
import java.util.function.Function;

public final class SettingKey<T> {

    private final String key;
    private final String permission;
    private final Class<T> type;
    private final T defaultValue;
    private final int schemaVersion;
    private final Function<String, String> migrationFunction;

    private SettingKey(String key,
                       String permission,
                       Class<T> type,
                       T defaultValue,
                       int schemaVersion,
                       Function<String, String> migrationFunction) {
        this.key = Objects.requireNonNull(key);
        this.type = Objects.requireNonNull(type);
        this.permission = permission;
        this.defaultValue = defaultValue;
        this.schemaVersion = schemaVersion;
        this.migrationFunction = migrationFunction;
    }

    public String key() {
        return key;
    }

    public String permission() {
        return permission;
    }

    public Class<T> type() {
        return type;
    }

    public T defaultValue() {
        return defaultValue;
    }

    public int schemaVersion() {
        return schemaVersion;
    }

    public Function<String, String> migrationFunction() {
        return migrationFunction;
    }

    public static <T> Builder<T> builder(Class<T> type, String key) {
        return new Builder<>(type, key);
    }

    public static final class Builder<T> {
        private final Class<T> type;
        private final String key;
        private String permission;
        private T defaultValue;
        private int schemaVersion = 1;
        private Function<String, String> migrationFunction;

        private Builder(Class<T> type, String key) {
            this.type = type;
            this.key = key;
        }

        public Builder<T> defaultValue(T value) {
            this.defaultValue = value;
            return this;
        }

        public Builder<T> withPermission(String permission) {
            this.permission = permission;
            return this;
        }

        public Builder<T> schemaVersion(int v) {
            this.schemaVersion = v;
            return this;
        }

        public Builder<T> migration(Function<String, String> migrationFn) {
            this.migrationFunction = migrationFn;
            return this;
        }

        public SettingKey<T> build() {
            return new SettingKey<>(key, permission, type, defaultValue, schemaVersion, migrationFunction);
        }
    }
}