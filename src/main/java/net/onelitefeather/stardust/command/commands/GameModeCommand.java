package net.onelitefeather.stardust.command.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.stardust.FeatherEssentials;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record GameModeCommand(FeatherEssentials featherEssentials) {

    @CommandMethod("gamemode|gm <gamemode> [player]")
    @CommandPermission("featheressentials.command.gamemode")
    @CommandDescription("Change the GameMode of a Player.")
    public void onCommand(@NotNull CommandSender commandSender, @Argument(value = "gamemode") GameMode gameMode, @Argument(value = "player", suggestions = "players") String targetName) {

        if (!commandSender.hasPermission("featheressentials.command.gamemode.*")) {
            if (!commandSender.hasPermission("featheressentials.command.gamemode." + gameMode.name().toLowerCase())) {
                commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.game-mode.no-permission", this.featherEssentials.getPrefix(), gameMode.name().toLowerCase())));
                return;
            }
        }

        TranslatableComponent gameModeName = Component.translatable(String.format("gameMode.%s", gameMode.name().toLowerCase()));

        Player target = null;
        if (null != targetName) {
            target = commandSender.getServer().getPlayer(targetName);
        } else {
            if (commandSender instanceof Player player) {
                target = player;
            }
        }

        if (target == null) {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("plugin.player-not-found", this.featherEssentials.getPrefix(), targetName)));
            return;
        }

        target.setGameMode(gameMode);
        if (target.equals(commandSender)) {
            commandSender.sendMessage(Component.translatable("commands.gamemode.success.self").args(gameModeName));
        } else {

            if (!commandSender.equals(target)) {
                if (!commandSender.hasPermission("featheressentials.command.gamemode.others")) {
                    commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("plugin.not-enough-permissions", this.featherEssentials.getPrefix())));
                    return;
                }

                target.sendMessage(Component.translatable("gameMode.changed").args(gameModeName));
            }

            commandSender.sendMessage(Component.translatable("commands.gamemode.success.other").args(this.featherEssentials.getVaultHook().getDisplayName(target), gameModeName));
        }
    }

    @Suggestions(value = "players")
    public List<String> getPlayers(CommandContext<CommandSender> context, String input) {
        List<String> strings = this.featherEssentials.getVisiblePlayers(context.getSender()).stream().map(HumanEntity::getName).toList();
        return StringUtil.copyPartialMatches(input, strings, new ArrayList<>(strings.size()));
    }
}

