package net.onelitefeather.stardust.command.commands

import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.Flag
import net.kyori.adventure.text.minimessage.MiniMessage
import net.onelitefeather.stardust.StardustPlugin
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.Damageable

class RepairCommand(private val stardustPlugin: StardustPlugin) {

    @CommandMethod("repair")
    @CommandPermission("stardust.command.repair")
    @CommandDescription("Repair the Item in your Hand!")
    fun handleCommand(player: Player, @Flag(value = "all") repairAll: Boolean?) {

        if (null != repairAll) {
            if (player.hasPermission("stardust.command.repairall")) {
                val repaired: List<String> = repairAll(player)
                if (repaired.isNotEmpty()) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<lang:commands.repair.all.success:'${stardustPlugin.getPluginPrefix()}':'${repaired.joinToString("<lang:commands.repair.repaired-items.separator>")}'>"))
                    player.playSound(player.location, Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f)
                }
            } else {
                repairItemInHand(player)
            }
        } else {
            repairItemInHand(player)
        }
    }

    private fun repairItemInHand(player: Player) {

        val itemStack = player.inventory.itemInMainHand
        if (itemStack.type == Material.AIR || itemStack.type.isBlock) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<lang:plugin.no-item-in-hand:'${stardustPlugin.getPluginPrefix()}'>"))
            return
        }

        if (itemStack.itemMeta !is Damageable) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<lang:commands.repair.invalid-item:'${stardustPlugin.getPluginPrefix()}'>"))
            return
        }

        val damageable = itemStack.itemMeta as Damageable
        if (damageable.damage > 0) {
            damageable.damage = 0
            itemStack.itemMeta = damageable
            player.updateInventory()
            player.playSound(player.location, Sound.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f)
            player.sendMessage(MiniMessage.miniMessage().deserialize("<lang:commands.repair.success:'${stardustPlugin.getPluginPrefix()}'>"))
        }

    }

    private fun repairAll(player: Player): List<String> {

        val inventory = player.inventory
        val items = inventory.contents

        val list = arrayListOf<String>()

        for (itemStack in items) {
            if (itemStack == null) continue
            val itemMeta = itemStack.itemMeta
            if (itemMeta is Damageable && itemMeta.damage > 0) {
                itemMeta.damage = 0
                itemStack.itemMeta = itemMeta
                list.add(itemStack.type.name.lowercase())
            }
        }

        return list
    }
}