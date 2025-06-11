package net.onelitefeather.stardust.service;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.stardust.StardustPlugin;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.StringUtil;
import org.incendo.cloud.annotation.specifier.Quoted;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SyncFrogService implements Listener {

    private static final String FROG_BUCKET_NAME = "Frog Bucket";
    private static final Frog.Variant DEFAULT_VARIANT = Frog.Variant.TEMPERATE;

    private final StardustPlugin plugin;
    private final NamespacedKey frogNamespacedKey;
    private final NamespacedKey frogVariantKey;
    private final NamespacedKey frogCustomNameKey;

    public SyncFrogService(StardustPlugin plugin) {
        this.plugin = plugin;
        this.frogNamespacedKey = new NamespacedKey(plugin, "frog_data_key");
        this.frogVariantKey = new NamespacedKey(plugin, "frog_variant_key");
        this.frogCustomNameKey = new NamespacedKey(plugin, "frog_custom_name_key");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Command("frogbucket <customName> <variant> <amount>")
    @CommandDescription("Gives you a Frog bucket with the given amount!")
    @Permission("stardust.command.frogbucket")
    public void executeCommand(Player player,
                               @Quoted @Argument(value = "customName") String customName,
                               @Argument(value = "variant", suggestions = "frogVariants") String variant,
                               @Argument(value = "amount") int amount) {
        addFrogBucketToPlayer(
                player,
                amount,
                MiniMessage.miniMessage().deserialize(customName),
                frogVariant(variant),
                false
        );
    }

    @EventHandler
    public void handlePlayerInteract(PlayerInteractEvent event) {

        var player = event.getPlayer();

        var itemStack = event.getItem();
        if(itemStack == null) return;

        var clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (!isFrogInBucket(itemStack)) return;
        if (deserializeFrogData(itemStack, clickedBlock.getLocation().add(0.0, 1.0, 0.0))) {
            removeFrogBucket(player, player.getInventory().getItemInMainHand(), true);
            player.sendMessage(Component.translatable("frog-bucket-spawn-success").arguments(plugin.getPrefix()));
        } else {
            player.sendMessage(Component.translatable("frog-cannot-be-spawned").arguments(plugin.getPrefix()));
        }
    }

    @EventHandler
    public void handlePlayerPreAttack(PrePlayerAttackEntityEvent event) {

        var player = event.getPlayer();
        var entity = event.getAttacked();
        var itemInHand = player.getInventory().getItemInMainHand();

        if (isFrogInBucket(itemInHand)) return;

        if (entity instanceof Frog frog && itemInHand.getType() == Material.BUCKET) {

            var customName = entity.customName() != null ? entity.customName() : MiniMessage.miniMessage().deserialize(entity.getName());

            addFrogBucketToPlayer(
                    player,
                    1,
                    customName,
                    frog.getVariant(),
                    true
            );

            frog.setHealth(0.0);
            removeFrogBucket(player, itemInHand, false);
            event.setCancelled(true);
        }
    }

    private void removeFrogBucket(Player player, ItemStack stack, boolean giveEmptyBucket) {
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (stack.getAmount() > 1) {
            player.getInventory().setItemInMainHand(stack.subtract(1));
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        if (giveEmptyBucket) player.getInventory().addItem(new ItemStack(Material.BUCKET));
    }

    private boolean isFrogInBucket(@NotNull ItemStack stack) {

        var itemMeta = stack.getItemMeta();
        if (itemMeta == null) return false;

        var container = itemMeta.getPersistentDataContainer();

        if (!container.has(frogNamespacedKey)) return false;
        var value = container.get(frogNamespacedKey, PersistentDataType.BOOLEAN);
        if (value == null) return false;

        return value;
    }

    private boolean deserializeFrogData(ItemStack stack, Location location) {

        var frog = location.getWorld().spawn(location, Frog.class);
        if (!frog.isInWorld()) return false;

        var itemMeta = stack.getItemMeta();
        if (itemMeta == null) return false;

        var container = itemMeta.getPersistentDataContainer();
        var customName = container.get(frogCustomNameKey, PersistentDataType.STRING);
        var variant = container.get(frogVariantKey, PersistentDataType.STRING);

        if (customName != null) {
            frog.customName(MiniMessage.miniMessage().deserialize(customName));
        }

        frog.setVariant(frogVariant(variant));
        return true;
    }

    private ItemStack createFrogItemStack(int amount, Frog.Variant variant, Component customName) {

        var itemStack = new ItemStack(Material.BUCKET, amount);
        var itemMeta = itemStack.getItemMeta();

        itemMeta.displayName(MiniMessage.miniMessage().deserialize(FROG_BUCKET_NAME));
        itemMeta.addEnchant(Enchantment.EFFICIENCY, 1, false);
        itemMeta.lore(List.of(Component.text(frogVariantName(variant))));

        var container = itemMeta.getPersistentDataContainer();
        container.set(frogCustomNameKey, PersistentDataType.STRING, customName != null ? MiniMessage.miniMessage().serialize(customName) : "");
        container.set(frogNamespacedKey, PersistentDataType.BOOLEAN, true);
        container.set(frogVariantKey, PersistentDataType.STRING, frogVariantName(variant));
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    private void addFrogBucketToPlayer(Player player, int amount, Component customName, Frog.Variant variant, boolean actionBarMessage) {

        var slot = player.getInventory().firstEmpty();
        if(slot == -1) {
            player.sendMessage(Component.translatable("plugin.inventory-full").arguments(plugin.getPrefix()));
            return;
        }

        var itemStack = createFrogItemStack(amount, variant, customName);
        player.getInventory().setItem(slot, itemStack);
        var message = Component.translatable("frog-bucket-added-to-inventory").arguments(
                plugin.getPrefix(),
                Component.text(FROG_BUCKET_NAME),
                Component.text(amount),
                player.displayName()
        );

        if (!actionBarMessage) {
            player.sendMessage(message);
        } else {
            player.sendActionBar(message);
        }
    }

    @NotNull
    private Frog.Variant frogVariant(@Nullable String variant) {

        if (variant == null) return DEFAULT_VARIANT;

        var key = NamespacedKey.fromString(variant.toLowerCase());
        if (key == null) return Frog.Variant.TEMPERATE;

        var frogVariant = Registry.FROG_VARIANT.get(key);

        return frogVariant != null ? frogVariant : DEFAULT_VARIANT;
    }

    private String frogVariantName(Frog.Variant variant) {
        return variant.key().value();
    }

    @Suggestions(value = "frogVariants")
    public List<String> frogVariants(CommandContext<CommandSender> context, String input) {
        var variants = Registry.FROG_VARIANT.stream()
                .map(Frog.Variant::getKey)
                .map(NamespacedKey::toString)
                .filter(key -> key.startsWith(input.toLowerCase()))
                .toList();
        return StringUtil.copyPartialMatches(input, variants, variants);
    }
}
