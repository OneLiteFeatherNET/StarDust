package net.onelitefeather.stardust.service;

import net.kyori.adventure.text.format.NamedTextColor;
import net.onelitefeather.stardust.StardustPlugin;
import net.onelitefeather.stardust.command.commands.*;
import net.onelitefeather.stardust.command.mapper.BukkitSenderMapper;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.*;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.minecraft.extras.AudienceProvider;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.paper.PaperCommandManager;

public class PaperCommandService {

    private final StardustPlugin plugin;
    private final PaperCommandManager<CommandSender> paperCommandManager;
    private final AnnotationParser<CommandSender> annotationParser;
    private final MinecraftHelp<CommandSender> minecraftHelp;
    private final MinecraftHelp.HelpColors helpColors = MinecraftHelp.helpColors(
            NamedTextColor.GOLD,
            NamedTextColor.YELLOW,
            NamedTextColor.GOLD,
            NamedTextColor.GRAY,
            NamedTextColor.GOLD
    );

    public PaperCommandService(StardustPlugin plugin) {
        this.plugin = plugin;
        this.paperCommandManager = buildCommandSystem();
        this.annotationParser = buildAnnotationParser(paperCommandManager);
        this.minecraftHelp = buildHelpSystem(paperCommandManager);
    }

    @SuppressWarnings("UnstableApiUsage")
    private PaperCommandManager<CommandSender> buildCommandSystem() {
        return PaperCommandManager.builder(new BukkitSenderMapper())
                .executionCoordinator(ExecutionCoordinator.asyncCoordinator())
                .buildOnEnable(plugin);
    }

    private AnnotationParser<CommandSender> buildAnnotationParser(PaperCommandManager<CommandSender> commandManager) {
        return new AnnotationParser<>(commandManager, CommandSender.class);
    }

    private MinecraftHelp<CommandSender> buildHelpSystem(PaperCommandManager<CommandSender> commandManager) {
        return MinecraftHelp.<CommandSender>builder()
                .commandManager(commandManager)
                .audienceProvider(AudienceProvider.nativeAudience())
                .commandPrefix("/stardust help")
                .colors(helpColors)
                .build();
    }

    public void registerCommands() {
        annotationParser.parse(this);
        annotationParser.parse(new FlightCommand(plugin));
        annotationParser.parse(new GlowCommand(plugin));
        annotationParser.parse(new GodmodeCommand(plugin));
        annotationParser.parse(new HealCommand(plugin));
        annotationParser.parse(new RenameCommand(plugin));
        annotationParser.parse(new IPSameCommand(plugin));
        annotationParser.parse(new RepairCommand(plugin));
        annotationParser.parse(new SignCommand(plugin));
        annotationParser.parse(new SkullCommand(plugin));
        annotationParser.parse(new VanishCommand(plugin));
        annotationParser.parse(plugin.getSyncFrogService());
        annotationParser.parse(new InvSeeCommand());
        annotationParser.parse(new VPNCheckCommand(plugin));
        annotationParser.parse(new PingCommand(plugin));
        annotationParser.parse(new IpShowCommand(plugin));
    }

    @Command("stardust help [query]")
    @CommandDescription("Shows the help menu")
    @Permission("stardust.command.help")
    private void helpCommand(CommandSender sender, @Argument("query") @Greedy String query) {
        minecraftHelp.queryCommands(query != null ? query : "", sender);
    }
}
