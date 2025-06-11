package net.onelitefeather.stardust.api;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface ItemSignService<I extends ItemStack, P extends Player> {

    /**
     * Sign an item with specific lore lines and more
     * @param baseItemStack to sign
     * @param lore to bet set to the item
     * @param player be used for replacement on the lore
     */
    I sign(I baseItemStack, List<Component> lore, P player);

    /**
     * Remove a signature from an item
     * @param baseItemStack to be modified
     * @param player be used for replacement on the lore
     */
    I removeSignature(I baseItemStack, P player);

    /**
     * Checks if the item be signed
     * @param itemStack to be checked
     * @param player be used
     */
    boolean hasSigned(I itemStack, P player);
}
