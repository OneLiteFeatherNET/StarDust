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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record GlowCommand(StardustPlugin stardustPlugin) {

    @CommandMethod("glow [player]")
    @CommandPermission("featheressentials.command.glow")
    @CommandDescription("Makes a Player glowing in his scoreboard team color.")
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

        target.setGlowing(!target.isGlowing());
        String enabled = this.stardustPlugin.getMessage("commands.glow.enabled", this.stardustPlugin.getPrefix(), this.stardustPlugin.getVaultHook().getPlayerDisplayName(target));
        String disabled = this.stardustPlugin.getMessage("commands.glow.disabled", this.stardustPlugin.getPrefix(), this.stardustPlugin.getVaultHook().getPlayerDisplayName(target));

        if (!commandSender.equals(target)) {

            if (!commandSender.hasPermission("featheressentials.command.glow.others")) {
                commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.stardustPlugin.getMessage("plugin.not-enough-permissions", this.stardustPlugin.getPrefix())));
                return;
            }

            commandSender.sendMessage(MiniMessage.miniMessage().deserialize((target.isGlowing() ? enabled : disabled)));
        }

        commandSender.sendMessage(MiniMessage.miniMessage().deserialize((target.isGlowing() ? enabled : disabled)));
    }

    @Suggestions(value = "players")
    public List<String> getPlayers(CommandContext<CommandSender> context, String input) {
        List<String> strings = this.stardustPlugin.getVisiblePlayers(context.getSender()).stream().map(HumanEntity::getName).toList();
        return StringUtil.copyPartialMatches(input, strings, new ArrayList<>(strings.size()));
    }


}
