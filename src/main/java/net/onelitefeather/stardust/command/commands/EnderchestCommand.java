package net.onelitefeather.stardust.command.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.stardust.StardustPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public record EnderchestCommand(StardustPlugin stardustPlugin) {

    @CommandMethod("enderchest|ec [player]")
    @CommandPermission("featheressentials.command.enderchest")
    @CommandDescription("Open the Enderchest from another player")
    public void handleCommand(CommandSender commandSender, @Argument(value = "player", suggestions = "players") String targetName) {

        if (commandSender instanceof Player player) {

            if(null == targetName) {
                targetName = player.getName();
            }

            Player target = commandSender.getServer().getPlayer(targetName);
            if(target == null) {
                commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.stardustPlugin.getMessage("plugin.player-not-found", this.stardustPlugin.getPrefix(), targetName)));
                return;
            }

            if (!player.equals(target)) {
                if (!commandSender.hasPermission("featheressentials.command.enderchest.others")) {
                    commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.stardustPlugin.getMessage("plugin.not-enough-permissions", this.stardustPlugin.getPrefix())));
                    return;
                }
            }

            player.openInventory(target.getEnderChest());

        } else {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.stardustPlugin.getMessage("plugin.only-player-command")));
        }
    }

    @Suggestions(value = "players")
    public List<String> getPlayers(CommandContext<CommandSender> context, String input) {
        List<String> strings = this.stardustPlugin.getVisiblePlayers(context.getSender()).stream().map(HumanEntity::getName).toList();
        return StringUtil.copyPartialMatches(input, strings, new ArrayList<>(strings.size()));
    }

}
