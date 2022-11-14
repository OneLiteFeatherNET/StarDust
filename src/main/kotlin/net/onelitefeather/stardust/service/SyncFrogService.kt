package net.onelitefeather.stardust.service

import net.kyori.adventure.text.format.NamedTextColor
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.extenstions.convertComponentToString
import net.onelitefeather.stardust.extenstions.miniMessage
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Frog
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class SyncFrogService(stardustPlugin: StardustPlugin) : Listener {

    private var frogNameSpacedKey: NamespacedKey = NamespacedKey(stardustPlugin, "frog_data_key")

    init {
        stardustPlugin.server.pluginManager.registerEvents(this, stardustPlugin)
    }

    @EventHandler
    fun handlePlayerInteract(event: PlayerInteractEvent) {

        val player = event.player
        val itemStack = event.item ?: return
        val clickedBlock = event.clickedBlock ?: return

        if(event.action != Action.RIGHT_CLICK_BLOCK) return

        if (!itemStack.itemMeta.persistentDataContainer.has(frogNameSpacedKey)) return
        if (deserializeFrogData(itemStack, clickedBlock.location.add(0.0, 1.0, 0.0))) {
            player.sendMessage(miniMessage { "Frog was successfully synced!" })
        }
    }

    @EventHandler
    fun handlePlayerEntityInteract(event: PlayerInteractEntityEvent) {

        val player = event.player
        val entity = event.rightClicked

        if (entity is Frog && player.inventory.itemInMainHand.type == Material.BUCKET) {
            player.inventory.addItem(saveFrogData(entity, player))
            entity.health = 0.0
        }
    }

    private fun saveFrogData(frog: Frog, player: Player): ItemStack {
        val itemStack = player.inventory.itemInMainHand
        val itemMeta = itemStack.itemMeta

        itemMeta.displayName(frog.customName() ?: miniMessage { "${frog.name}-Bucket" })
        itemMeta.lore(listOf(miniMessage { NamedTextColor.GRAY.toString() + frog.variant.name.lowercase() }))

        val container = itemMeta.persistentDataContainer
        container[frogNameSpacedKey, PersistentDataType.STRING] = serializeFrogData(frog)
        itemStack.itemMeta = itemMeta
        return itemStack
    }

    private fun deserializeFrogData(itemStack: ItemStack, location: Location): Boolean {

        val frog = location.world.spawn(location, Frog::class.java)
        val itemMeta = itemStack.itemMeta
        val container = itemMeta.persistentDataContainer
        if (!container.has(frogNameSpacedKey)) return false

        val rawData = container[frogNameSpacedKey, PersistentDataType.STRING] ?: return false

        if (rawData.contains(":")) {
            // Split the String by a colon
            val data = rawData.split(":")

            // Check the data contains a customName
            if (data.contains("customName")) {

                val customName = data[0].split("=")[0]
                val variantName = data[1].split("=")[0]

                frog.customName(miniMessage { customName })
                frog.variant = Frog.Variant.valueOf(variantName)

            } else {
                if (data.contains("variant")) {
                    val variantName = data[0].split("=")[0]
                    frog.variant = Frog.Variant.valueOf(variantName)
                }
            }

        } else {
            //We can set the variant safely
            frog.variant = Frog.Variant.valueOf(rawData.uppercase())
        }

        return true
    }


    private fun serializeFrogData(frog: Frog): String {

        var data = ""
        val customName = frog.customName()

        if (customName != null) {
            data.plus("customName=").plus(convertComponentToString(customName)).plus(":").plus("variant=")
                .plus(frog.variant.name)
        } else {
            data = frog.variant.name
        }

        return data
    }
}