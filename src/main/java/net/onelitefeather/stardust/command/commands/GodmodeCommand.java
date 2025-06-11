package net.onelitefeather.stardust.command.commands;

import net.kyori.adventure.text.Component;
import net.onelitefeather.stardust.StardustPlugin;
import net.onelitefeather.stardust.util.Constants;
import net.onelitefeather.stardust.util.PlayerUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;

public class GodmodeCommand {

    private final StardustPlugin plugin;

    public GodmodeCommand(StardustPlugin plugin) {
        this.plugin = plugin;
    }

    @Command("godmode [player]")
    @Permission("stardust.command.godmode")
    @CommandDescription("Makes a player invulnerable to everything")
    public void handleCommand(CommandSender commandSender, @Greedy @Argument(value = "player") Player target) {
        Player actualTarget = target != null ? target : (Player) commandSender;
        handleInvulnerability(commandSender, actualTarget);
    }

    private void handleInvulnerability(CommandSender commandSender, Player target) {
        if (!commandSender.equals(target) && !commandSender.hasPermission("stardust.command.godmode.others")) {
            commandSender.sendMessage(
                    Component.translatable("plugin.not-enough-permissions")
                            .arguments(plugin.getPrefix())
            );
            return;
        }

        target.setInvulnerable(!target.isInvulnerable());
        PlayerUtil.removeEnemies(target, Constants.RADIUS_REMOVE_ENEMIES);

        Component targetEnabledMessage = Component.translatable("commands.godmode.enable.target")
                .arguments(plugin.getPrefix());
        Component targetDisabledMessage = Component.translatable("commands.godmode.disable.target")
                .arguments(plugin.getPrefix());

        Component enabledMessage = Component.translatable("commands.godmode.enable")
                .arguments(plugin.getPrefix(), target.displayName());
        Component disabledMessage = Component.translatable("commands.godmode.disable")
                .arguments(plugin.getPrefix(), target.displayName());

        if (commandSender.equals(target)) {
            target.sendMessage(target.isInvulnerable() ? targetEnabledMessage : targetDisabledMessage);
        } else {
            commandSender.sendMessage(target.isInvulnerable() ? enabledMessage : disabledMessage);
            target.sendMessage(target.isInvulnerable() ? targetEnabledMessage : targetDisabledMessage);
        }
    }
}
