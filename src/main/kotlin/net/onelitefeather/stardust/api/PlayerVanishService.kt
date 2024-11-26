package net.onelitefeather.stardust.api

import org.bukkit.entity.Player

/**
 * API Class to handle player vanishing
 * @author TheMeinerLP
 */
interface PlayerVanishService<P : Player> {

    /**
     * Hides a player for other players
     * @param player to be hide
     */
    fun hidePlayer(player: P)

    /**
     * Shows a player for other players
     * @param player to be shown to other player
     */
    fun showPlayer(player: P)

    /**
     * Toggle vanish status of a player
     * @param player be affected
     */
    fun toggle(player: P): Boolean

    /**
     * Check if the player in vanish
     * @return the current status
     */
    fun isVanished(player: P): Boolean

    /**
     * Set a user explicit the vanish status
     * @param vanished status of the user
     * @param player to be affected
     */
    fun setVanished(player: P, vanished: Boolean)

    /**
     * Handles if a player joining the server
     * @param player be affected
     * @return true if the player is vanished
     */
    fun handlePlayerJoin(player: P): Boolean

    /**
     * Handles if a player quits the server
     * @param player be affected
     */
    fun handlePlayerQuit(player: P)
}
