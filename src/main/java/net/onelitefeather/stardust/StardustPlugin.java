package net.onelitefeather.stardust;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import net.onelitefeather.stardust.api.CommandCooldownService;
import net.onelitefeather.stardust.api.ItemSignService;
import net.onelitefeather.stardust.configuration.PluginConfiguration;
import net.onelitefeather.stardust.listener.*;
import net.onelitefeather.stardust.service.*;
import net.onelitefeather.stardust.translation.PluginTranslationRegistry;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class StardustPlugin extends JavaPlugin {

    private final List<Locale> supportedLocals = List.of(Locale.US, Locale.GERMANY);

    private SyncFrogService syncFrogService;
    private DatabaseConnectionService databaseService;
    private UserService userService;
    private CommandCooldownService cooldownService;
    private LuckPermsService luckPermsService;
    private PaperCommandService commandService;

    private ItemSignService<ItemStack, Player> itemSignService;
    private PacketListener packetListener;
    private PluginConfiguration pluginConfiguration;

    @Override
    public void onEnable() {

        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        this.pluginConfiguration = new PluginConfiguration(getConfig());

        var registry = TranslationRegistry.create(Key.key("stardust", "localization"));
        supportedLocals.forEach(locale -> {
            var bundle = ResourceBundle.getBundle("stardust", locale, UTF8ResourceBundleControl.get());
            registry.registerAll(locale, bundle, false);
        });

        registry.defaultLocale(supportedLocals.getFirst());
        GlobalTranslator.translator().addSource(new PluginTranslationRegistry(registry));

        this.syncFrogService = new SyncFrogService(this);

        this.commandService = new PaperCommandService(this);
        this.commandService.registerCommands();

        this.luckPermsService = new LuckPermsService(this);
        this.luckPermsService.init();

        this.databaseService = new DatabaseConnectionService(this);
        this.cooldownService = new BukkitCommandCooldownService(this, this.databaseService);

        this.itemSignService = new BukkitItemSignService(this);
        this.userService = new UserService(this);

        if (getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            packetListener = new PacketListener(this);
            packetListener.register();
        }

        registerListeners();
    }

    @Override
    public void onDisable() {
        if (this.packetListener != null) this.packetListener.unregister();
        this.userService.stopUserTask();
        this.luckPermsService.unsubscribeEvents();
        this.databaseService.close();
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

    public ItemSignService<ItemStack, Player> getItemSignService() {
        return itemSignService;
    }

    public Component getPrefix() {
        return Component.translatable("plugin.prefix");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new VanishSilentContainerFeature(this), this);
        getServer().getPluginManager().registerEvents(new CommandCooldownListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerVanishListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerAdvancementListener(this), this);
    }

}
