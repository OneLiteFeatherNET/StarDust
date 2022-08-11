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
import net.onelitefeather.stardust.util.Constants;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public record UserCommand(FeatherEssentials featherEssentials) {

    @CommandMethod("user info [player]")
    @CommandPermission("featheressentials.command.user.info")
    @CommandDescription("Get informations about a User")
    public void infoCommand(@NotNull CommandSender commandSender, @Argument(value = "player", suggestions = "user_names") String playerName) {

        UUID uuid = null;
        if (null != playerName) {
            uuid = commandSender.getServer().getPlayerUniqueId(playerName);
        } else {
            if (commandSender instanceof Player player) {
                uuid = player.getUniqueId();
                playerName = player.getName();
            }
        }

        if (uuid == null) {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("plugin.unknown-player", this.featherEssentials.getPrefix(), playerName)));
            return;
        }

        if (!commandSender.getName().equalsIgnoreCase(playerName)) {
            if (!commandSender.hasPermission("featheressentials.command.user.info.others")) {
                commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("plugin.not-enough-permissions", this.featherEssentials.getPrefix())));
                return;
            }
        }

        IUser user = this.featherEssentials.getUserManager().getFromDatabase(uuid);
        if (user != null) {

            String enabled = this.featherEssentials.getRawMessage("plugin.boolean-yes");
            String disabled = this.featherEssentials.getRawMessage("plugin.boolean-no");

            String prefix = this.featherEssentials.getPrefix();

            OfflinePlayer offlinePlayer = this.featherEssentials.getServer().getOfflinePlayer(uuid);
            Player player = null;

            if (offlinePlayer.isOnline()) {
                player = offlinePlayer.getPlayer();
            }

            boolean online = player != null;
            boolean invulnerable = online && player.isInvulnerable();

            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.user.info.uuid", prefix, user.getUniqueId().toString())));
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.user.info.name", prefix, user.getName())));

            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.user.info.flying", prefix, (user.isFlying() ? enabled : disabled))));
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.user.info.vanished", prefix, (user.isVanished() ? enabled : disabled))));
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.user.info.invulnerable", prefix, (invulnerable ? enabled : disabled))));
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.user.info.online", prefix, (online ? enabled : disabled))));

            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.user.info.firstJoin", prefix, Constants.DATE_FORMAT.format(new Date(user.getFirstJoin())))));
            if (player != null) {
                commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.user.info.display-name", prefix, this.featherEssentials.getVaultHook().getPlayerDisplayName(player))));
            } else {
                commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.user.info.lastSeen", prefix, this.featherEssentials.getRemainingTime(user.getLastSeen()))));
            }
        }
    }

    @CommandMethod("user delete <playername>")
    @CommandDescription("Delete a User from the database")
    @CommandPermission("featheressentials.command.user.delete")
    public void deleteUser(CommandSender commandSender, @Argument(value = "playername", suggestions = "user_names") String playerName) {

        UUID uuid = commandSender.getServer().getPlayerUniqueId(playerName);
        if (uuid == null) {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("plugin.unknown-player", this.featherEssentials.getPrefix(), playerName)));
            return;
        }

        IUser user = this.featherEssentials.getUserManager().getFromDatabase(uuid);
        if (user != null) {
            this.featherEssentials.getUserManager().deleteUser(user.getUniqueId(), success -> {
                if (success) {
                    if (user.kick(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.user.delete.kick-message")))) {
                        commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.user.delete.success", this.featherEssentials.getPrefix(), playerName)));
                    }
                } else {
                    commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.user.delete.failure", this.featherEssentials.getPrefix(), playerName)));
                }

            });
        } else {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("plugin.unknown-player", this.featherEssentials.getPrefix(), playerName)));
        }
    }

    @Suggestions(value = "user_names")
    public List<String> getUserNames(CommandContext<CommandSender> context, String input) {
        List<String> strings = this.featherEssentials.getUserManager().getUsers().stream().map(IUser::getName).toList();
        return StringUtil.copyPartialMatches(input, strings, new ArrayList<>(strings.size()));
    }
}

