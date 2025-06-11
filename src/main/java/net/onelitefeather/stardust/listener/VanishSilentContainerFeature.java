package net.onelitefeather.stardust.listener;

import net.onelitefeather.stardust.StardustPlugin;
import org.bukkit.GameMode;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.EnderChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class VanishSilentContainerFeature implements Listener {

    private final StardustPlugin stardustPlugin;
    private final Map<Player, Inventory> silentContainerLooter = new HashMap<>();

    public VanishSilentContainerFeature(StardustPlugin stardustPlugin) {
        this.stardustPlugin = stardustPlugin;
    }

    @EventHandler
    public void handleInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) silentContainerLooter.remove(player);
    }

    @EventHandler
    public void handleInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player whoClicked)) return;
        Inventory clickedInventory = event.getInventory();

        if (stardustPlugin.getUserService().getVanishService().isVanished(whoClicked)) {
            Inventory inventory = silentContainerLooter.get(whoClicked);
            if (inventory == null) return;
            boolean canInteract = whoClicked.hasPermission("stardust.vanish.silentopen.interact");
            if (clickedInventory.equals(inventory) && !canInteract) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void handlePlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getClickedBlock() == null) return;
        BlockState blockState = event.getClickedBlock().getState();

        boolean hasPermission = player.hasPermission("stardust.vanish.silentopen");
        boolean vanished = stardustPlugin.getUserService().getVanishService().isVanished(player);

        if (blockState instanceof EnderChest) {
            boolean useInteractBlock = vanished && !player.isSneaking() || player.isSneaking();

            if (vanished && player.isSneaking()) {
                openContainer(player, player.getEnderChest());
            }

            event.setCancelled(useInteractBlock);
            return;
        }

        if (!(blockState instanceof Container)) return;
        if (vanished && event.getAction().isRightClick()) {
            if (hasPermission && player.isSneaking()) {
                openContainer(player, ((Container) blockState).getInventory());
            } else {
                event.setCancelled(true);
            }
        }
    }

    private void openContainer(Player player, Inventory inventory) {
        silentContainerLooter.putIfAbsent(player, inventory);

        // Prevent double opening for EnderChest
        if (inventory == player.getEnderChest()) {
            player.openInventory(inventory);
        }

        player.setVelocity(player.getVelocity().setY(player.getLocation().getBlockY() + 1.5));
        player.setGameMode(GameMode.SPECTATOR);

        player.getServer().getScheduler().getMainThreadExecutor(stardustPlugin).execute(() -> {
            GameMode previousGameMode = player.getPreviousGameMode() != null
                    ? player.getPreviousGameMode()
                    : player.getServer().getDefaultGameMode();
            player.setGameMode(previousGameMode);
        });
    }
}
