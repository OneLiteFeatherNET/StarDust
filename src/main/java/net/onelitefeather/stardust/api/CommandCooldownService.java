package net.onelitefeather.stardust.api;

import net.onelitefeather.stardust.command.CommandCooldown;
import net.onelitefeather.stardust.command.CooldownData;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public interface CommandCooldownService {

    /**
     * Gives back a cooldown object
     *
     * @param commandSender be sent the command
     * @param command       be sent
     * @return a cooldown object if the uuid and command inside the cache.
     */
    CommandCooldown getCommandCooldown(UUID commandSender, String command);

    /**
     * Add a command based on a sender and a time into the cache
     *
     * @param commandSender be sent the command
     * @param command       be sent
     * @param time          after the command can used again
     * @param timeUnit      is the unit to specify time for day, minutes and more
     */
    void addCommandCooldown(UUID commandSender, String command, long time, TimeUnit timeUnit);

    /**
     * Removes a command from the cache
     *
     * @param command       be removed
     * @param commandSender be used to identify
     */
    void removeCommandCooldown(UUID commandSender, String command);

    /**
     * Check if the command in the cache
     *
     * @param command       to be checked
     * @param commandSender to be used to identify
     */
    default boolean exists(UUID commandSender, String command) {
        return getCommandCooldown(commandSender, command) != null;
    }

    /**
     * Check if the cooldown over for a command
     *
     * @param command       to be checked
     * @param commandSender to be used to identify
     */
    default boolean isCooldownOver(UUID commandSender, String command) {
        return Optional.ofNullable(getCommandCooldown(commandSender, command))
                .map(CommandCooldown::isOver)
                .orElse(true);
    }

    /**
     * Check of the command is still in cache
     *
     * @param commandOrLabel checks if inside the cache
     */
    default boolean hasCommandCooldown(String commandOrLabel) {
        return getCooldownData(commandOrLabel) != null;
    }

    /**
     * Gets a list of all cooldown elements
     *
     * @return a list of objects
     */
    List<CooldownData> getCooldownDataList();

    /**
     * Get a cooldown data based on the command
     *
     * @return a cooldown object or null if the command not in the cache
     */
    default CooldownData getCooldownData(String command) {
        return getCooldownDataList().stream()
                .filter(cooldownData -> cooldownData.getCommandName().equalsIgnoreCase(command))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get the milli time in the future based on a unit and a time indicator
     */
    default long getCooldownTime(TimeUnit timeUnit, long time) {
        return System.currentTimeMillis() + switch (timeUnit) {
            case TimeUnit.DAYS -> 1000 * 60 * 60 * 24 * time;
            case TimeUnit.HOURS -> 1000 * 60 * 60 * time;
            case TimeUnit.MINUTES -> 1000 * 60 * time;
            case TimeUnit.SECONDS -> 1000 * time;
            default -> throw new IllegalArgumentException(
                    "The TimeUnit " + timeUnit.toString().toLowerCase() + " is not allowed here"
            );
        };
    }
}
