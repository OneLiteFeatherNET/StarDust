package net.onelitefeather.stardust.command.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.stardust.FeatherEssentials;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record TpCommand(FeatherEssentials featherEssentials) {

    @CommandMethod("tphere <player>")
    @CommandPermission("featheressentials.command.tphere")
    @CommandDescription("Teleport a player to your location")
    public void onTpHere(@NotNull CommandSender commandSender, @Argument(value = "player", suggestions = "players") String targetName) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("plugin.only-player-command")));
            return;
        }

        Player target = commandSender.getServer().getPlayer(targetName);
        if (target == null) {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("plugin.player-not-found", this.featherEssentials.getPrefix(), targetName)));
            return;
        }
        target.teleport(player);
    }

    @CommandMethod("tpa <player>")
    @CommandPermission("featheressentials.command.tpa")
    @CommandDescription("Ask a player to teleport to his/her location")
    public void onTpa(@NotNull CommandSender commandSender, @Argument(value = "player", suggestions = "players") String targetName) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("plugin.only-player-command")));
            return;
        }

        Player target = commandSender.getServer().getPlayer(targetName);
        if (target == null) {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("plugin.player-not-found", this.featherEssentials.getPrefix(), targetName)));
            return;
        }

        Component accept = MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.teleport.tpa.accept"));
        Component deny = MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.teleport.tpa.deny"));

        TextComponent message = (TextComponent) MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.teleport.tpa", this.featherEssentials.getPrefix(), player.getName()));
        target.sendMessage(message.append(accept)
                .clickEvent(ClickEvent.runCommand("tpa accept " + player.getName())).hoverEvent(HoverEvent.showText(accept))
                .append(deny)
                .clickEvent(ClickEvent.runCommand("tpa deny " + player.getName())).hoverEvent(HoverEvent.showText(deny)));

    }

    @CommandMethod("tpa accept <player>")
    @CommandPermission("featheressentials.command.tpa")
    @CommandDescription("Ask a player to teleport to his/her location")
    public void onTpaAccept(@NotNull CommandSender commandSender, @Argument(value = "player", suggestions = "players") String targetName) {
        System.out.println("Accept");

    }

    @CommandMethod("tpa deny <player>")
    @CommandPermission("featheressentials.command.tpa")
    @CommandDescription("Ask a player to teleport to his/her location")
    public void onTpaDeny(@NotNull CommandSender commandSender, @Argument(value = "player", suggestions = "players") String targetName) {
        System.out.println("Deny");

    }

    @Suggestions(value = "players")
    public List<String> getPlayers(CommandContext<CommandSender> context, String input) {
        List<String> strings = this.featherEssentials.getVisiblePlayers(context.getSender()).stream().map(HumanEntity::getName).toList();
        return StringUtil.copyPartialMatches(input, strings, new ArrayList<>(strings.size()));
    }

}
