package net.onelitefeather.stardust

import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.arguments.parser.ParserParameters
import cloud.commandframework.arguments.parser.StandardParameters
import cloud.commandframework.bukkit.CloudBukkitCapabilities
import cloud.commandframework.execution.CommandExecutionCoordinator
import cloud.commandframework.meta.CommandMeta
import cloud.commandframework.minecraft.extras.MinecraftHelp
import cloud.commandframework.paper.PaperCommandManager
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.translation.GlobalTranslator
import net.kyori.adventure.translation.TranslationRegistry
import net.kyori.adventure.util.UTF8ResourceBundleControl
import net.onelitefeather.stardust.api.CommandCooldownService
import net.onelitefeather.stardust.api.ItemSignService
import net.onelitefeather.stardust.command.commands.*
import net.onelitefeather.stardust.listener.*
import net.onelitefeather.stardust.service.*
import net.onelitefeather.stardust.util.LynxWrapper
import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.MetadataValue
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import java.util.function.Function
import java.util.logging.Level


class StardustPlugin : JavaPlugin() {

    private val supportedLocals: Array<Locale> = arrayOf(Locale.US, Locale.GERMAN)

    private lateinit var paperCommandManager: PaperCommandManager<CommandSender>
    private lateinit var annotationParser: AnnotationParser<CommandSender>
    lateinit var minecraftHelp: MinecraftHelp<CommandSender>

    lateinit var i18nService: I18nService
    lateinit var databaseService: DatabaseService
    lateinit var userService: UserService
    lateinit var commandCooldownService: CommandCooldownService
    lateinit var luckPermsService: LuckPermsService
    lateinit var itemSignService: ItemSignService<ItemStack, Player>
    lateinit var packetListener: PacketListener
    lateinit var syncFrogService: SyncFrogService

    lateinit var chatConfirmationKey: NamespacedKey
    lateinit var signedNameSpacedKey: NamespacedKey

    lateinit var vanishedMetadata: MetadataValue
    lateinit var notVanishedMetadata: MetadataValue

    @Suppress("kotlin:S1874")
    override fun onEnable() {
        try {

            //Creating the default config
            saveDefaultConfig()

            //Saving the default config values
            config.options().copyDefaults(true)


            //Saving the config is needed
            saveConfig()

            val registry = TranslationRegistry.create(Key.key("stardust", "localization"))
            supportedLocals.forEach { locale ->
                val bundle = ResourceBundle.getBundle("stardust", locale, UTF8ResourceBundleControl.get())
                registry.registerAll(locale, bundle, false)
            }
            registry.defaultLocale(supportedLocals.first())
            GlobalTranslator.translator().addSource(LynxWrapper(registry))

            vanishedMetadata = FixedMetadataValue(this, true)
            notVanishedMetadata = FixedMetadataValue(this, false)

            syncFrogService = SyncFrogService(this)
            itemSignService = BukkitItemSignService(this)
            i18nService = I18nService(this)

            luckPermsService = LuckPermsService(this)
            luckPermsService.init()

            databaseService = DatabaseService(this)
            commandCooldownService = BukkitCommandCooldownService(this)

            initLuckPermsSupport()
            buildCommandSystem()
            buildHelpSystem()
            registerCommands()

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

            signedNameSpacedKey = NamespacedKey(this, "signed")
            chatConfirmationKey = NamespacedKey(this, "chat_confirmation")
        } catch (e: Exception) {
            this.logger.log(Level.SEVERE, "Could not load plugin", e)
        }
    }

    override fun onDisable() {

        if (this::userService.isInitialized) {
            userService.stopUserTask()
        }

        if (this::databaseService.isInitialized) {
            databaseService.shutdown()
        }

        if (this::packetListener.isInitialized) {
            packetListener.unregister()
        }
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

    /**
     * Register some commands from this plugin
     */
    private fun registerCommands() {
        annotationParser.parse(FlightCommand(this))
        annotationParser.parse(GameModeCommand(this))
        annotationParser.parse(GlowCommand(this))
        annotationParser.parse(GodmodeCommand(this))
        annotationParser.parse(HealCommand(this))
        annotationParser.parse(HelpCommand(this))
        annotationParser.parse(RenameCommand(this))
        annotationParser.parse(RepairCommand(this))
        annotationParser.parse(SignCommand(this))
        annotationParser.parse(SkullCommand(this))
        annotationParser.parse(VanishCommand(this))
        annotationParser.parse(syncFrogService)
    }

    /**
     * Create the command system
     */
    private fun buildCommandSystem() {
        try {
            paperCommandManager = PaperCommandManager(
                    this,
                    CommandExecutionCoordinator.simpleCoordinator(),
                    Function.identity(),
                    Function.identity()
            )
        } catch (e: Exception) {
            logger.log(Level.WARNING, "Failed to build command system", e)
            server.pluginManager.disablePlugin(this)
            return
        }

        if (paperCommandManager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            paperCommandManager.registerBrigadier()
            logger.info("Brigadier support enabled")
        }

        if (paperCommandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            paperCommandManager.registerAsynchronousCompletions()
            logger.info("Asynchronous completions enabled")
        }


        val commandMetaFunction =
                Function<ParserParameters, CommandMeta> { p: ParserParameters ->
                    CommandMeta.simple().with(
                            CommandMeta.DESCRIPTION,
                            p.get(StandardParameters.DESCRIPTION, "No description")
                    ).build()
                }

        annotationParser = AnnotationParser(
                paperCommandManager,
                CommandSender::class.java, commandMetaFunction
        )
    }

    /**
     * Creates the help system
     */
    private fun buildHelpSystem() {
        minecraftHelp = MinecraftHelp.createNative(
                "/stardust help",
                paperCommandManager
        )

        minecraftHelp.helpColors = MinecraftHelp.HelpColors.of(
                NamedTextColor.GOLD,
                NamedTextColor.YELLOW,
                NamedTextColor.GOLD,
                NamedTextColor.GRAY,
                NamedTextColor.GOLD
        )
    }

    fun getPluginPrefix(): String {
        return "<lang:plugin.prefix:${this.name}>"
    }
}
