package net.onelitefeather.stardust.service;

import de.bluecolored.bluemap.api.BlueMapAPI;
import net.onelitefeather.stardust.api.PlayerVanishService;
import org.bukkit.entity.Player;

public final class DelegatedBlueMapVanishService implements PlayerVanishService<Player> {

    private final PlayerVanishService<Player> delegate;

    public DelegatedBlueMapVanishService(PlayerVanishService<Player> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void hidePlayer(Player player) {
        this.delegate.hidePlayer(player);
    }

    @Override
    public void showPlayer(Player player) {
        this.delegate.showPlayer(player);
    }

    @Override
    public boolean toggle(Player player) {
        return this.delegate.toggle(player);
    }

    @Override
    public boolean isVanished(Player player) {
        return this.delegate.isVanished(player);
    }

    @Override
    public void setVanished(Player player, boolean vanished) {
        BlueMapAPI.getInstance().map(BlueMapAPI::getWebApp).ifPresent(api -> api.setPlayerVisibility(player.getUniqueId(), !vanished));
        this.delegate.setVanished(player, vanished);
    }

    @Override
    public boolean handlePlayerJoin(Player player) {
        return this.delegate.handlePlayerJoin(player);
    }

    @Override
    public void handlePlayerQuit(Player player) {
        this.delegate.handlePlayerQuit(player);
    }

    @Override
    public boolean canSee(Player player, Player target) {
        return this.delegate.canSee(player, target);
    }

    @Override
    public boolean isVanishPermitted(Player player) {
        return this.delegate.isVanishPermitted(player);
    }
}
