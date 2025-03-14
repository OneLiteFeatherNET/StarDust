package net.onelitefeather.stardust.listener

import net.onelitefeather.stardust.StardustPlugin
import org.bukkit.GameMode
import org.bukkit.block.Container
import org.bukkit.block.EnderChest
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory

class VanishSilentContainerFeature(private val stardustPlugin: StardustPlugin) : Listener {

    private val silentContainerLooter: MutableMap<Player, Inventory> = HashMap()

    @EventHandler
    fun handleInventoryClose(event: InventoryCloseEvent) {
        silentContainerLooter.remove(event.player)
    }

    @EventHandler
    fun handleInventoryClick(event: InventoryClickEvent) {

        val whoClicked = event.whoClicked
        if (whoClicked !is Player) return
        val clickedInventory = event.inventory

        if (stardustPlugin.userService.playerVanishService.isVanished(whoClicked)) {
            val inventory = silentContainerLooter[whoClicked] ?: return
            event.isCancelled =
                clickedInventory == inventory && !whoClicked.hasPermission("stardust.vanish.silentopen.interact")
        }
    }

    @EventHandler
    fun handlePlayerInteract(event: PlayerInteractEvent) {

        val player = event.player
        val clickedBlock = event.clickedBlock ?: return
        val blockState = clickedBlock.state

        val hasPermission = player.hasPermission("stardust.vanish.silentopen")

        val vanished = stardustPlugin.userService.playerVanishService.isVanished(player)
        if (blockState is EnderChest) {

            val useInteractBlock = if (vanished && !player.isSneaking) {
                true
            } else {
                player.isSneaking
            }

            if (vanished && player.isSneaking) {
                player.openInventory(player.enderChest)
            }

            event.isCancelled = useInteractBlock
            return
        }

        if (blockState !is Container) return

        if (vanished) {
            if (hasPermission && player.isSneaking && event.action.isRightClick) {
                silentContainerLooter[player] = blockState.inventory

                player.velocity.setY(player.location.blockY + 1.5)
                player.gameMode = GameMode.SPECTATOR
                player.server.scheduler.runTaskLater(stardustPlugin, Runnable {
                    val previousGameMode = player.previousGameMode ?: player.server.defaultGameMode
                    player.gameMode = previousGameMode
                }, 20L)
            } else {
                event.isCancelled = true
            }
        }
    }
}