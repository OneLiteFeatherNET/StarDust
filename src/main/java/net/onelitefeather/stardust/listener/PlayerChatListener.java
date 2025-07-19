package net.onelitefeather.stardust.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.onelitefeather.stardust.StardustPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

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
        var user = plugin.getUserService().getUser(player.getUniqueId());

        if (user != null && user.isVanished()) {
            if (!user.hasChatConfirmation(chatConfirmationKey)) {
                user.confirmChatMessage(chatConfirmationKey, true);
                event.setCancelled(true);
                player.sendMessage(Component.translatable("vanish.confirm-chat-message")
                        .arguments(plugin.getPrefix()));
            } else {
                user.confirmChatMessage(chatConfirmationKey, false);
            }
        }
    }
}
