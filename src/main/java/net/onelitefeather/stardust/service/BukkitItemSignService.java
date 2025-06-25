package net.onelitefeather.stardust.service;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.onelitefeather.stardust.StardustPlugin;
import net.onelitefeather.stardust.api.ItemSignService;
import net.onelitefeather.stardust.user.User;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BukkitItemSignService implements ItemSignService<ItemStack, Player> {

    private final StardustPlugin stardustPlugin;
    private final NamespacedKey signedNameSpacedKey;

    public BukkitItemSignService(StardustPlugin stardustPlugin) {
        this.stardustPlugin = stardustPlugin;
        this.signedNameSpacedKey = new NamespacedKey(stardustPlugin, "signed");
    }

    @Override
    public boolean hasSigned(ItemStack itemStack, Player player) {

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return false;

        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        if (!container.has(signedNameSpacedKey, PersistentDataType.LONG_ARRAY)) return false;

        long[] array = container.get(signedNameSpacedKey, PersistentDataType.LONG_ARRAY);
        User user = stardustPlugin.getUserService().getUser(player.getUniqueId());

        if (user == null || array == null) return false;
        return Arrays.stream(array).anyMatch(id -> id == user.getId());
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

        User user = stardustPlugin.getUserService().getUser(player.getUniqueId());
        if (user == null) return base;

        ItemStack itemStack = buildSignedUsers(player, base, sign, user);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.lore(buildLore(player, base, sign, lore));
        itemStack.setItemMeta(itemMeta);

        if (!isInCreative) {
            itemStack.setAmount(1);
        }
        return itemStack;
    }

    private ItemStack buildSignedUsers(Player player, ItemStack itemStack, boolean sign, User user) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        if (sign) {
            if (!hasSigned(itemStack, player)) {
                container.set(signedNameSpacedKey, PersistentDataType.LONG_ARRAY, addPlayerSign(itemStack, user));
            }
        } else {
            container.set(signedNameSpacedKey, PersistentDataType.LONG_ARRAY, removePlayerSign(itemStack, user));
        }

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
                currentLore.addAll(removePlayerFromLore(player, currentLore));
            }
        }

        return currentLore;
    }

    private List<Component> removePlayerFromLore(Player player, List<Component> lore) {
        UUID playerId = player.getUniqueId();
        String playerName = player.getName();
        List<Component> filtered = new ArrayList<>();
        for (Component component : lore) {
            String text = PlainTextComponentSerializer.plainText().serialize(component);
            UUID foundId = stardustPlugin.getServer().getPlayerUniqueId(playerName);
            if (!(text.contains(playerName) && playerId.equals(foundId))) {
                filtered.add(component);
            }
        }
        return filtered;
    }

    private long[] removePlayerSign(ItemStack itemStack, User user) {
        long[] players = getSignedPlayers(itemStack);
        if (players == null) return new long[]{user.getId()};
        return Arrays.stream(players)
                .filter(id -> id != user.getId())
                .toArray();
    }

    private long[] addPlayerSign(ItemStack itemStack, User user) {
        long[] players = getSignedPlayers(itemStack);
        if (players == null) return new long[]{user.getId()};
        long[] result = new long[players.length + 1];
        result[players.length] = user.getId();
        return result;
    }

    private long[] getSignedPlayers(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return new long[0];
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        if (!container.has(signedNameSpacedKey, PersistentDataType.LONG_ARRAY)) return new long[0];
        return container.get(signedNameSpacedKey, PersistentDataType.LONG_ARRAY);
    }
}
