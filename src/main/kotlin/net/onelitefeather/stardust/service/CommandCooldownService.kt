package net.onelitefeather.stardust.service

import io.sentry.Sentry
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.command.CommandCooldown
import org.hibernate.HibernateException
import org.hibernate.Transaction
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.logging.Level

class CommandCooldownService(private val stardustPlugin: StardustPlugin) {

    fun getCommandCooldown(commandSender: UUID, command: String): CommandCooldown? {
        try {
            stardustPlugin.databaseService.sessionFactory.openSession().use { session ->

                val query = session.createQuery(
                    "SELECT cc FROM CommandCooldown cc WHERE cc.commandSender = :commandSender AND cc.command = :command",
                    CommandCooldown::class.java
                )

                query.setParameter("commandSender", commandSender.toString())
                query.setParameter("command", command)
                return query.uniqueResult()
            }
        } catch (e: HibernateException) {
            stardustPlugin.logger.log(
                Level.SEVERE,
                "Could not get command cooldown by the given sender $commandSender and command $command",
                e
            )
            Sentry.captureException(e)
        }

        return null
    }

    fun addCommandCooldown(commandSender: UUID, command: String, timeUnit: TimeUnit, time: Long) {

        var transaction: Transaction? = null
        try {
            stardustPlugin.databaseService.sessionFactory.openSession().use { session ->

                transaction = session.beginTransaction()
                var commandCooldown = getCommandCooldown(commandSender, command)
                val executedAt = System.currentTimeMillis() + getCooldownTime(timeUnit, time)

                if (commandCooldown != null) {
                    session.merge(
                        commandCooldown.copy(
                            commandSender = commandSender.toString(),
                            command = command,
                            executedAt = executedAt
                        )
                    )
                } else {
                    commandCooldown = CommandCooldown(
                        null,
                        commandSender.toString(),
                        command,
                        executedAt
                    )
                    session.persist(commandCooldown)
                }

                transaction?.commit()
            }
        } catch (e: HibernateException) {
            stardustPlugin.logger.log(Level.SEVERE, "Could not add command cooldown", e)
            Sentry.captureException(e)
            if (transaction != null) {
                transaction?.rollback()
            }
        }
    }

    fun removeCommandCooldown(commandSender: UUID, command: String) {
        var transaction: Transaction? = null
        try {
            stardustPlugin.databaseService.sessionFactory.openSession().use { session ->
                transaction = session.beginTransaction()
                val commandCooldown = getCommandCooldown(commandSender, command)
                session.remove(commandCooldown)
                transaction?.commit()
            }
        } catch (e: HibernateException) {
            stardustPlugin.logger.log(Level.SEVERE, "Could not remove command cooldown", e)
            Sentry.captureException(e)
            if (transaction != null) {
                transaction?.rollback()
            }
        }
    }

    fun exists(commandSender: UUID, command: String): Boolean = getCommandCooldown(commandSender, command) != null

    fun isCooldownOver(commandSender: UUID, command: String): Boolean = getCommandCooldown(commandSender, command)?.isOver() == true

    fun getCooldownTime(timeUnit: TimeUnit, time: Long): Long {
        return when (timeUnit) {
            TimeUnit.DAYS -> 1000 * 60 * 60 * 24 * time
            TimeUnit.HOURS -> 1000 * 60 * 60 * time
            TimeUnit.MINUTES -> 1000 * 60 * time
            else -> time
        }
    }

    fun hasCommandCooldown(commandLabel: String): Boolean {
        return false
    }
}