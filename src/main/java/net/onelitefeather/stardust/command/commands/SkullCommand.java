package net.onelitefeather.stardust.command.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.stardust.FeatherEssentials;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public record SkullCommand(FeatherEssentials featherEssentials) {

    @CommandMethod("skull <name>")
    @CommandPermission("featheressentials.command.skull")
    public void onCommand(@NotNull CommandSender commandSender, @Argument(value = "name") String name) {

        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("plugin.only-player-command")));
            return;
        }

        /*ItemStack skull = new SkullBuilder().owner(name).build();
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(skull);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), skull);
        }*/

        commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.skull.success", this.featherEssentials.getPrefix(), name)));
    }
}
