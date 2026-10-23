package net.onelitefeather.stardust.api.registry;

import net.onelitefeather.stardust.api.player.setting.SettingKey;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SettingRegistry {

    private final Map<String, SettingKey<?>> keys;

    public SettingRegistry() {
        this.keys = new ConcurrentHashMap<>();
    }

    public <T> void register(SettingKey<T> key) {
        if (keys.putIfAbsent(key.key(), key) != null) {
            throw new IllegalStateException("Setting key already registered: " + key.key());
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<SettingKey<T>> get(String key) {
        return Optional.ofNullable((SettingKey<T>) keys.get(key));
    }

    public void registerDefaults() {
        register(SettingKey.builder(Boolean.class, "flying")
                .defaultValue(Boolean.FALSE)
                .schemaVersion(1)
                .build());

        register(SettingKey.builder(Boolean.class, "vanished")
                .defaultValue(Boolean.FALSE)
                .schemaVersion(1)
                .withPermission("stardust.feature.vanish")
                .build());

        register(SettingKey.builder(Boolean.class, "vanish_no_drop")
                .defaultValue(Boolean.FALSE)
                .schemaVersion(1)
                .withPermission("stardust.feature.vanish")
                .build());

        register(SettingKey.builder(Boolean.class, "vanish_allow_building")
                .defaultValue(Boolean.FALSE)
                .schemaVersion(1)
                .withPermission("stardust.feature.vanish")
                .build());

        register(SettingKey.builder(Boolean.class, "vanish_allow_pve")
                .defaultValue(Boolean.FALSE)
                .schemaVersion(1)
                .withPermission("stardust.feature.vanish")
                .build());

    }
}
