package net.onelitefeather.stardust;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

public class FeatherEssentials extends JavaPlugin {

    private AnnotationParser<CommandSender> annotationParser;
    private MinecraftHelp<CommandSender> minecraftHelp;
    private NamespacedKey signedNameSpacedKey;
    private ResourceBundle messages;
    private SessionFactory sessionFactory;

    public static FeatherEssentials getInstance() {
        return JavaPlugin.getPlugin(FeatherEssentials.class);
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
        if(pluginManager.isPluginEnabled("ProtocolLib")) {
            //TODO enable packet listener
        }

        Map<String, Object> hibernateProperties = new HashMap<>();
        hibernateProperties.put(Environment.DIALECT, "org.hibernate.dialect.MariaDBDialect");
        hibernateProperties.put(Environment.HBM2DDL_AUTO, "update");
        hibernateProperties.put(Environment.DRIVER, "com.mysql.cj.jdbc.Driver");
        hibernateProperties.put(Environment.USER, config.getString("mysql.username"));
        hibernateProperties.put(Environment.PASS, config.getString("mysql.password"));
        hibernateProperties.put(Environment.SHOW_SQL, false);
        hibernateProperties.put(Environment.LOG_SESSION_METRICS, false);
        hibernateProperties.put("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
        hibernateProperties.put(Environment.URL, "jdbc:mysql://" + config.getString("mysql.hostname") + ":" + config.getString("mysql.port") + "/" + config.getString("mysql.database"));
        this.sessionFactory = buildSessionFactory(hibernateProperties);

    }

    public SessionFactory buildSessionFactory(Map<String, Object> hibernateProperties) {
        Configuration configuration = new Configuration();
        Properties properties = new Properties();
        properties.putAll(hibernateProperties);
        configuration.setProperties(properties);

        return configuration.buildSessionFactory();
    }
}
