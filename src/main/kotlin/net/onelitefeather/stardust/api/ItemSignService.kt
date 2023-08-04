package net.onelitefeather.stardust.api

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * API Class to handle item Signing
 * @author TheMeinerLP
 */
interface ItemSignService<I : ItemStack, P : Player> {

    /**
     * Sign an item with specific lore lines and more
     * @param baseItemStack to sign
     * @param lore to bet set to the item
     * @param player be used for replacement on the lore
     */
    fun sign(baseItemStack: I, lore: List<Component>, player: P): I

    /**
     * Remove a signature from an item
     * @param baseItemStack to be modified
     * @param player be used for replacement on the lore
     */
    fun removeSignature(baseItemStack: I, player: P): I

    /**
     * Checks if the item be signed
     * @param itemStack to be checked
     * @param player be used
     */
    fun hasSigned(itemStack: I, player: P): Boolean

}