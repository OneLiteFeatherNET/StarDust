package net.onelitefeather.stardust.setting;

import net.onelitefeather.stardust.FeatherEssentials;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;

public class SettingManager {

    private final FeatherEssentials featherEssentials;

    private final List<EssentialsSetting<?>> essentialsSettingList;

    public SettingManager(FeatherEssentials featherEssentials) {
        this.featherEssentials = featherEssentials;
        this.essentialsSettingList = new ArrayList<>();
        this.essentialsSettingList.add(new EssentialsSetting<>("debug", "settings.debug", true));
        this.essentialsSettingList.add(new EssentialsSetting<>("auto_afk_time", "settings.auto-afk-time", 300L));
        this.essentialsSettingList.add(new EssentialsSetting<>("afk_kick_time", "settings.afk-kick-time", 600L));
    }

    public <T> T getSetting(YamlConfiguration configuration, String settingName, Class<T> target, T fallback) {

        for(EssentialsSetting<?> essentialsSetting : this.essentialsSettingList) {
            if (essentialsSetting.getName().equalsIgnoreCase(settingName)) {
                T value = configuration.getObject(essentialsSetting.getConfigKey(), target);
                return value != null ? value : fallback;
            }
        }

        return fallback;
    }

    public <T> T getSetting(String settingName, Class<T> target, T fallback) {
        return getSetting((YamlConfiguration) this.featherEssentials.getConfig(), settingName, target, fallback);
    }


}
