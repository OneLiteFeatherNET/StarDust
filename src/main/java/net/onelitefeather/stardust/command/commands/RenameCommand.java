package net.onelitefeather.stardust.command.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Quoted;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.onelitefeather.stardust.StardustPlugin;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public record RenameCommand(StardustPlugin stardustPlugin) {

    @CommandMethod("itemrename|rename <text>")
    @CommandPermission("featheressentials.command.rename")
    @CommandDescription("Rename a Item")
    public void onCommand(@NotNull CommandSender commandSender, @Argument(value = "text") @Quoted String text) {

        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.stardustPlugin.getMessage("plugin.only-player-command")));
            return;
        }

        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack.getType() == Material.AIR) {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.stardustPlugin.getMessage("commands.rename.invalid-item", this.stardustPlugin.getPrefix())));
            return;
        }

        Component displayName = LegacyComponentSerializer.legacy('&').deserialize(text);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(displayName);
        itemStack.setItemMeta(itemMeta);
        player.updateInventory();
        commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.stardustPlugin.getMessage("commands.rename.success", this.stardustPlugin.getPrefix(), LegacyComponentSerializer.legacyAmpersand().serialize(displayName))));
    }
}

