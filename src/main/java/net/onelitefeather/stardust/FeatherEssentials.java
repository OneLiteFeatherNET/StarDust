package net.onelitefeather.stardust;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.paper.PaperCommandManager;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import net.onelitefeather.stardust.command.CommandCooldown;
import net.onelitefeather.stardust.command.CommandCooldownManager;
import net.onelitefeather.stardust.command.commands.*;
import net.onelitefeather.stardust.hook.VaultHook;
import net.onelitefeather.stardust.listener.*;
import net.onelitefeather.stardust.position.SpawnManager;
import net.onelitefeather.stardust.setting.SettingManager;
import net.onelitefeather.stardust.tasks.UserTask;
import net.onelitefeather.stardust.user.User;
import net.onelitefeather.stardust.user.UserManager;
import net.onelitefeather.stardust.util.Constants;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;

public class FeatherEssentials extends JavaPlugin {

    private AnnotationParser<CommandSender> annotationParser;
    private MinecraftHelp<CommandSender> minecraftHelp;
    private UserManager userManager;
    private UserTask userTask;
    private SettingManager settingManager;
    private CommandCooldownManager commandCooldownManager;
    private VaultHook vaultHook;
    private PacketListener packetListener;
    private SessionFactory sessionFactory;
    private NamespacedKey signedNameSpacedKey;
    private SpawnManager spawnManager;

    private ResourceBundle messages;

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

        this.settingManager = new SettingManager(this);

        this.spawnManager = new SpawnManager(this);

        this.vaultHook = new VaultHook(this);
        this.vaultHook.initVaultSupport();

        if (pluginManager.isPluginEnabled("ProtocolLib")) {
            this.packetListener = new PacketListener(this);
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
        hibernateProperties.put("hibernate.integration.envers.enabled", false);
        this.sessionFactory = buildSessionFactory(hibernateProperties);

        this.commandCooldownManager = new CommandCooldownManager(this);
        this.userManager = new UserManager(this);

        if (this.sessionFactory != null && !this.sessionFactory.isClosed()) {
            this.commandCooldownManager.load();
            this.userManager.load();
        }

        buildCommandSystem();
        pluginManager.registerEvents(new EntityListener(this), this);
        pluginManager.registerEvents(new InventoryClickListener(this), this);
        pluginManager.registerEvents(new PlayerListener(this), this);
        pluginManager.registerEvents(new PlayerConnectionListener(this), this);
        pluginManager.registerEvents(new PlayerSpawnListener(this.spawnManager), this);

        this.userTask = new UserTask(this);
        getServer().getScheduler().runTaskTimerAsynchronously(this, bukkitTask -> this.userTask.run(), 0L, 20);
        getLogger().log(Level.INFO, "Plugin " + getDescription().getName() + " was enabled in " + (System.currentTimeMillis() - time) + "ms.");
    }

    @Override
    public void onDisable() {
        if (this.packetListener != null) {
            this.packetListener.unregister();
        }
    }

    public AnnotationParser<CommandSender> getAnnotationParser() {
        return annotationParser;
    }

    public MinecraftHelp<CommandSender> getMinecraftHelp() {
        return minecraftHelp;
    }

    public NamespacedKey getSignedNameSpacedKey() {
        return signedNameSpacedKey;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    public SettingManager getSettingManager() {
        return settingManager;
    }

    public VaultHook getVaultHook() {
        return vaultHook;
    }

    public CommandCooldownManager getCommandCooldownManager() {
        return this.commandCooldownManager;
    }

    public UserManager getUserManager() {
        return this.userManager;
    }

    public List<Player> getVisiblePlayers(CommandSender commandSender) {

        List<Player> players = new ArrayList<>(getServer().getOnlinePlayers());
        if (commandSender instanceof Player player) {
            players.removeIf(current -> !player.canSee(current));
        }

        return players;
    }

    public ResourceBundle getMessages() {
        return messages;
    }

    public String getPrefix() {
        return MessageFormat.format(this.messages.getString("plugin.prefix"), this.getDescription().getName());
    }

    public String getRawMessage(String key) {
        return this.messages.containsKey(key) ? this.messages.getString(key) : String.format(Constants.NOT_AVAILABLE_CONFIG_FALLBACK, key);
    }

    public String getMessage(String key, Object... variables) {
        return this.messages.containsKey(key) ? MessageFormat.format(this.messages.getString(key), variables) : String.format(Constants.NOT_AVAILABLE_CONFIG_FALLBACK, key);
    }

    public String getRemainingTime(long time) {

        long diff = Math.abs(time - System.currentTimeMillis());
        long seconds = (diff / 1000) % 60;
        long minutes = (diff / (1000 * 60)) % 60;
        long hours = (diff / (1000 * 60 * 60)) % 24;
        long days = (diff / (1000 * 60 * 60 * 24)) % 365;

        String remainingTime;
        if (diff > 60 * 60 * 24) {
            remainingTime = this.getMessage("remaining-time.days", days, hours, minutes, seconds);
        } else if (diff > 60 * 60) {
            remainingTime = this.getMessage("remaining-time.hours", hours, minutes, seconds);
        } else if (diff > 60) {
            remainingTime = this.getMessage("remaining-time.minutes", minutes, seconds);
        } else {
            remainingTime = this.getMessage("remaining-time.seconds", seconds);
        }

        return remainingTime;
    }

    private void buildCommandSystem() {
        final PaperCommandManager<CommandSender> bukkitCommandManager;
        try {
            bukkitCommandManager = new PaperCommandManager<>(this, CommandExecutionCoordinator.simpleCoordinator(), Function.identity(), Function.identity());

            if (bukkitCommandManager.queryCapability(CloudBukkitCapabilities.BRIGADIER)) {
                bukkitCommandManager.registerBrigadier();
            }

            if (bukkitCommandManager.queryCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
                bukkitCommandManager.registerAsynchronousCompletions();
            }

            final Function<ParserParameters, CommandMeta> commandMetaFunction = p -> CommandMeta.simple().with(CommandMeta.DESCRIPTION, p.get(StandardParameters.DESCRIPTION, "No description")).build();
            this.annotationParser = new AnnotationParser<>(bukkitCommandManager, CommandSender.class, commandMetaFunction);
            this.minecraftHelp = MinecraftHelp.createNative("/featheressentials help", bukkitCommandManager);
        } catch (final Exception e) {
            this.getLogger().warning("Failed to initialize Brigadier support: " + e.getMessage());
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.minecraftHelp.setHelpColors(MinecraftHelp.HelpColors.of(
                TextColor.color(251, 0, 0),
                TextColor.color(253, 108, 62),
                TextColor.color(253, 197, 0),
                NamedTextColor.GRAY,
                NamedTextColor.DARK_GRAY
        ));

        this.annotationParser.parse(new HelpCommand(this));
        this.annotationParser.parse(new EnderchestCommand(this));
        this.annotationParser.parse(new FlightCommand(this));
        this.annotationParser.parse(new GameModeCommand(this));
        this.annotationParser.parse(new GlowCommand(this));

        this.annotationParser.parse(new SetSpawnCommand(this, this.spawnManager));
        this.annotationParser.parse(new SpawnCommand(this, this.spawnManager));

        this.annotationParser.parse(new GodmodeCommand(this));

        this.annotationParser.parse(new HealCommand(this));
        this.annotationParser.parse(new InvseeCommand(this));
        this.annotationParser.parse(new RenameCommand(this));
        this.annotationParser.parse(new RepairCommand(this));
        this.annotationParser.parse(new SignCommand(this));
        this.annotationParser.parse(new SkullCommand(this));

        this.annotationParser.parse(new SpawnMobCommand(this));
        this.annotationParser.parse(new SpeedCommand(this));

        this.annotationParser.parse(new UserCommand(this));
        this.annotationParser.parse(new VanishCommand(this));

        this.annotationParser.parse(new TpCommand(this));
    }

    public SessionFactory buildSessionFactory(Map<String, Object> hibernateProperties) {

        Configuration configuration = new Configuration();
        Properties properties = new Properties();

        properties.putAll(hibernateProperties);
        configuration.setProperties(properties);
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(CommandCooldown.class);

        return configuration.buildSessionFactory();
    }

}
