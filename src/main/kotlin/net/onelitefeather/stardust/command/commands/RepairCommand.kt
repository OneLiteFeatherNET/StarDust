package net.onelitefeather.stardust.command.commands

import net.kyori.adventure.text.Component
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.util.COMPONENT_JOIN_CONFIG
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.Damageable
import org.incendo.cloud.annotations.*

class RepairCommand(private val stardustPlugin: StardustPlugin) {

    @Command("repair")
    @Permission("stardust.command.repair")
    @CommandDescription("Repair the Item in your Hand!")
    fun handleCommand(player: Player,
                      @Default(value = "false")
                      @Flag(value = "all") repairAll: Boolean?) {

        if (null != repairAll) {
            if (player.hasPermission("stardust.command.repairall")) {
                val repaired = repairAll(player)
                if (repaired.isNotEmpty()) {

                    player.sendMessage(
                        Component.translatable("commands.repair.all.success").arguments(
                            stardustPlugin.getPluginPrefix(),
                            Component.join(COMPONENT_JOIN_CONFIG, repaired)
                        )
                    )

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
            player.sendMessage(
                Component.translatable("plugin.no-item-in-hand").arguments(stardustPlugin.getPluginPrefix())
            )
            return
        }

        if (itemStack.itemMeta !is Damageable) {
            player.sendMessage(
                Component.translatable("commands.repair.invalid-item").arguments(stardustPlugin.getPluginPrefix())
            )
            return
        }

        val damageable = itemStack.itemMeta as Damageable
        if (damageable.damage > 0) {
            damageable.damage = 0
            itemStack.itemMeta = damageable
            player.updateInventory()
            player.playSound(player.location, Sound.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f)
            player.sendMessage(
                Component.translatable("commands.repair.success").arguments(stardustPlugin.getPluginPrefix())
            )
        }

    }

    private fun repairAll(player: Player): List<Component> {

        val inventory = player.inventory
        val items = inventory.contents

        val list = arrayListOf<Component>()
        items.forEach {
            if (it == null) return@forEach
            val itemMeta = it.itemMeta
            if (itemMeta is Damageable && itemMeta.damage > 0) {
                itemMeta.damage = 0
                it.itemMeta = itemMeta
                list.add(Component.text(it.type.name.lowercase()))
            }
        }

        return list
    }
}
