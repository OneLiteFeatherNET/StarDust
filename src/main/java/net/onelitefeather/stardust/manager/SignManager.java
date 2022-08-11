package net.onelitefeather.stardust.manager;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.onelitefeather.stardust.FeatherEssentials;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

public class SignManager {

    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    private final FeatherEssentials featherEssentials;
    private ItemStack itemStack;

    public SignManager(@NotNull FeatherEssentials featherEssentials, @NotNull ItemStack itemStack) {
        this.featherEssentials = featherEssentials;
        this.itemStack = itemStack;
    }

    public void setItemStack(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStack sign(@NotNull UUID uuid, @NotNull String name, @NotNull String message) {
        List<Component> lore;
        if (!isSigned())
            setSigned(true);

        ItemMeta itemMeta = this.itemStack.getItemMeta();
        lore = itemMeta.lore() == null ? Lists.newArrayList() : itemMeta.lore();
        if (lore != null) {
            lore.add(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.sign.item-lore-header-footer")));
            lore.add(Component.text(" "));
            lore.add(MiniMessage.miniMessage().deserialize(MiniMessage.miniMessage().serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(this.featherEssentials.getMessage("commands.sign.item-lore-message", message)))));
            lore.add(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.sign.item-lore-author", name, DATE_FORMAT.format(System.currentTimeMillis()))));
            lore.add(Component.text(" "));
            lore.add(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.sign.item-lore-header-footer")));
            lore.add(Component.text(" "));
            itemMeta.lore(lore);
            this.itemStack.setItemMeta(itemMeta);
        }

        return this.itemStack;
    }


    public boolean isSigned() {
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        if (itemMeta == null) return false;
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        if (!container.has(this.featherEssentials.getSignedNameSpacedKey(), PersistentDataType.INTEGER)) return false;
        Integer integer = container.get(this.featherEssentials.getSignedNameSpacedKey(), PersistentDataType.INTEGER);
        return integer != null && integer == 1;
    }

    public void setSigned(boolean signed) {
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        if (itemMeta == null) return;
        itemMeta.getPersistentDataContainer().set(this.featherEssentials.getSignedNameSpacedKey(), PersistentDataType.INTEGER, signed ? 1 : 0);
        this.itemStack.setItemMeta(itemMeta);
    }


}
