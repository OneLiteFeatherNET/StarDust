package net.onelitefeather.stardust.api;

import java.util.UUID;

public interface PlayerVanishService {

    /**
     * Hides a player for other players
     *
     * @param playerId to be hidden
     */
    void hidePlayer(UUID playerId);

    /**
     * Shows a player for other players
     *
     * @param playerId to be shown to other players
     */
    void showPlayer(UUID playerId);

    /*
     * @param playerId to be affected
     * @return new vanish state
     */
    boolean toggle(UUID playerId);

    /**
     * Check if the player is vanished
     *
     * @return the current status
     */
    boolean isVanished(UUID playerId);

    /**
     * Set a user explicit the vanish status
     *
     * @param playerId  to be affected
     * @param vanished  status of the user
     */
    void setVanished(UUID playerId, boolean vanished);

    /**
     * Handles if a player joining the server
     *
     * @param playerId to be affected
     * @return true if the player is vanished
     */
    boolean handlePlayerJoin(UUID playerId);

    /**
     * Handles if a player quits the server
     *
     * @param playerId to be affected
     */
    void handlePlayerQuit(UUID playerId);

    boolean canSee(UUID viewerId, UUID targetId);

    boolean isVanishPermitted(UUID playerId);
}
