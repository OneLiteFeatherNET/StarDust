package net.onelitefeather.stardust.api;

import net.onelitefeather.stardust.api.player.StardustPlayer;
import net.onelitefeather.stardust.service.UserService;
import net.onelitefeather.stardust.user.User;

import java.util.List;
import java.util.UUID;

public interface StardustPlayerService {

    /**
     * Retrieves a list of all Stardust players, limited to the specified number.
     *
     * @param limit the maximum number of players to retrieve
     * @return a list of Stardust players
     */
    List<StardustPlayer> findAll(int limit);

    /**
     * Retrieves a list of all online Stardust players.
     *
     * @return a list of online Stardust players
     */
    List<StardustPlayer> findOnlinePlayers();

    /**
     * Finds a Stardust player by their unique identifier (UUID).
     *
     * @param playerUuid the UUID of the player to find
     * @return the Stardust player with the specified UUID, or null if not found
     */
    StardustPlayer findById(UUID playerUuid);

    /**
     * Saves or updates a Stardust player in the database.
     *
     * @param player the Stardust player to save or update
     */
    void saveOrUpdate(StardustPlayer player);

    /**
     * Deletes a Stardust player from the database.
     *
     * @param player the Stardust player to delete
     */
    void delete(StardustPlayer player);

    /**
     * Checks if a player with the specified UUID exists in the database.
     *
     * @param playerUuid the UUID of the player to check
     * @return true if the player exists, false otherwise
     */
    boolean playerExists(UUID playerUuid);


    /**
     * Migrates a user to a Stardust player.
     *
     * @param user the user to migrate
     * @param userService the user service to use for migration
     */
    @Deprecated(forRemoval = true, since = "1.15.4")
    void migrateUser(User user, UserService userService);
}
