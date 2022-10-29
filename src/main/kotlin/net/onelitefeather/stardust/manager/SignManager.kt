package net.onelitefeather.stardust.manager

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.onelitefeather.stardust.StardustPlugin
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import java.text.SimpleDateFormat
import java.util.*


class SignManager(private val stardustPlugin: StardustPlugin, private val itemStack: ItemStack) {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy")

    fun sign(uuid: UUID, name: String, message: String): ItemStack {
        val lore: MutableList<Component>?
        if (!isSigned()) setSigned(true)
        val itemMeta: ItemMeta = itemStack.itemMeta
        lore = if (itemMeta.lore() == null) null else itemMeta.lore()
        if (lore != null) {
            lore.add(
                MiniMessage.miniMessage()
                    .deserialize(stardustPlugin.getMessage("commands.sign.item-lore-header-footer"))
            )
            lore.add(Component.text(" "))
            lore.add(
                MiniMessage.miniMessage().deserialize(
                    MiniMessage.miniMessage().serialize(
                        LegacyComponentSerializer.legacyAmpersand().deserialize(
                            stardustPlugin.getMessage("commands.sign.item-lore-message", message)
                        )
                    )
                )
            )
            lore.add(
                MiniMessage.miniMessage().deserialize(
                    stardustPlugin.getMessage(
                        "commands.sign.item-lore-author", name, dateFormat.format(
                            System.currentTimeMillis()
                        )
                    )
                )
            )
            lore.add(Component.text(" "))
            lore.add(
                MiniMessage.miniMessage()
                    .deserialize(stardustPlugin.getMessage("commands.sign.item-lore-header-footer"))
            )
            lore.add(Component.text(" "))
            itemMeta.lore(lore)
            itemStack.itemMeta = itemMeta
        }

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
        itemMeta.persistentDataContainer[stardustPlugin.signedNameSpacedKey, PersistentDataType.INTEGER] = if (signed) 1 else 0
        itemStack.itemMeta = itemMeta
    }
}