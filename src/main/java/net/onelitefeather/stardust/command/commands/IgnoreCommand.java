package net.onelitefeather.stardust.command.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.stardust.FeatherEssentials;
import net.onelitefeather.stardust.api.user.IUser;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record IgnoreCommand(FeatherEssentials featherEssentials) {

    @CommandMethod("ignore <player>")
    @CommandPermission("featheressentials.command.ignore")
    @CommandDescription("Ignore a Player.")
    public void onCommand(@NotNull CommandSender commandSender, @Argument(value = "player", suggestions = "players") String targetName) {

        Player target = commandSender.getServer().getPlayer(targetName);
        if(target == null) {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("plugin.player-not-found", this.featherEssentials.getPrefix(), targetName)));
            return;
        }

        IUser user = this.featherEssentials.getUserManager().getUser(target.getUniqueId());

        if (targetName.equalsIgnoreCase(commandSender.getName())) {
            commandSender.sendMessage(this.featherEssentials.getMessage("commands.ignore.cannot-ignore-self", this.featherEssentials.getPrefix()));
            return;
        }

        UUID uuid = commandSender.getServer().getPlayerUniqueId(targetName);
        if (uuid == null) {
            commandSender.sendMessage(this.featherEssentials.getMessage("plugin.unknown-player", this.featherEssentials.getPrefix(), targetName));
            return;
        }

        IUser targetUser = this.featherEssentials.getUserManager().getUser(uuid);
        if (targetUser == null) {
            commandSender.sendMessage(this.featherEssentials.getMessage("plugin.unknown-player", this.featherEssentials.getPrefix(), targetName));
            return;
        }

        if (user.isIgnoring(targetUser.getUniqueId())) {
            user.unIgnorePlayer(targetUser.getUniqueId());
        } else {
            user.ignorePlayer(targetUser.getUniqueId());
        }

        commandSender.sendMessage((user.isIgnoring(targetUser.getUniqueId()) ? "Ignoring" : "Unignoring") + " player " + targetUser.getName());
    }

    @Suggestions(value = "players")
    public List<String> getPlayers(CommandContext<CommandSender> context, String input) {
        List<String> strings = this.featherEssentials.getVisiblePlayers(context.getSender()).stream().map(HumanEntity::getName).toList();
        return StringUtil.copyPartialMatches(input, strings, new ArrayList<>(strings.size()));
    }


}
