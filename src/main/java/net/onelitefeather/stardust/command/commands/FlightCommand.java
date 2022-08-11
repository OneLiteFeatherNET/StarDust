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
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record FlightCommand(FeatherEssentials featherEssentials) {
    @CommandMethod("flight|fly [player]")
    @CommandPermission("featheressentials.command.flight")
    @CommandDescription("Allows a Player to flight.")
    public void onCommand(@NotNull CommandSender commandSender, @Argument(value = "player", suggestions = "players") String targetName) {

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

        if (GameMode.CREATIVE == target.getGameMode() || GameMode.SPECTATOR == target.getGameMode()) {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.flight.already-flying", this.featherEssentials.getPrefix(), this.featherEssentials.getVaultHook().getPlayerDisplayName(target))));
            return;
        }

        IUser user = this.featherEssentials.getUserManager().getUser(target.getUniqueId());
        String displayName = this.featherEssentials.getVaultHook().getPlayerDisplayName(target);

        String enabled = this.featherEssentials.getMessage("commands.flight.enable", this.featherEssentials.getPrefix(), displayName);
        String disabled = this.featherEssentials.getMessage("commands.flight.disable", this.featherEssentials.getPrefix(), displayName);

        if (!commandSender.equals(target)) {
            if (!commandSender.hasPermission("featheressentials.command.flight.others")) {
                commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("plugin.not-enough-permissions", this.featherEssentials.getPrefix())));
                return;
            }
        }

        target.setAllowFlight(!target.getAllowFlight());
        user.setFlying(target.getAllowFlight());
        commandSender.sendMessage(MiniMessage.miniMessage().deserialize((target.getAllowFlight() ? enabled : disabled)));
        if (!commandSender.equals(target)) {
            target.sendMessage(MiniMessage.miniMessage().deserialize((target.getAllowFlight() ? enabled : disabled)));
        }
    }

    @Suggestions(value = "players")
    public List<String> getPlayers(CommandContext<CommandSender> context, String input) {
        List<String> strings = this.featherEssentials.getVisiblePlayers(context.getSender()).stream().map(HumanEntity::getName).toList();
        return StringUtil.copyPartialMatches(input, strings, new ArrayList<>(strings.size()));
    }
}

