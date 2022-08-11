package net.onelitefeather.stardust.command.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.onelitefeather.stardust.FeatherEssentials;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record RepairCommand(FeatherEssentials featherEssentials) {

    @CommandMethod("repair [all]")
    @CommandPermission("featheressentials.command.repair")
    @CommandDescription("Repair the Item in your Hand!")
    public void repairItem(@NotNull CommandSender commandSender, @Argument(value = "all") Boolean repairAll) {

        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("plugin.only-player-command")));
            return;
        }

        if (null != repairAll) {
            if (commandSender.hasPermission("featheressentials.command.repairall")) {
                List<String> repaired = repairAll(player);
                if (repaired.size() >= 1) {
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 3.0F, 2.0F);
                    commandSender.sendMessage("§6Repaired: §c" + String.join("§6,§c", repaired));
                    commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.repair.all.success", this.featherEssentials.getPrefix())));
                }
            } else {
                repairItemInHand(player);
            }

        } else {
            repairItemInHand(player);
        }
    }

    private void repairItemInHand(Player player) {

        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack.getType() == Material.AIR || itemStack.getType().isBlock()) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("plugin.no-item-in-hand", this.featherEssentials.getPrefix())));
            return;
        }

        if (!(itemStack.getItemMeta() instanceof Damageable damageable)) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.repair.invalid-item", this.featherEssentials.getPrefix())));
            return;
        }

        if (damageable.getDamage() > 1) {
            damageable.setDamage(0);
            itemStack.setItemMeta(damageable);
            player.updateInventory();
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            player.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.repair.success", this.featherEssentials.getPrefix())));
        }
    }

    private List<String> repairAll(@NotNull Player player) {

        List<String> list = new ArrayList<>();
        PlayerInventory inventory = player.getInventory();
        ItemStack[] items = inventory.getStorageContents();

        Arrays.stream(items).filter(Objects::nonNull).forEach(item -> {
            if (item.getItemMeta() instanceof Damageable damageable) {
                if (damageable.getDamage() > 0) {
                    damageable.setDamage(0);
                    item.setItemMeta(damageable);
                    Component displayName = damageable.displayName();
                    list.add(LegacyComponentSerializer.legacyAmpersand().serialize((displayName != null ? displayName : item.displayName())));
                }
            }
        });

        return list;
    }
}

