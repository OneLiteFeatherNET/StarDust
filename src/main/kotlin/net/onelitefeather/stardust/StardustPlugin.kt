package net.onelitefeather.stardust

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.translation.GlobalTranslator
import net.kyori.adventure.translation.TranslationRegistry
import net.kyori.adventure.util.UTF8ResourceBundleControl
import net.onelitefeather.stardust.api.CommandCooldownService
import net.onelitefeather.stardust.api.ItemSignService
import net.onelitefeather.stardust.listener.*
import net.onelitefeather.stardust.service.*
import net.onelitefeather.stardust.translation.PluginTranslationRegistry
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import java.util.logging.Level

class StardustPlugin : JavaPlugin() {

    private val supportedLocals: Array<Locale> = arrayOf(Locale.US, Locale.GERMANY)

    //Services
    lateinit var databaseService: DatabaseService
    lateinit var userService: UserService
    lateinit var commandCooldownService: CommandCooldownService
    lateinit var luckPermsService: LuckPermsService
    lateinit var itemSignService: ItemSignService<ItemStack, Player>
    lateinit var syncFrogService: SyncFrogService
    lateinit var paperCommandService: PaperCommandService

    //Third parties plugins
    lateinit var packetListener: PacketListener

    //Config
    lateinit var itemSignMessage: String

    @Suppress("kotlin:S1874")
    override fun onEnable() {

        saveDefaultConfig()
        config.options().copyDefaults(true)
        saveConfig()

        try {
            val registry = TranslationRegistry.create(Key.key("stardust", "localization"))
            supportedLocals.forEach { locale ->
                val bundle = ResourceBundle.getBundle("stardust", locale, UTF8ResourceBundleControl.get())
                registry.registerAll(locale, bundle, false)
            }
            registry.defaultLocale(supportedLocals.first())
            GlobalTranslator.translator().addSource(PluginTranslationRegistry(registry))

            syncFrogService = SyncFrogService(this)
            itemSignService = BukkitItemSignService(this)

            luckPermsService = LuckPermsService(this)
            luckPermsService.init()

            databaseService = DatabaseService(this)
            commandCooldownService = BukkitCommandCooldownService(this)

            paperCommandService = PaperCommandService(this)
            paperCommandService.enable()

            initLuckPermsSupport()

            userService = UserService(this)
            userService.startUserTask()

            if (server.pluginManager.isPluginEnabled("ProtocolLib")) {
                packetListener = PacketListener(this)
                packetListener.register()
            }

            server.pluginManager.registerEvents(VanishSilentContainerFeature(this), this)
            server.pluginManager.registerEvents(CommandCooldownListener(this), this)
            server.pluginManager.registerEvents(PlayerChatListener(this), this)
            server.pluginManager.registerEvents(PlayerConnectionListener(this), this)
            server.pluginManager.registerEvents(PlayerVanishListener(this), this)
            server.pluginManager.registerEvents(PlayerAdvancementListener(this), this)
            itemSignMessage = config.getString("item-signing.message")!!
        } catch (e: Exception) {
            this.logger.log(Level.SEVERE, "Could not load plugin", e)
        }
    }

    override fun onDisable() {
        userService.stopUserTask()
        databaseService.shutdown()
        packetListener.unregister()
        luckPermsService.unsubscribeEvents()
    }

    /**
     * Enables luckperms support and dependency
     */
    private fun initLuckPermsSupport() {
        if (server.pluginManager.isPluginEnabled("LuckPerms")) {
            luckPermsService = LuckPermsService(this)
            luckPermsService.init()
        }
    }

    fun getPluginPrefix(): Component {
        return Component.translatable("plugin.prefix")
    }
}
