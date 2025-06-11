package net.onelitefeather.stardust.command.commands;

import net.kyori.adventure.text.Component;
import net.onelitefeather.stardust.StardustPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;

public class GlowCommand {

    private final StardustPlugin plugin;

    public GlowCommand(StardustPlugin plugin) {
        this.plugin = plugin;
    }

    @Command("glow [player]")
    @Permission("stardust.command.glow")
    @CommandDescription("Makes a player glowing depending on his team color")
    public void handleCommand(CommandSender commandSender, @Greedy @Argument(value = "player") Player target) {
        if (target == null) {
            handleGlow(commandSender, (Player) commandSender);
        } else {
            handleGlow(commandSender, target);
        }
    }

    private void handleGlow(CommandSender commandSender, Player target) {
        if (!commandSender.equals(target) && !commandSender.hasPermission("stardust.command.glow.others")) {
            commandSender.sendMessage(
                    Component.translatable("plugin.not-enough-permissions")
                            .arguments(plugin.getPrefix())
            );
            return;
        }

        Component enabledMessage = Component.translatable("commands.glow.enabled")
                .arguments(plugin.getPrefix(), target.displayName());
        Component disabledMessage = Component.translatable("commands.glow.disabled")
                .arguments(plugin.getPrefix(), target.displayName());

        target.setGlowing(!target.isGlowing());
        commandSender.sendMessage(target.isGlowing() ? enabledMessage : disabledMessage);

    }
}
