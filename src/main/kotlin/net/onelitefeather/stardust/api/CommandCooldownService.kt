package net.onelitefeather.stardust.api

import net.onelitefeather.stardust.command.CommandCooldown
import java.util.UUID
import java.util.concurrent.TimeUnit

interface CommandCooldownService {

    fun getCommandCooldown(commandSender: UUID, command: String): CommandCooldown?

    fun addCommandCooldown(commandSender: UUID, command: String, timeUnit: TimeUnit, time: Long)

    fun removeCommandCooldown(commandSender: UUID, command: String)

    fun exists(commandSender: UUID, command: String): Boolean

    fun isCooldownOver(commandSender: UUID, command: String): Boolean

    fun hasCommandCooldown(commandLabel: String): Boolean

    fun getCooldownTime(timeUnit: TimeUnit, time: Long): Long {
        return when (timeUnit) {
            TimeUnit.DAYS -> 1000 * 60 * 60 * 24 * time
            TimeUnit.HOURS -> 1000 * 60 * 60 * time
            TimeUnit.MINUTES -> 1000 * 60 * time
            else -> time
        }
    }
}