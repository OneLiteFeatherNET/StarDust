package net.onelitefeather.stardust.command.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Quoted;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.stardust.StardustPlugin;
import net.onelitefeather.stardust.manager.SignManager;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record SignCommand(StardustPlugin stardustPlugin) {

    @CommandMethod("sign <text>")
    @CommandPermission("featheressentials.command.sign")
    @CommandDescription("Sign the Item in your Hand.")
    public void onCommand(@NotNull CommandSender commandSender, @Argument(value = "text") @Quoted String text) {

        if(commandSender instanceof Player player) {

            ItemStack itemStack = player.getInventory().getItemInMainHand();
            if (itemStack.getType() == Material.AIR) {
                commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.stardustPlugin.getMessage("commands.sign.no-item-in-hand", this.stardustPlugin.getPrefix())));
                return;
            }

            SignManager signManager = new SignManager(this.stardustPlugin, itemStack);
            if (signManager.isSigned() && !commandSender.hasPermission("featheressentials.command.sign.override")) {
                commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.stardustPlugin.getMessage("commands.sign.already-signed", this.stardustPlugin.getPrefix())));
                return;
            }

            if (player.getInventory().firstEmpty() == -1) {
                commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.stardustPlugin.getMessage("plugin.inventory-full", this.stardustPlugin.getPrefix())));
                return;
            }

            boolean hasCreativeMode = player.getGameMode() == GameMode.CREATIVE;
            if (!hasCreativeMode) {
                if (itemStack.getAmount() > 1) {
                    itemStack.setAmount(itemStack.getAmount() - 1);
                } else {
                    player.getInventory().remove(itemStack);
                }

                signManager.setItemStack(itemStack.asOne());
                player.getInventory().addItem(signManager.sign(player.getUniqueId(), player.getName(), ChatColor.translateAlternateColorCodes('&', text)));

            } else {
                player.getInventory().setItemInMainHand(signManager.sign(player.getUniqueId(), player.getName(), ChatColor.translateAlternateColorCodes('&', text)));
            }

            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.stardustPlugin.getMessage("commands.sign.signed", this.stardustPlugin.getPrefix())));
        }
    }
}
