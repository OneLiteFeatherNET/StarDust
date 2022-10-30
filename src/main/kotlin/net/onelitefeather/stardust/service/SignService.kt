package net.onelitefeather.stardust.service

import net.kyori.adventure.text.Component
import net.onelitefeather.stardust.StardustPlugin
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType


class SignService(private val stardustPlugin: StardustPlugin, private var itemStack: ItemStack) {

    fun sign(lore: List<Component>, player: Player): ItemStack {
        if (!isSigned()) setSigned(true)
        if (player.gameMode != GameMode.CREATIVE) {
            if (itemStack.amount > 1) {
                itemStack.amount = itemStack.amount - 1
            } else {
                player.inventory.remove(itemStack)
            }

            itemStack = itemStack.asOne()
        }

        itemStack.lore(lore)
        return itemStack
    }

    fun isSigned(): Boolean {
        val itemMeta = itemStack.itemMeta ?: return false
        val container = itemMeta.persistentDataContainer
        if (!container.has(stardustPlugin.signedNameSpacedKey, PersistentDataType.INTEGER)) return false
        val integer = container[stardustPlugin.signedNameSpacedKey, PersistentDataType.INTEGER]
        return integer != null && integer == 1
    }


    fun setSigned(signed: Boolean) {
        val itemMeta = itemStack.itemMeta ?: return
        itemMeta.persistentDataContainer[stardustPlugin.signedNameSpacedKey, PersistentDataType.INTEGER] =
            if (signed) 1 else 0
        itemStack.itemMeta = itemMeta
    }
}