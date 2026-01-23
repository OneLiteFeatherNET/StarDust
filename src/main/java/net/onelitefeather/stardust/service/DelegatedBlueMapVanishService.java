package net.onelitefeather.stardust.service;

import de.bluecolored.bluemap.api.BlueMapAPI;
import net.onelitefeather.stardust.api.PlayerVanishService;

import java.util.UUID;

public record DelegatedBlueMapVanishService(PlayerVanishService delegate) implements PlayerVanishService {

    @Override
    public void hidePlayer(UUID playerId) {
        this.delegate.hidePlayer(playerId);
    }

    @Override
    public void showPlayer(UUID playerId) {
        this.delegate.showPlayer(playerId);
    }

    @Override
    public boolean toggle(UUID playerId) {
        return this.delegate.toggle(playerId);
    }

    @Override
    public boolean isVanished(UUID playerId) {
        return this.delegate.isVanished(playerId);
    }

    @Override
    public void setVanished(UUID playerId, boolean vanished) {
        this.delegate.setVanished(playerId, vanished);
        BlueMapAPI.getInstance().map(BlueMapAPI::getWebApp).ifPresent(api -> api.setPlayerVisibility(playerId, !vanished));
    }

    @Override
    public boolean handlePlayerJoin(UUID playerId) {
        return this.delegate.handlePlayerJoin(playerId);
    }

    @Override
    public void handlePlayerQuit(UUID playerId) {
        this.delegate.handlePlayerQuit(playerId);
    }

    @Override
    public boolean canSee(UUID playerId, UUID targetId) {
        return this.delegate.canSee(playerId, targetId);
    }

    @Override
    public boolean isVanishPermitted(UUID playerId) {
        return this.delegate.isVanishPermitted(playerId);
    }
}
