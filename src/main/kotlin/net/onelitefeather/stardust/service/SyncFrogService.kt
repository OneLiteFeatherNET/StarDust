package net.onelitefeather.stardust.service

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.util.PlayerUtils
import net.onelitefeather.stardust.util.StringUtils
import org.bukkit.*
import org.bukkit.command.CommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Frog
import org.bukkit.entity.Frog.Variant
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.StringUtil
import org.incendo.cloud.annotation.specifier.Quoted
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.suggestion.Suggestions
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput

class SyncFrogService(val stardustPlugin: StardustPlugin) : Listener, StringUtils, PlayerUtils {

    private var frogNameSpacedKey: NamespacedKey = NamespacedKey(stardustPlugin, "frog_data_key")
    private var frogVariantKey: NamespacedKey = NamespacedKey(stardustPlugin, "frog_variant_key")
    private var frogCustomNameKey: NamespacedKey = NamespacedKey(stardustPlugin, "frog_custom_name_key")
    private var frogInBucket: Int = 1
    private var frogBucketName: String = "Frog Bucket"

    init {
        stardustPlugin.server.pluginManager.registerEvents(this, stardustPlugin)
    }

    @Command("frogbucket <customName> <variant> <amount>")
    @CommandDescription("Gives you a Frog bucket with the given amount!")
    @Permission("stardust.command.frogbucket")
    fun executeCommand(
        player: Player,
        @Quoted @Argument(value = "customName") customName: String,
        @Argument(value = "variant", suggestions = "frogVariants") variant: String,
        @Argument(value = "amount") amount: Int
    ) {
        addFrogBucketToPlayer(player, amount, MiniMessage.miniMessage().deserialize(customName), frogVariant(variant), false)
    }

    @EventHandler
    fun handlePlayerInteract(event: PlayerInteractEvent) {

        val player = event.player
        val itemStack = event.item ?: return
        val clickedBlock = event.clickedBlock ?: return

        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        if (!isFrogInBucket(itemStack)) return
        if (deserializeFrogData(itemStack, clickedBlock.location.add(0.0, 1.0, 0.0))) {
            removeFrogBucket(player, player.inventory.itemInMainHand, true)
            player.sendMessage(
                Component.translatable("frog-bucket-spawn-success").arguments(stardustPlugin.getPluginPrefix())
            )
        } else {
            player.sendMessage(Component.translatable("frog-cannot-be-spawned").arguments(stardustPlugin.getPluginPrefix()))
        }
    }

    private fun removeFrogBucket(player: Player, itemStack: ItemStack, giveEmptyBucket: Boolean) {
        if (player.gameMode == GameMode.CREATIVE) return
        if (itemStack.amount > 1) {
            player.inventory.setItemInMainHand(itemStack.subtract(1))
        } else {
            player.inventory.setItemInMainHand(null)
        }

        if (giveEmptyBucket) player.inventory.addItem(ItemStack(Material.BUCKET))
    }

    @EventHandler
    fun handlePlayerEntityInteract(event: PrePlayerAttackEntityEvent) {

        val player = event.player
        val entity = event.attacked
        val itemInHand = player.inventory.itemInMainHand

        if (isFrogInBucket(itemInHand)) return
        if (entity is Frog && itemInHand.type == Material.BUCKET) {
            addFrogBucketToPlayer(
                player,
                1,
                entity.customName() ?: MiniMessage.miniMessage().deserialize(entity.name),
                entity.variant,
                true
            )
            entity.health = 0.0
            removeFrogBucket(player, itemInHand, false)
            event.isCancelled = true
        }
    }

    private fun isFrogInBucket(itemStack: ItemStack): Boolean {

        val itemMeta = itemStack.itemMeta ?: return false
        val container = itemMeta.persistentDataContainer

        if (!container.has(frogNameSpacedKey)) return false
        val value = container[frogNameSpacedKey, PersistentDataType.INTEGER]
        return value != null && value == frogInBucket
    }

    private fun deserializeFrogData(itemStack: ItemStack, location: Location): Boolean {

        val frog = location.world.spawn(location, Frog::class.java)
        if (!frog.isInWorld) return false
        val itemMeta = itemStack.itemMeta
        val container = itemMeta.persistentDataContainer
        if (!container.has(frogCustomNameKey) || !container.has(frogVariantKey)) return false

        val customName = container[frogCustomNameKey, PersistentDataType.STRING]
        val variant = container[frogVariantKey, PersistentDataType.STRING] ?: frogVariantName(Variant.TEMPERATE)

        if (customName != null) {
            frog.customName(colorText(customName))
        }

        frog.variant = frogVariant(variant)
        return true
    }

    private fun createFrogItemStack(amount: Int, variant: Variant, customName: Component?): ItemStack {

        val itemStack = ItemStack(Material.BUCKET, amount)
        val itemMeta = itemStack.itemMeta

        itemMeta.displayName(MiniMessage.miniMessage().deserialize(frogBucketName))

        itemMeta.addEnchant(Enchantment.EFFICIENCY, 1, false)
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        itemMeta.lore(listOf(MiniMessage.miniMessage().deserialize(frogVariantName(variant))))

        val container = itemMeta.persistentDataContainer

        if (customName != null) {
            container[frogCustomNameKey, PersistentDataType.STRING] = convertComponentToString(customName)
        }
        container[frogNameSpacedKey, PersistentDataType.INTEGER] = frogInBucket
        container[frogVariantKey, PersistentDataType.STRING] = variant.key().value()

        itemStack.itemMeta = itemMeta
        return itemStack
    }

    private fun addFrogBucketToPlayer(
        player: Player,
        amount: Int,
        customName: Component,
        variant: Variant,
        actionBarMessage: Boolean
    ) {

        val slot = player.inventory.firstEmpty()

        //If the slot returns -1 the inventory of the player is full
        if (slot == -1) {
            player.sendMessage(
                Component.translatable("plugin.inventory-full").arguments(stardustPlugin.getPluginPrefix())
            )
            return
        }

        val itemStack = createFrogItemStack(amount, variant, customName)
        player.inventory.setItem(slot, itemStack)
        val message = Component.translatable("frog-bucket-added-to-inventory").arguments(
            stardustPlugin.getPluginPrefix(),
            Component.text(frogBucketName),
            Component.text(amount),
            player.displayName()
        )

        if (!actionBarMessage) {
            player.sendMessage(message)
        } else {
            player.sendActionBar(message)
        }
    }

    private fun frogVariantName(variant: Variant): String = variant.key().value()

    private fun frogVariant(key: String): Variant {
        return Registry.FROG_VARIANT[NamespacedKey.fromString(key.lowercase())!!] as Variant
    }

    @Suggestions(value = "frogVariants")
    fun frogVariants(context: CommandContext<CommandSender>, input: String): MutableList<String> {
        val variants = Registry.FROG_VARIANT.stream().map { it.key.value() }.toList()
        return StringUtil.copyPartialMatches(input, variants, variants)
    }
}