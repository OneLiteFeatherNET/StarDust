package net.onelitefeather.stardust.command.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.stardust.FeatherEssentials;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record HealCommand(FeatherEssentials featherEssentials) {

    @CommandMethod("heal [player]")
    @CommandPermission("featheressentials.command.heal")
    @CommandDescription("Heal a Player.")
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
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("plugin.player-not-found")));
            return;
        }

        if(!target.equals(commandSender)) {
            if (!commandSender.hasPermission("featheressentials.command.heal.others")) {
                commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("plugin.not-enough-permissions", this.featherEssentials.getPrefix())));
                return;
            }
        }

        AttributeInstance healthAttribute = target.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttribute != null) {
            target.setHealth(healthAttribute.getValue());
        }

        target.setFireTicks(0);
        target.setVisualFire(false);
        target.setFoodLevel(20);
        target.setSaturation(20);

        if (!commandSender.equals(target)) {
            target.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.heal.target-success", this.featherEssentials.getPrefix(), target.getHealth())));
        }

        commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.heal.sender-success", this.featherEssentials.getPrefix(), this.featherEssentials.getVaultHook().getPlayerDisplayName(target), target.getHealth())));
    }

    @Suggestions(value = "players")
    public List<String> getPlayers(CommandContext<CommandSender> context, String input) {
        List<String> strings = this.featherEssentials.getVisiblePlayers(context.getSender()).stream().map(HumanEntity::getName).toList();
        return StringUtil.copyPartialMatches(input, strings, new ArrayList<>(strings.size()));
    }
}

