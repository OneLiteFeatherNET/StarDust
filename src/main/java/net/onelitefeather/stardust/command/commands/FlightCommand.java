package net.onelitefeather.stardust.command.commands;

import net.kyori.adventure.text.Component;
import net.onelitefeather.stardust.StardustPlugin;
import net.onelitefeather.stardust.user.UserPropertyType;
import net.onelitefeather.stardust.util.PlayerUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;

public class FlightCommand {

    private final StardustPlugin plugin;

    public FlightCommand(StardustPlugin plugin) {
        this.plugin = plugin;
    }

    @Command("flight|fly [player]")
    @Permission("stardust.command.flight")
    @CommandDescription("Allows a player to flight.")
    public void handleFlightCommand(CommandSender sender, @Greedy @Argument(value = "player")Player target) {

        if (sender instanceof Player player && target == null) {
            handleFlight(sender, player);
            return;
        }

        if (target != null) {
            handleFlight(sender, target);
        }
    }

    private void handleFlight(CommandSender commandSender, Player target) {
        var user = this.plugin.getUserService().getUser(target.getUniqueId());
        if (commandSender != target && !commandSender.hasPermission("stardust.command.flight.others")) {
            commandSender.sendMessage(Component.translatable("plugin.not-enough-permissions").arguments(plugin.getPrefix()));
            return;
        }

        if (PlayerUtil.canEnterFlyMode(target)) {
            commandSender.sendMessage(
                    Component.translatable("already-in-flight-mode").arguments(
                            plugin.getPrefix(),
                            target.displayName()));
            return;
        }

        target.setAllowFlight(!target.getAllowFlight());
        this.plugin.getUserService().setUserProperty(user, UserPropertyType.FLYING, target.getAllowFlight());

        var targetEnabledMessage = Component.translatable("commands.flight.target.enable").arguments(this.plugin.getPrefix());
        var targetDisabledMessage = Component.translatable("commands.flight.target.disable").arguments(this.plugin.getPrefix());

        var enabledMessage = Component.translatable("commands.flight.enable")
                .arguments(plugin.getPrefix(), target.displayName());

        var disabledMessage = Component.translatable("commands.flight.disable")
                .arguments(plugin.getPrefix(), target.displayName());

        if(commandSender == target) {
            target.sendMessage(target.getAllowFlight() ? targetEnabledMessage : targetDisabledMessage);
        } else {
            commandSender.sendMessage(target.getAllowFlight() ? enabledMessage : disabledMessage);
            target.sendMessage(target.getAllowFlight() ? targetEnabledMessage : targetDisabledMessage);
        }
    }
}
