package net.onelitefeather.stardust.command.commands;

import net.kyori.adventure.text.Component;
import net.onelitefeather.stardust.StardustPlugin;
import net.onelitefeather.stardust.util.StringUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.incendo.cloud.annotation.specifier.Quoted;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;

public class RenameCommand {

    private final StardustPlugin plugin;

    public RenameCommand(StardustPlugin plugin) {
        this.plugin = plugin;
    }

    @Command("itemrename|rename <text>")
    @Permission("stardust.command.rename")
    @CommandDescription("Rename your current held item")
    public void handleCommand(Player player, @Argument(value = "text") @Quoted String text) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() == Material.AIR) {
            player.sendMessage(Component.translatable("commands.rename.invalid-item").arguments(plugin.getPrefix()));
            return;
        }

        Component itemDisplayName = StringUtil.secureComponent(player, StringUtil.colorText(text));

        ItemMeta itemMeta = itemInHand.getItemMeta();
        itemMeta.displayName(itemDisplayName);
        itemInHand.setItemMeta(itemMeta);
        player.updateInventory();
        player.sendMessage(Component.translatable("commands.rename.success").arguments(plugin.getPrefix(), StringUtil.secureComponent(player, itemDisplayName)));
    }
}
