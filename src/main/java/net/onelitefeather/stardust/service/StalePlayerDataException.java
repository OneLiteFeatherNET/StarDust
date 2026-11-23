package net.onelitefeather.stardust.service;

import java.util.UUID;

/**
 * Thrown when a player is written with a version that is no longer the stored version, i.e. the
 * stored data was changed by another write (for example on another server) after the copy was loaded.
 */
public class StalePlayerDataException extends RuntimeException {

    private final UUID playerUuid;
    private final long modelVersion;
    private final long storedVersion;

    /**
     * @param playerUuid    the player whose write was rejected
     * @param modelVersion  the version the rejected copy was loaded with
     * @param storedVersion the version currently stored, or -1 if it is not known
     */
    public StalePlayerDataException(UUID playerUuid, long modelVersion, long storedVersion) {
        super("Stale data for player " + playerUuid + ": model version " + modelVersion + ", stored version " + storedVersion);
        this.playerUuid = playerUuid;
        this.modelVersion = modelVersion;
        this.storedVersion = storedVersion;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public long getModelVersion() {
        return modelVersion;
    }

    public long getStoredVersion() {
        return storedVersion;
    }
}
