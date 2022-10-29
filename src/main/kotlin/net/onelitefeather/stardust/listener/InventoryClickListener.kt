package net.onelitefeather.stardust.listener

import net.onelitefeather.stardust.StardustPlugin
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType

class InventoryClickListener(private val stardustPlugin: StardustPlugin) : Listener {

    @EventHandler
    fun handleInventoryClick(event: InventoryClickEvent) {

        if (event.slotType == InventoryType.SlotType.OUTSIDE) return

        val humanEntity = event.whoClicked
        if (humanEntity !is Player) return

        val inventory = event.clickedInventory ?: return
        if (inventory.holder == null) return

        val groupPriority: Int = stardustPlugin.luckPermsService.getGroupPriority(humanEntity)

        if (inventory == humanEntity.inventory) return
        event.isCancelled = if (inventory.holder is Player) {
            this.stardustPlugin.luckPermsService.getGroupPriority(inventory.holder as Player) > groupPriority
        } else {
            false
        }
    }
}