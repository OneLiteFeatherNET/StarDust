package net.onelitefeather.stardust.command.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.stardust.StardustPlugin;
import net.onelitefeather.stardust.api.user.IUser;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record VanishCommand(StardustPlugin stardustPlugin) {

    @CommandMethod("vanish|v [player]")
    @CommandPermission("featheressentials.command.vanish")
    @CommandDescription("Make a player invisible for other players")
    public void onCommand(@NotNull CommandSender commandSender, @Argument(value = "player", suggestions = "players") String targetName) {

        Player target = null;
        if (null != targetName) {
            target = commandSender.getServer().getPlayer(targetName);
        } else {
            if (commandSender instanceof Player player) {
                target = player;
            }
        }

        if(target == null) {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.stardustPlugin.getMessage("plugin.player-not-found", this.stardustPlugin.getPrefix(), targetName)));
            return;
        }

        if(!target.equals(commandSender)) {
            if (!commandSender.hasPermission("featheressentials.command.vanish.others")) {
                commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.stardustPlugin.getMessage("plugin.not-enough-permissions", this.stardustPlugin.getPrefix())));
                return;
            }
        }

        IUser user = this.stardustPlugin.getUserService().getUser(target.getUniqueId());
        if (user != null) {
            boolean state = user.toggleVanish();
            String enable = this.stardustPlugin.getMessage("commands.vanish.enable", this.stardustPlugin.getPrefix());
            String disable = this.stardustPlugin.getMessage("commands.vanish.disable", this.stardustPlugin.getPrefix());

            String displayName = this.stardustPlugin.getVaultHook().getPlayerDisplayName(target);
            String targetEnable = this.stardustPlugin.getMessage("commands.vanish.target.enable", this.stardustPlugin.getPrefix(), displayName);
            String targetDisable = this.stardustPlugin.getMessage("commands.vanish.target.disable", this.stardustPlugin.getPrefix(), displayName);

            if (!commandSender.equals(target)) {
                commandSender.sendMessage(MiniMessage.miniMessage().deserialize(state ? targetEnable : targetDisable));
            }

            target.sendMessage(MiniMessage.miniMessage().deserialize((state ? enable : disable)));
        }
    }

    @Suggestions(value = "players")
    public List<String> getPlayers(CommandContext<CommandSender> context, String input) {
        List<String> strings = this.stardustPlugin.getVisiblePlayers(context.getSender()).stream().map(HumanEntity::getName).toList();
        return StringUtil.copyPartialMatches(input, strings, new ArrayList<>(strings.size()));
    }
}
