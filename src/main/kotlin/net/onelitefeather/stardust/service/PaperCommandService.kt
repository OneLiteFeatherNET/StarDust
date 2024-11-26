package net.onelitefeather.stardust.service

import net.kyori.adventure.text.format.NamedTextColor
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.command.commands.*
import net.onelitefeather.stardust.command.mapper.BukkitSenderMapper
import org.bukkit.command.CommandSender
import org.incendo.cloud.annotation.specifier.Greedy
import org.incendo.cloud.annotations.*
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.minecraft.extras.AudienceProvider
import org.incendo.cloud.minecraft.extras.MinecraftHelp
import org.incendo.cloud.paper.PaperCommandManager


class PaperCommandService(private val plugin: StardustPlugin) {

    private lateinit var paperCommandManager: PaperCommandManager<CommandSender>
    private lateinit var annotationParser: AnnotationParser<CommandSender>
    private lateinit var minecraftHelp: MinecraftHelp<CommandSender>
    private val helpColors = MinecraftHelp.helpColors(
        NamedTextColor.GOLD,
        NamedTextColor.YELLOW,
        NamedTextColor.GOLD,
        NamedTextColor.GRAY,
        NamedTextColor.GOLD)

    fun enable() {
        paperCommandManager = buildCommandSystem()
        annotationParser = buildAnnotationParser(paperCommandManager)
        minecraftHelp = buildHelpSystem(paperCommandManager)
        registerCommands()
    }

    /**
     * Create the command system
     */
    private fun buildCommandSystem(): PaperCommandManager<CommandSender> {
       return PaperCommandManager.builder(BukkitSenderMapper())
           .executionCoordinator(ExecutionCoordinator.asyncCoordinator())
           .buildOnEnable(plugin)
    }

    private fun buildAnnotationParser(commandManager: PaperCommandManager<CommandSender>) =
        AnnotationParser(commandManager, CommandSender::class.java)

    /**
     * Creates the help system
     */
    private fun buildHelpSystem(commandManager: PaperCommandManager<CommandSender>) = MinecraftHelp.builder<CommandSender>()
        .commandManager(commandManager)
        .audienceProvider(AudienceProvider.nativeAudience())
        .commandPrefix("/stardust")
        .colors(helpColors)
        .build()

    /**
     * Register some commands from this plugin
     */
    private fun registerCommands() {
        annotationParser.parse(this)
        annotationParser.parse(FlightCommand(plugin))
        annotationParser.parse(GlowCommand(plugin))
        annotationParser.parse(GodmodeCommand(plugin))
        annotationParser.parse(HealCommand(plugin))
        annotationParser.parse(RenameCommand(plugin))
        annotationParser.parse(RepairCommand(plugin))
        annotationParser.parse(SignCommand(plugin))
        annotationParser.parse(SkullCommand(plugin))
        annotationParser.parse(VanishCommand(plugin))
        annotationParser.parse(plugin.syncFrogService)
    }

    @Command("stardust help [query]")
    @CommandDescription("Shows the help menu")
    @Permission("stardust.command.help")
    private fun helpCommand(sender: CommandSender, @Argument("query") @Greedy query: String?) {
        minecraftHelp.queryCommands(query ?: "", sender)
    }
}
