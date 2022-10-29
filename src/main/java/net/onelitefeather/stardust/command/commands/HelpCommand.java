package net.onelitefeather.stardust.command.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Greedy;
import net.onelitefeather.stardust.StardustPlugin;
import org.bukkit.command.CommandSender;

public record HelpCommand(StardustPlugin stardustPlugin) {

    @CommandDescription("Shows the help menu")
    @CommandMethod("featheressentials help [query]")
    @CommandPermission("featheressentials.command.help")
    private void helpCommand(CommandSender sender, final @Argument("query") @Greedy String query) {
        this.stardustPlugin.getMinecraftHelp().queryCommands(query == null ? "" : query, sender);
    }


}
