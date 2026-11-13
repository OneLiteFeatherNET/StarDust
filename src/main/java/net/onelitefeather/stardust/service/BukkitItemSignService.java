package net.onelitefeather.stardust.service;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.onelitefeather.stardust.StardustPlugin;
import net.onelitefeather.stardust.api.ItemSignService;
import net.onelitefeather.stardust.util.UUIDTagType;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BukkitItemSignService implements ItemSignService<ItemStack, Player> {

    private final NamespacedKey signedNameSpacedKey;

    public BukkitItemSignService(StardustPlugin stardustPlugin) {
        this.signedNameSpacedKey = new NamespacedKey(stardustPlugin, "signed");
    }

    @Override
    public boolean hasSigned(ItemStack itemStack, Player player) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return false;
        return getSignedPlayers(itemStack).contains(player.getUniqueId());
    }

    @Override
    public ItemStack sign(ItemStack baseItemStack, List<Component> lore, Player player) {
        return getItemStack(baseItemStack, player, lore, true);
    }

    @Override
    public ItemStack removeSignature(ItemStack baseItemStack, Player player) {
        List<Component> lore = baseItemStack.lore() != null ? baseItemStack.lore() : new ArrayList<>();
        return getItemStack(baseItemStack, player, lore, false);
    }

    private ItemStack getItemStack(ItemStack base, Player player, List<Component> lore, boolean sign) {
        boolean isInCreative = player.getGameMode() == GameMode.CREATIVE;
        if (!isInCreative) {
            if (base.getAmount() > 1) {
                base.setAmount(base.getAmount() - 1);
            } else {
                player.getInventory().removeItem(base);
            }
        }

        ItemStack item = base.clone();
        if (!isInCreative) item.setAmount(1);

        ItemStack itemStack = buildSignedPlayers(player, item, sign);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.lore(buildLore(player, itemStack, sign, lore));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private ItemStack buildSignedPlayers(Player player, ItemStack itemStack, boolean sign) {

        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        var uuidList = PersistentDataType.LIST.listTypeFrom(UUIDTagType.UUID);
        var players = new ArrayList<>(getSignedPlayers(itemStack));

        boolean hasNotSigned = !hasSigned(itemStack, player);
        if (sign) {

            //The if statement has to be separated to the one above,
            // because of players who can sign the same item multiple times
            if (hasNotSigned) players.add(player.getUniqueId());

        } else {
            players.remove(player.getUniqueId());
        }

        container.set(signedNameSpacedKey, uuidList, players);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private List<Component> buildLore(Player player, ItemStack itemStack, boolean sign, List<Component> lore) {
        List<Component> currentLore = itemStack.lore();
        if (currentLore == null) {
            return lore;
        } else {
            if (sign) {
                currentLore.addAll(lore);
            } else {
                currentLore.clear();
                currentLore.addAll(removePlayerFromLore(player, lore));
            }
        }

        return currentLore;
    }

    private List<Component> removePlayerFromLore(Player player, List<Component> lore) {
        String playerName = player.getName();
        List<Component> filtered = new ArrayList<>();
        for (Component component : lore) {
            String text = PlainTextComponentSerializer.plainText().serialize(component);
            if (!(text.contains(playerName))) {
                filtered.add(component);
            }
        }
        return filtered;
    }

    private List<UUID> getSignedPlayers(ItemStack itemStack) {

        var uuidList = PersistentDataType.LIST.listTypeFrom(UUIDTagType.UUID);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return Collections.emptyList();

        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        if (!container.has(signedNameSpacedKey, uuidList)) return new ArrayList<>();

        var signedPlayers = container.get(signedNameSpacedKey, uuidList);
        if (signedPlayers == null) return new ArrayList<>();
        return signedPlayers;
    }
}
