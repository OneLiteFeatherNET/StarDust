package net.onelitefeather.stardust.service

import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.api.PlayerVanishService
import net.onelitefeather.stardust.tasks.UserTask
import net.onelitefeather.stardust.user.User
import net.onelitefeather.stardust.user.UserProperty
import net.onelitefeather.stardust.user.UserPropertyType
import net.onelitefeather.stardust.user.getDefaultUserProperties
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import org.hibernate.HibernateException
import org.hibernate.Transaction
import java.util.*
import java.util.function.Consumer
import java.util.logging.Level

class UserService(private val stardustPlugin: StardustPlugin) {

    lateinit var userTask: UserTask
    lateinit var bukkitUserTask: BukkitTask
    lateinit var playerVanishService: PlayerVanishService<Player>

    fun startUserTask() {
        userTask = UserTask(stardustPlugin)
        playerVanishService = BukkitPlayerVanishService(stardustPlugin, this)
        bukkitUserTask = stardustPlugin.server.scheduler.runTaskTimerAsynchronously(stardustPlugin, userTask, 0L, 20L)
    }

    fun stopUserTask() {
        if (this::bukkitUserTask.isInitialized) {
            bukkitUserTask.cancel()
        }
    }

    fun getUsers(): List<User> {
        try {
            stardustPlugin.databaseService.sessionFactory.openSession().use { session ->
                val query = session.createQuery("SELECT u FROM User u", User::class.java)
                return query.list()
            }
        } catch (e: HibernateException) {
            stardustPlugin.logger.log(Level.SEVERE, "Could not load users.", e)
        }

        return emptyList()
    }

    fun getUser(uuid: UUID): User? {
        try {
            stardustPlugin.databaseService.sessionFactory.openSession().use {
                val query = it.createQuery("SELECT u FROM User u JOIN FETCH u.properties WHERE u.uuid = :uuid", User::class.java)
                query.setParameter("uuid", uuid.toString())
                return query.uniqueResult()
            }
        } catch (e: HibernateException) {
            stardustPlugin.logger.log(Level.SEVERE, "Could not find an user by the given uuid $uuid", e)
        }

        return null
    }

    fun getUser(name: String): User? {
        try {
            stardustPlugin.databaseService.sessionFactory.openSession().use {
                val query = it.createQuery("SELECT u FROM User u WHERE u.name = :name", User::class.java)
                query.setParameter("name", name)
                return query.uniqueResult()
            }
        } catch (e: HibernateException) {
            stardustPlugin.logger.log(Level.SEVERE, "Could not find an user by the given name $name", e)
        }

        return null
    }

    fun registerUser(player: Player, consumer: Consumer<User>) {

        val uuid = player.uniqueId
        val name = player.name

        if (!isUserCreated(uuid)) {

            var transaction: Transaction? = null

            try {
                stardustPlugin.databaseService.sessionFactory.openSession().use { session ->

                    transaction = session.beginTransaction()

                    val user = User(null, uuid.toString(), name, getDefaultUserProperties())
                    user.properties.forEach { session.persist(it.copy(user = user)) }
                    session.persist(user)
                    transaction?.commit()
                    consumer.accept(user)
                }
            } catch (e: HibernateException) {
                transaction?.rollback()
            }
        }
    }

    fun isUserCreated(uuid: UUID): Boolean = getUser(uuid) != null

    fun updateUser(user: User): User? {
        var transaction: Transaction? = null
        try {
            stardustPlugin.databaseService.sessionFactory.openSession().use { session ->
                transaction = session.beginTransaction()

                val existUser = isUserCreated(user.getUniqueId())
                if (!existUser) {
                    session.persist(user)
                } else {
                    session.merge(user)
                }

                transaction?.commit()
                stardustPlugin.logger.info("Successfully ${if (!existUser) "created" else "updated"} User ${user.getUniqueId()} (${user.name})")
            }
        } catch (e: HibernateException) {
            stardustPlugin.logger.log(Level.SEVERE, "Something went wrong!", e)
            transaction?.rollback()
        }

        return getUser(user.getUniqueId())
    }

    fun deleteUser(uuid: UUID, consumer: Consumer<Boolean>) {

        val cachedUser = getUser(uuid)

        if (cachedUser == null) {
            consumer.accept(false)
            return
        }

        var transaction: Transaction? = null
        try {
            stardustPlugin.databaseService.sessionFactory.openSession().use { session ->
                transaction = session.beginTransaction()
                cachedUser.properties.forEach { session.remove(it) }
                session.remove(cachedUser)
                transaction?.commit()
                consumer.accept(true)
            }
        } catch (e: HibernateException) {
            consumer.accept(false)
            stardustPlugin.logger.log(Level.SEVERE, "Something went wrong!", e)
            transaction?.rollback()
        }
    }

    fun setUserProperty(user: User, type: UserPropertyType, value: Any) {

        var transaction: Transaction? = null
        try {
            stardustPlugin.databaseService.sessionFactory.openSession().use { session ->
                transaction = session.beginTransaction()
                val userProperty = user.getProperty(type)
                if (userProperty == null) {
                    session.persist(UserProperty(null, type.name.lowercase(), value.toString(), type.type))
                } else {
                    session.merge(userProperty.copy(value = value.toString()))
                }

                transaction?.commit()
            }
        } catch (e: HibernateException) {
            transaction?.rollback()
            stardustPlugin.logger.log(Level.SEVERE, "Cannot set user property $type for user ${user.name}", e)
        }
    }
}