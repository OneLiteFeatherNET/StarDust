package net.onelitefeather.stardust.setting;

import org.bukkit.configuration.file.YamlConfiguration;

public record EssentialsSetting<T>(String name, String configKey, T defaultValue) {

    public String getName() {
        return name;
    }

    public String getConfigKey() {
        return configKey;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public T getValue(YamlConfiguration config, Class<T> target) {
        return config.getObject(this.configKey, target, this.defaultValue);
    }

}
