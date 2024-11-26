package net.onelitefeather.stardust.api

import net.onelitefeather.stardust.command.CommandCooldown
import net.onelitefeather.stardust.command.CooldownData
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * API class for handle command cooldown's
 * @author TheMeinerLP
 */
interface CommandCooldownService {

    /**
     * Gives back a cooldown object
     * @param commandSender be sent the command
     * @param command be sent
     * @return a cooldown object if the uuid and command inside the cache.
     */
    fun getCommandCooldown(commandSender: UUID, command: String): CommandCooldown?

    /**
     * Add a command based on a sender and a time into the cache
     * @param commandSender be sent the command
     * @param command be sent
     * @param time after the command can used again
     * @param timeUnit is the unit to specify time for day, minutes and more
     */
    fun addCommandCooldown(commandSender: UUID, command: String, timeUnit: TimeUnit, time: Long)

    /**
     * Removes a command from the cache
     * @param command be removed
     * @param commandSender be used to identify
     */
    fun removeCommandCooldown(commandSender: UUID, command: String)

    /**
     * Check if the command in the cache
     * @param command to be checked
     * @param commandSender to be used to identify
     */
    fun exists(commandSender: UUID, command: String): Boolean

    /**
     * Check if the cooldown over for a command
     * @param command to be checked
     * @param commandSender to be used to identify
     */
    fun isCooldownOver(commandSender: UUID, command: String): Boolean

    /**
     * Check of the command is still in cache
     * @param commandOrLabel checks if inside the cache
     */
    fun hasCommandCooldown(commandOrLabel: String): Boolean

    /**
     * Gets a list of all cooldown elements
     * @return a list of objects
     */
    fun getCooldownDataList(): List<CooldownData>

    /**
     * Get a cooldown data based on the command
     * @return a cooldown object or null if the command not in the cache
     */
    fun getCooldownData(commandName: String): CooldownData?

    /**
     * Get the milli time in the future based on a unit and a time indicator
     */
    fun getCooldownTime(timeUnit: TimeUnit, time: Long): Long {
        return System.currentTimeMillis() + when (timeUnit) {
            TimeUnit.DAYS -> 1000 * 60 * 60 * 24 * time
            TimeUnit.HOURS -> 1000 * 60 * 60 * time
            TimeUnit.MINUTES -> 1000 * 60 * time
            TimeUnit.SECONDS -> 1000 * time
            else -> throw IllegalArgumentException(
                "The TimeUnit " + timeUnit.name.lowercase() + " is not allowed here"
            )
        }
    }
}