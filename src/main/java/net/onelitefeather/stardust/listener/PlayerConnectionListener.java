package net.onelitefeather.stardust.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.onelitefeather.stardust.FeatherEssentials;
import net.onelitefeather.stardust.api.user.IUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public record PlayerConnectionListener(FeatherEssentials featherEssentials) implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {

        //TODO: Performance boost
        Player player = event.getPlayer();

        boolean[] vanished = {false};

        this.featherEssentials.getUserManager().loadUser(player, user -> {

            String displayName = this.featherEssentials.getVaultHook().getPlayerDisplayName(player);

            if (user != null) {

                vanished[0] = user.isVanished();
                player.displayName(this.featherEssentials.getVaultHook().getDisplayName(player));

                user.setDisplayName(LegacyComponentSerializer.legacySection().serialize(player.displayName()));
                user.checkCanFly();

                player.setGameMode(player.hasPermission("featheressentials.join.gamemode") ? player.getGameMode() : player.getServer().getDefaultGameMode());
            } else {

                //Register a new User
                this.featherEssentials.getUserManager().registerUser(player, register -> player.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("plugin.first-join", this.featherEssentials.getPrefix(), displayName))));
            }

        });

        event.joinMessage(vanished[0] ? null : MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("listener.join-message")).append(Component.text(" ").append(player.displayName())));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {

        IUser user = this.featherEssentials.getUserManager().getUser(event.getPlayer().getUniqueId());
        boolean vanished = false;

        if (user != null) {
            vanished = user.isVanished();
            this.featherEssentials.getUserManager().updateUser(user, true);
        }

        event.quitMessage(vanished ? null : MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("listener.quit-message")).append(Component.text(" ")).append(this.featherEssentials.getVaultHook().getDisplayName(event.getPlayer())));
    }
}
