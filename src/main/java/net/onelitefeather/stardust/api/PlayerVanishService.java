package net.onelitefeather.stardust.api;

import org.bukkit.entity.Player;

public interface PlayerVanishService<P extends Player> {

    /**
     * Hides a player for other players
     * @param player to be hide
     */
    void hidePlayer(P player);

    /**
     * Shows a player for other players
     * @param player to be shown to other player
     */
    void showPlayer(P player);

    /**
     * Toggle vanish status of a player
     * @param player be affected
     */
    boolean toggle(P player);

    /**
     * Check if the player in vanish
     * @return the current status
     */
    boolean isVanished(P player);

    /**
     * Set a user explicit the vanish status
     * @param player to be affected
     * @param vanished status of the user
     */
    void setVanished(P player, boolean vanished);

    /**
     * Handles if a player joining the server
     * @param player be affected
     * @return true if the player is vanished
     */
    boolean handlePlayerJoin(P player);

    /**
     * Handles if a player quits the server
     * @param player be affected
     */
    void handlePlayerQuit(P player);

    boolean canSee(P player, P target);
}
