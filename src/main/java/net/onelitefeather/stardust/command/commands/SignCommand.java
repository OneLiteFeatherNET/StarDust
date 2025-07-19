package net.onelitefeather.stardust.command.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.onelitefeather.stardust.StardustPlugin;
import net.onelitefeather.stardust.util.Constants;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.incendo.cloud.annotation.specifier.Quoted;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;

import java.util.List;

public class SignCommand {

    private final StardustPlugin plugin;

    public SignCommand(StardustPlugin plugin) {
        this.plugin = plugin;
    }

    @Command("unsign")
    @Permission("stardust.command.unsign")
    @CommandDescription("Remove your signature from a Item")
    public void execute(Player player) {
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (!plugin.getItemSignService().hasSigned(itemStack, player)) {
            player.sendMessage(Component.translatable("commands.unsign.not-signed")
                    .arguments(plugin.getPrefix()));
            return;
        }

        giveItemStack(player, plugin.getItemSignService().removeSignature(itemStack, player));
        player.sendMessage(Component.translatable("commands.unsign.success")
                .arguments(plugin.getPrefix()));
    }

    @Command("sign <text>")
    @Permission("stardust.command.sign")
    @CommandDescription("Signature the Item in your Hand.")
    public void handleCommand(Player player, @Argument(value = "text") @Quoted String text) {
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack.getType() == Material.AIR) {
            player.sendMessage(Component.translatable("commands.sign.no-item-in-hand")
                    .arguments(plugin.getPrefix()));
            return;
        }

        var signService = plugin.getItemSignService();
        if (signService.hasSigned(itemStack, player) && !player.hasPermission("stardust.command.sign.override")) {
            player.sendMessage(Component.translatable("commands.sign.already-signed")
                    .arguments(plugin.getPrefix()));
            return;
        }

        String formattedDate = Constants.DATE_FORMAT.format(System.currentTimeMillis());
        String itemSignMessage = plugin.getPluginConfiguration().getItemSignMessage();

        Component message = MiniMessage.miniMessage().deserialize(
                itemSignMessage,
                Placeholder.component("text", MiniMessage.miniMessage().deserialize(text)),
                Placeholder.component("player", player.displayName()),
                Placeholder.unparsed("date", formattedDate)
        );

        giveItemStack(player, signService.sign(itemStack, List.of(message), player));
        player.sendMessage(Component.translatable("commands.sign.signed")
                .arguments(plugin.getPrefix()));
    }

    private void giveItemStack(Player player, ItemStack itemStack) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            player.getInventory().setItemInMainHand(itemStack);
        } else {
            int firstEmpty = player.getInventory().firstEmpty();
            if (firstEmpty != -1) {
                player.getInventory().addItem(itemStack);
            } else {
                player.sendMessage(Component.translatable("plugin.inventory-full")
                        .arguments(plugin.getPrefix()));
            }
        }
    }
}
