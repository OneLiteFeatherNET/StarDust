package net.onelitefeather.stardust.api.registry;

import net.onelitefeather.stardust.api.player.setting.PlayerSetting;
import net.onelitefeather.stardust.api.player.setting.SettingKey;
import net.onelitefeather.stardust.api.player.setting.SettingValueCodec;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SettingRegistry {

    private static final Map<String, SettingKey<?>> defaultSettings = new ConcurrentHashMap<>();

    public <T> void register(SettingKey<T> key) {
        if (defaultSettings.putIfAbsent(key.key(), key) != null) {
            throw new IllegalStateException("Setting key already registered: " + key.key());
        }
    }

    public static PlayerSetting fromKey(SettingKey<?> key) {
        return new PlayerSetting(SettingValueCodec.encode(key.defaultValue()), key.schemaVersion(), Clock.systemUTC().instant());
    }

    /**
     * <b>Warning: No safety for static access method!</b>
     *
     * @param key the given key name
     * @param <T> the type classifier
     * @return the expected setting key
     */
    @SuppressWarnings("unchecked")
    public static <T> SettingKey<T> getSettingKey(String key) {
        return (SettingKey<T>) defaultSettings.get(key);
    }

    public static Map<String, SettingKey<?>> getDefaults() {
        return defaultSettings;
    }

    public List<SettingKey<?>> getDefaultSettings() {
        return (List<SettingKey<?>>) getDefaults().values();
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<SettingKey<T>> get(String key) {
        return Optional.ofNullable((SettingKey<T>) defaultSettings.get(key));
    }

    public List<SettingKey<?>> getVanishSettings() {
        return defaultSettings.values().stream().filter(settingKey -> settingKey.key().startsWith("vanish_")).toList();
    }

    public void registerDefaults() {
        register(SettingKey.builder(Boolean.class, "allow_flight")
                .defaultValue(Boolean.FALSE)
                .schemaVersion(1)
                .withTranslationKey("setting.allow-flight")
                .build());

        register(SettingKey.builder(Boolean.class, "vanished")
                .defaultValue(Boolean.FALSE)
                .schemaVersion(1)
                .withTranslationKey("setting.vanished")
                .withPermission("stardust.feature.vanish")
                .build());

        register(SettingKey.builder(Boolean.class, "vanish_keep_inventory")
                .defaultValue(Boolean.TRUE)
                .schemaVersion(1)
                .withPermission("stardust.feature.vanish")
                .withTranslationKey("setting.vanish-keep-inventory")
                .build());

        register(SettingKey.builder(Boolean.class, "vanish_allow_building")
                .defaultValue(Boolean.FALSE)
                .schemaVersion(1)
                .withPermission("stardust.feature.vanish")
                .withTranslationKey("setting.vanish-allow-building")
                .build());

        register(SettingKey.builder(Boolean.class, "vanish_allow_pve")
                .defaultValue(Boolean.FALSE)
                .schemaVersion(1)
                .withPermission("stardust.feature.vanish")
                .withTranslationKey("setting.vanish-allow-pve")
                .build());
    }
}
