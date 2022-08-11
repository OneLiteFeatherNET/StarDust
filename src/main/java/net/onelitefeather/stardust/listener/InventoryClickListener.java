package net.onelitefeather.stardust.listener;

import net.onelitefeather.stardust.FeatherEssentials;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public record InventoryClickListener(FeatherEssentials featherEssentials) implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (event.getSlotType() == InventoryType.SlotType.OUTSIDE) return;
        HumanEntity humanEntity = event.getWhoClicked();
        int playerPotency = this.featherEssentials.getVaultHook().getGroupPriority(humanEntity.getUniqueId());

        Inventory inventory = event.getView().getTopInventory();

        boolean cancel = !humanEntity.hasPermission("featheressentials.invsee.others");
        if (inventory.getHolder() instanceof Player target) {
            if (humanEntity == target) return;
            if (this.featherEssentials.getVaultHook().getGroupPriority(target.getUniqueId()) > playerPotency) {
                cancel = true;
            }
        }

        event.setCancelled(cancel);
    }
}
