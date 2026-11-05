package net.onelitefeather.stardust;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.translation.GlobalTranslator;
import net.onelitefeather.stardust.api.CommandCooldownService;
import net.onelitefeather.stardust.api.ItemSignService;
import net.onelitefeather.stardust.api.PlayerVanishService;
import net.onelitefeather.stardust.api.StardustPlayerService;
import net.onelitefeather.stardust.api.registry.SettingRegistry;
import net.onelitefeather.stardust.configuration.PluginConfiguration;
import net.onelitefeather.stardust.listener.*;
import net.onelitefeather.stardust.service.*;
import net.onelitefeather.stardust.task.StardustPluginTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

@SuppressWarnings("removal")
public class StardustPlugin extends JavaPlugin {

    public StardustPlugin() {
    }

    public StardustPlugin(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    private static final String PLUGIN_BLUEMAP_ENABLED = "BlueMap";
    private final List<Locale> supportedLocals = List.of(Locale.US, Locale.GERMANY);

    private SyncFrogService syncFrogService;
    private DatabaseConnectionService databaseService;
    private UserService userService;
    private CommandCooldownService cooldownService;
    private PlayerVanishService vanishService;
    private StardustPlayerService playerService;
    private LuckPermsService luckPermsService;
    private PaperCommandService commandService;
    private SettingRegistry settingRegistry;

    private ItemSignService<ItemStack, Player> itemSignService;
    private VanishNoPacketListener packetListener;
    private PluginConfiguration pluginConfiguration;
    private MiniMessageTranslationStore translationStore;

    private BukkitTask pluginTask;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
        this.packetListener = new VanishNoPacketListener(this);
        this.packetListener.register();
    }

    public MiniMessageTranslationStore getTranslationStore() {
        return translationStore;
    }

    @Override
    public void onEnable() {

        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        this.pluginConfiguration = new PluginConfiguration(getConfig());

        this.translationStore = MiniMessageTranslationStore.create(Key.key("stardust", "translations"));
        this.translationStore.defaultLocale(supportedLocals.getFirst());
        GlobalTranslator.translator().addSource(this.translationStore);

        supportedLocals.forEach(locale -> this.translationStore.registerAll(
                locale,
                ResourceBundle.getBundle("stardust", locale),
                false));

        this.syncFrogService = new SyncFrogService(this);

        this.commandService = new PaperCommandService(this);
        this.commandService.registerCommands();

        this.luckPermsService = new LuckPermsService(this);
        this.luckPermsService.init();

        this.databaseService = new DatabaseConnectionService(getDataFolder().toPath().resolve("hibernate.cfg.xml"));
        this.cooldownService = new BukkitCommandCooldownService(this, this.databaseService);
        this.itemSignService = new BukkitItemSignService(this);

        this.userService = new UserService(this);

        this.playerService = new DefaultStardustPlayerService(this.databaseService.getSessionFactory().orElse(null));
        this.settingRegistry = new SettingRegistry();
        this.settingRegistry.registerDefaults();

        this.vanishService = loadVanishService(this.playerService);
        this.pluginTask = getServer().getScheduler().runTaskTimerAsynchronously(this, new StardustPluginTask(this), 20L, 20L);

        PacketEvents.getAPI().init();
        registerListeners();
    }

    @Override
    public void onDisable() {
        stopPluginTask();
        this.luckPermsService.unsubscribeEvents();
        this.databaseService.close();

        GlobalTranslator.translator().removeSource(this.translationStore);
        this.packetListener.unregister();
        PacketEvents.getAPI().terminate();
    }

    public PluginConfiguration getPluginConfiguration() {
        return pluginConfiguration;
    }

    public SyncFrogService getSyncFrogService() {
        return syncFrogService;
    }

    public PaperCommandService getCommandService() {
        return commandService;
    }

    public LuckPermsService getLuckPermsService() {
        return luckPermsService;
    }

    public CommandCooldownService getCooldownService() {
        return cooldownService;
    }

    public UserService getUserService() {
        return userService;
    }

    public DatabaseConnectionService getDatabaseService() {
        return databaseService;
    }

    public StardustPlayerService getPlayerService() {
        return playerService;
    }

    public PlayerVanishService getVanishService() {
        return vanishService;
    }

    public ItemSignService<ItemStack, Player> getItemSignService() {
        return itemSignService;
    }

    public Component getPrefix() {
        return Component.translatable("plugin.prefix");
    }

    public SettingRegistry getSettingRegistry() {
        return settingRegistry;
    }

    private void stopPluginTask() {
        if (this.pluginTask == null) return;
        this.pluginTask.cancel();
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new VanishSilentContainerFeature(this), this);
        getServer().getPluginManager().registerEvents(new CommandCooldownListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerVanishListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerAdvancementListener(this), this);
    }

    private PlayerVanishService loadVanishService(StardustPlayerService playerService) {
        if (Bukkit.getPluginManager().isPluginEnabled(PLUGIN_BLUEMAP_ENABLED)) {
            return new DelegatedBlueMapVanishService(new BukkitPlayerVanishService(playerService, this));
        } else {
            return new BukkitPlayerVanishService(playerService, this);
        }
    }

}
