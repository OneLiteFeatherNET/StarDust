package net.onelitefeather.stardust.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.onelitefeather.stardust.StardustPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;

public class PlayerChatListener implements Listener {

    private final StardustPlugin plugin;
    private final NamespacedKey chatConfirmationKey;

    public PlayerChatListener(StardustPlugin plugin) {
        this.plugin = plugin;
        this.chatConfirmationKey = new NamespacedKey(plugin, "chat_confirmation");
    }

    @EventHandler
    public void onAsyncChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        var stardustPlayer = plugin.getPlayerService().findById(player.getUniqueId());

        if (stardustPlayer != null && stardustPlayer.isVanished()) {
            if (!hasChatConfirmation(player, chatConfirmationKey)) {
                confirmChatMessage(player, chatConfirmationKey, true);
                event.setCancelled(true);
                player.sendMessage(Component.translatable("vanish.confirm-chat-message")
                        .arguments(plugin.getPrefix()));
            } else {
                confirmChatMessage(player, chatConfirmationKey, false);
            }
        }
    }

    private void confirmChatMessage(Player player, NamespacedKey key, Boolean value) {
        player.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, value);
    }

    private boolean hasChatConfirmation(Player player, NamespacedKey key) {

        //Remove old data from the persistent container.
        var container = player.getPersistentDataContainer();
        if (container.has(key, PersistentDataType.INTEGER)) {
            container.remove(key);
            return false;
        }

        var value = container.get(key, PersistentDataType.BOOLEAN);
        if (value == null) return false;

        return value;
    }

}
