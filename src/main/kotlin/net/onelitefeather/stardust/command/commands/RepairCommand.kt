package net.onelitefeather.stardust.command.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.specifier.Greedy
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.extenstions.miniMessage
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.Damageable

class RepairCommand(private val stardustPlugin: StardustPlugin) {

    @CommandMethod("repair [all]")
    @CommandPermission("stardust.command.repair")
    @CommandDescription("Repair the Item in your Hand!")
    fun handleCommand(player: Player, @Greedy @Argument(value = "all") repairAll: Boolean?) {

        if (null != repairAll) {
            if (player.hasPermission("stardust.command.repairall")) {
                val repaired: List<String> = repairAll(player)
                if (repaired.isNotEmpty()) {
                    player.sendMessage("§6Repaired: §c".plus(repaired.joinToString("§6,§c")))
                    player.sendMessage(
                        miniMessage {
                            stardustPlugin.i18nService.getMessage(
                                "commands.repair.all.success",
                                *arrayOf(stardustPlugin.i18nService.getPluginPrefix())
                            )
                        }
                    )
                    player.playSound(player.location, Sound.BLOCK_ANVIL_USE, 3.0f, 2.0f)
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
            player.sendMessage(miniMessage {
                stardustPlugin.i18nService.getMessage(
                    "plugin.no-item-in-hand",
                    *arrayOf(stardustPlugin.i18nService.getPluginPrefix())
                )
            })
            return
        }

        if (itemStack.itemMeta !is Damageable) {
            player.sendMessage(miniMessage {
                stardustPlugin.i18nService.getMessage(
                    "commands.repair.invalid-item",
                    *arrayOf(stardustPlugin.i18nService.getPluginPrefix())
                )
            })
            return
        }

        val damageable = itemStack.itemMeta as Damageable
        if (damageable.damage > 1) {
            damageable.damage = 0
            itemStack.itemMeta = damageable
            player.updateInventory()
            player.playSound(player.location, Sound.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f)
            player.sendMessage(miniMessage {
                stardustPlugin.i18nService.getMessage(
                    "commands.repair.success",
                    *arrayOf(stardustPlugin.i18nService.getPluginPrefix())
                )
            })
        }

    }

    private fun repairAll(player: Player): List<String> {

        val inventory = player.inventory
        val items = inventory.storageContents

        val list = arrayListOf<String>()

        for (itemStack in items) {
            if (itemStack == null) continue
            val itemMeta = itemStack.itemMeta
            if (itemMeta is Damageable && itemMeta.damage > 0) {
                itemMeta.damage = 0
                itemStack.itemMeta = itemMeta
                list.add(stardustPlugin.i18nService.translateLegacyString(itemStack.displayName()))
            }
        }

        return list
    }
}