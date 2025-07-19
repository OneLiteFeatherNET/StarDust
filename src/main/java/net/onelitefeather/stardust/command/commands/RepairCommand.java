package net.onelitefeather.stardust.command.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.onelitefeather.stardust.StardustPlugin;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.incendo.cloud.annotations.*;

import java.util.ArrayList;
import java.util.List;

public class RepairCommand {

    private final StardustPlugin plugin;

    public RepairCommand(StardustPlugin plugin) {
        this.plugin = plugin;
    }

    @Command("repair")
    @Permission("stardust.command.repair")
    @CommandDescription("Repair the Item in your Hand!")
    public void handleCommand(
            Player player,
            @Default("false") @Flag("all") Boolean repairAll
    ) {
        if (repairAll != null && repairAll) {
            if (player.hasPermission("stardust.command.repairall")) {
                List<Component> repaired = repairAll(player);
                if (!repaired.isEmpty()) {
                    player.sendMessage(Component
                            .translatable("commands.repair.all.success")
                            .arguments(plugin.getPrefix(), Component.join(JoinConfiguration.commas(false), repaired)));

                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
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
            player.sendMessage(Component.translatable("plugin.no-item-in-hand").arguments(plugin.getPrefix()));
            return;
        }

        if (!(itemStack.getItemMeta() instanceof Damageable damageable)) {
            player.sendMessage(Component.translatable("commands.repair.invalid-item").arguments(plugin.getPrefix()));
            return;
        }

        if (damageable.getDamage() > 0) {
            damageable.setDamage(0);
            itemStack.setItemMeta(damageable);
            player.updateInventory();
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
            player.sendMessage(Component.translatable("commands.repair.success").arguments(plugin.getPrefix()));
        }
    }

    private List<Component> repairAll(Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack[] items = inventory.getContents();
        List<Component> list = new ArrayList<>();
        for (ItemStack item : items) {
            if (item == null) continue;
            if (item.getItemMeta() instanceof Damageable damageable && damageable.getDamage() > 0) {
                damageable.setDamage(0);
                item.setItemMeta(damageable);
                list.add(Component.text(item.getType().name().toLowerCase()));
            }
        }
        player.updateInventory();
        return list;
    }
}
