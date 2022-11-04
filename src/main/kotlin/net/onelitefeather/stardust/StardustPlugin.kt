package net.onelitefeather.stardust

import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.minecraft.extras.MinecraftHelp
import cloud.commandframework.paper.PaperCommandManager
import net.onelitefeather.stardust.api.CommandCooldownService
import net.onelitefeather.stardust.extenstions.buildCommandSystem
import net.onelitefeather.stardust.extenstions.buildHelpSystem
import net.onelitefeather.stardust.extenstions.initLuckPermsSupport
import net.onelitefeather.stardust.extenstions.registerCommands
import net.onelitefeather.stardust.listener.*
import net.onelitefeather.stardust.service.*
import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class StardustPlugin : JavaPlugin() {

    lateinit var paperCommandManager: PaperCommandManager<CommandSender>
    lateinit var annotationParser: AnnotationParser<CommandSender>
    lateinit var minecraftHelp: MinecraftHelp<CommandSender>

    lateinit var i18nService: I18nService
    lateinit var signedNameSpacedKey: NamespacedKey
    lateinit var databaseService: DatabaseService
    lateinit var userService: UserService
    lateinit var commandCooldownService: CommandCooldownService
    lateinit var luckPermsService: LuckPermsService

    lateinit var packetListener: PacketListener

    override fun onEnable() {

        saveDefaultConfig()
        signedNameSpacedKey = NamespacedKey(this, "signed")


        luckPermsService = LuckPermsService(this)

        i18nService = I18nService(this)

        val jdbcUrl = config.getString("database.jdbcUrl")
        val databaseDriver = config.getString("database.driver")
        val username = config.getString("database.username")
        val password = config.getString("database.password") ?: "IReallyKnowWhatIAmDoingISwear"

        if(jdbcUrl != null && databaseDriver != null && username != null) {
            databaseService = DatabaseService(jdbcUrl, username, password, databaseDriver)
            databaseService.init()
            commandCooldownService = BukkitCommandCooldownService(this)
        }

        initLuckPermsSupport()
        buildCommandSystem()
        buildHelpSystem()
        registerCommands()

        userService = UserService(this)
        userService.startUserTask()

        if(server.pluginManager.isPluginEnabled("ProtocolLib")) {
            packetListener = PacketListener(this)
            packetListener.register()
        }

        server.pluginManager.registerEvents(CommandCooldownListener(this), this)
        server.pluginManager.registerEvents(InventoryClickListener(this), this)
        server.pluginManager.registerEvents(PlayerChatListener(this), this)
        server.pluginManager.registerEvents(PlayerConnectionListener(this), this)
        server.pluginManager.registerEvents(PlayerVanishListener(this), this)

        signedNameSpacedKey = NamespacedKey(this, "signed")
    }

    override fun onDisable() {

        if(this::userService.isInitialized) {
            userService.stopUserTask()
        }

        if (this::databaseService.isInitialized) {
            databaseService.shutdown()
        }

        if (this::packetListener.isInitialized) {
            packetListener.unregister()
        }
    }
}
