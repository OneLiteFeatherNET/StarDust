package net.onelitefeather.stardust

import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.minecraft.extras.MinecraftHelp
import cloud.commandframework.paper.PaperCommandManager
import net.onelitefeather.stardust.extenstions.buildCommandSystem
import net.onelitefeather.stardust.extenstions.buildHelpSystem
import net.onelitefeather.stardust.extenstions.initLuckPermsSupport
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

    override fun onEnable() {

        saveDefaultConfig()

        i18nService = I18nService(this)
        databaseService = DatabaseService(
            config.getString("database.jdbcUrl", "Invalid url")!!,
            config.getString("database.username", "Unknown username")!!,
            config.getString("database.password", "IReallyKnowWhatIAmDoingISwear")!!
        )

        databaseService.init()
        commandCooldownService = CommandCooldownService(this)

        initLuckPermsSupport()
        buildCommandSystem()
        buildHelpSystem()

        userService = UserService(this)
        userService.startUserTask()

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
    }
}
