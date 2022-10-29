package net.onelitefeather.stardust;

import net.kyori.adventure.util.UTF8ResourceBundleControl;
import net.onelitefeather.stardust.listener.*;
import net.onelitefeather.stardust.setting.SettingManager;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ResourceBundle;
import java.util.logging.Level;

public class FeatherEssentials extends JavaPlugin {


    private SettingManager settingManager;
    private PacketListener packetListener;
    private NamespacedKey signedNameSpacedKey;

    public static StardustPlugin getInstance() {
        return JavaPlugin.getPlugin(StardustPlugin.class);
    }

    @Override
    public void onLoad() {
        this.messages = ResourceBundle.getBundle("essentials", new UTF8ResourceBundleControl());
    }

    @Override
    public void onEnable() {

        long time = System.currentTimeMillis();
        this.signedNameSpacedKey = new NamespacedKey(this, "signed");

        PluginManager pluginManager = getServer().getPluginManager();

        saveDefaultConfig();
        FileConfiguration config = getConfig();
        config.options().copyDefaults(true);

        this.settingManager = new SettingManager(this);

        if (pluginManager.isPluginEnabled("ProtocolLib")) {
            this.packetListener = new PacketListener(this);
        }

        pluginManager.registerEvents(new EntityListener(this), this);
        pluginManager.registerEvents(new InventoryClickListener(this), this);
        pluginManager.registerEvents(new PlayerListener(this), this);
        pluginManager.registerEvents(new PlayerConnectionListener(this), this);


        getLogger().log(Level.INFO, "Plugin " + getDescription().getName() + " was enabled in " + (System.currentTimeMillis() - time) + "ms.");
    }

    @Override
    public void onDisable() {
        if (this.packetListener != null) {
            this.packetListener.unregister();
        }
    }

    public NamespacedKey getSignedNameSpacedKey() {
        return signedNameSpacedKey;
    }

    public SettingManager getSettingManager() {
        return settingManager;
    }
}
