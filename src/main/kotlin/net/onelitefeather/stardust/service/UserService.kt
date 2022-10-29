package net.onelitefeather.stardust.service

import io.sentry.Sentry
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.api.IUser
import net.onelitefeather.stardust.tasks.UserTask
import net.onelitefeather.stardust.user.User
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import org.hibernate.HibernateException
import org.hibernate.Transaction
import java.util.UUID
import java.util.function.Consumer
import java.util.logging.Level

class UserService(val stardustPlugin: StardustPlugin) {

    lateinit var userTask: UserTask
    lateinit var bukkitUserTask: BukkitTask

    fun startUserTask() {
        userTask = UserTask(stardustPlugin)
        bukkitUserTask = stardustPlugin.server.scheduler.runTaskTimerAsynchronously(stardustPlugin, userTask, 0L, 20L)
    }

    fun stopUserTask() {
        if (this::bukkitUserTask.isInitialized) {
            bukkitUserTask.cancel()
        }
    }

    fun getUsers(): List<IUser> {
        try {
            stardustPlugin.databaseService.sessionFactory.openSession().use { session ->
                val query = session.createQuery("SELECT u FROM User u", User::class.java)
                return query.list()
            }
        } catch (e: HibernateException) {
            stardustPlugin.logger.log(Level.SEVERE, "Could not load users.", e)
            Sentry.captureException(e)
        }

        return emptyList()
    }

    fun getUser(uuid: UUID): IUser? {
        try {
            stardustPlugin.databaseService.sessionFactory.openSession().use {
                val query = it.createQuery("SELECT u FROM User u WHERE u.uuid = :uuid", User::class.java)
                query.setParameter("uuid", uuid.toString())
                return query.uniqueResult()
            }
        } catch (e: HibernateException) {
            stardustPlugin.logger.log(Level.SEVERE, "Could not find an user by the given uuid $uuid", e)
            Sentry.captureException(e)
        }

        return null
    }

    fun getUser(name: String): IUser? {
        try {
            stardustPlugin.databaseService.sessionFactory.openSession().use {
                val query = it.createQuery("SELECT u FROM User u WHERE u.lastKnownName = :name", User::class.java)
                query.setParameter("name", name)
                return query.uniqueResult()
            }
        } catch (e: HibernateException) {
            stardustPlugin.logger.log(Level.SEVERE, "Could not find an user by the given name $name", e)
            Sentry.captureException(e)
        }

        return null
    }

    fun registerUser(player: Player, consumer: Consumer<IUser>) {

        val uuid = player.uniqueId
        val name = player.name

        if (!isUserCreated(uuid)) {
            val user = User(null, uuid.toString(), name, vanished = false, flying = false)
            updateUser(user)
            consumer.accept(user)
        }
    }

    fun isUserCreated(uuid: UUID): Boolean = getUser(uuid) != null

    fun updateUser(user: IUser) {
        var transaction: Transaction? = null
        try {
            stardustPlugin.databaseService.sessionFactory.openSession().use { session ->
                if (user is User) {
                    transaction = session.beginTransaction()
                    val existUser = isUserCreated(user.getUniqueId())
                    if (!existUser) session.persist(user) else session.merge(user)
                    transaction?.commit()
                    stardustPlugin.logger.info("Successfully ${if (!existUser) "created" else "updated"} User ${user.getUniqueId()} (${user.getName()})")
                }
            }
        } catch (e: HibernateException) {
            stardustPlugin.logger.log(Level.SEVERE, "Something went wrong!", e)
            transaction?.rollback()
        }
    }

    fun deleteUser(uuid: UUID, consumer: Consumer<Boolean>) {

        val cachedUser = getUser(uuid)
        var success: Boolean
        var transaction: Transaction? = null
        try {
            stardustPlugin.databaseService.sessionFactory.openSession().use { session ->
                transaction = session.beginTransaction()
                session.remove(cachedUser)
                transaction?.commit()
                success = true
            }
        } catch (e: HibernateException) {
            success = false
            stardustPlugin.logger.log(Level.SEVERE, "Something went wrong!", e)
            transaction?.rollback()
        }

        consumer.accept(success)
    }

    fun toggleVanish(user: IUser): Boolean {

        val base = user.getBase() ?: return false
        if (!user.isVanished()) {
            user.setVanished(true)
            hidePlayer(base)
        } else {
            user.setVanished(false)
            stardustPlugin.server.onlinePlayers.forEach { player ->
                player.showPlayer(stardustPlugin, base)
            }
        }

        base.isGlowing = user.isVanished()
        return user.isVanished()
    }

    fun hidePlayer(toHide: Player) {
        stardustPlugin.server.onlinePlayers.forEach { players: Player ->
            if (stardustPlugin.luckPermsService.getGroupPriority(toHide) > stardustPlugin.luckPermsService.getGroupPriority(
                    players
                )
            ) {
                players.hidePlayer(stardustPlugin, toHide)
            }
        }
    }
}