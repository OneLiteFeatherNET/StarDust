package net.onelitefeather.stardust.command.commands;

import net.kyori.adventure.text.Component;
import net.onelitefeather.stardust.StardustPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SkullCommand {

    private final StardustPlugin plugin;

    public SkullCommand(StardustPlugin plugin) {
        this.plugin = plugin;
    }

    @Command("skull [name]")
    @Permission("stardust.command.skull")
    public void handleCommand(Player player, @Greedy @Argument(value = "name") String name) {
        String skullOwner = name != null ? name : player.getName();

        CompletableFuture.completedFuture(player).thenAcceptAsync(player1 -> {
            ItemStack skullItem = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();

            UUID skullOwnerId = plugin.getServer().getPlayerUniqueId(skullOwner);
            if (skullOwnerId == null) skullOwnerId = player1.getUniqueId();

            OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(skullOwnerId);
            if (!offlinePlayer.hasPlayedBefore()) {
                skullMeta.setPlayerProfile(plugin.getServer().createProfile(skullOwnerId));
            } else {
                skullMeta.setOwningPlayer(offlinePlayer);
            }

            skullItem.setItemMeta(skullMeta);
            player1.getInventory().addItem(skullItem);
            player1.sendMessage(Component.translatable("commands.skull.success").arguments(plugin.getPrefix(), Component.text(skullOwner)));
        }, Bukkit.getScheduler().getMainThreadExecutor(this.plugin));
    }
}
