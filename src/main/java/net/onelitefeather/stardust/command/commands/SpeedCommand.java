package net.onelitefeather.stardust.command.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Greedy;
import cloud.commandframework.annotations.specifier.Range;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.stardust.StardustPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public record SpeedCommand(StardustPlugin stardustPlugin) {

    @CommandMethod("speed [speed]")
    @CommandPermission("featheressentials.command.speed")
    @CommandDescription("Increase or decrease your walk/fly speed.")
    public void onCommand(@NotNull CommandSender commandSender, @Greedy @Range(min = "1", max = "10") @Argument(value = "speed") Float speedInput) {

        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.stardustPlugin.getMessage("plugin.only-player-command")));
            return;
        }

        boolean onGround = !player.isFlying();
        float speed = speedInput != null ? speedInput / 10 : onGround ? 0.2F : 0.1F;

        if (speed > 1.0F) {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.stardustPlugin.getMessage("commands.speed.value-to-high", this.stardustPlugin.getPrefix())));
            return;
        }

        if (onGround) {
            player.setWalkSpeed(speed);
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.stardustPlugin.getMessage("commands.speed.walk-speed", this.stardustPlugin.getPrefix(), speed)));
        } else {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.stardustPlugin.getMessage("commands.speed.fly-speed", this.stardustPlugin.getPrefix(), speed)));
            player.setFlySpeed(speed);
        }
    }
}
