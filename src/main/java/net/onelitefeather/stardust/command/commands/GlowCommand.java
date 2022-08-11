package net.onelitefeather.stardust.command.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.stardust.FeatherEssentials;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record GlowCommand(FeatherEssentials featherEssentials) {

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
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("plugin.player-not-found", this.featherEssentials.getPrefix(), targetName)));
            return;
        }

        target.setGlowing(!target.isGlowing());
        String enabled = this.featherEssentials.getMessage("commands.glow.enabled", this.featherEssentials.getPrefix(), this.featherEssentials.getVaultHook().getPlayerDisplayName(target));
        String disabled = this.featherEssentials.getMessage("commands.glow.disabled", this.featherEssentials.getPrefix(), this.featherEssentials.getVaultHook().getPlayerDisplayName(target));

        if (!commandSender.equals(target)) {

            if (!commandSender.hasPermission("featheressentials.command.glow.others")) {
                commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("plugin.not-enough-permissions", this.featherEssentials.getPrefix())));
                return;
            }

            commandSender.sendMessage(MiniMessage.miniMessage().deserialize((target.isGlowing() ? enabled : disabled)));
        }

        commandSender.sendMessage(MiniMessage.miniMessage().deserialize((target.isGlowing() ? enabled : disabled)));
    }

    @Suggestions(value = "players")
    public List<String> getPlayers(CommandContext<CommandSender> context, String input) {
        List<String> strings = this.featherEssentials.getVisiblePlayers(context.getSender()).stream().map(HumanEntity::getName).toList();
        return StringUtil.copyPartialMatches(input, strings, new ArrayList<>(strings.size()));
    }


}
