package net.onelitefeather.stardust.service

import net.onelitefeather.stardust.command.CommandCooldown
import net.onelitefeather.stardust.user.User
import org.hibernate.SessionFactory
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.Configuration
import org.hibernate.cfg.Environment
import java.util.Properties

class DatabaseService(val jdbcUrl: String, val username: String, val password: String) {

    lateinit var sessionFactory: SessionFactory

    fun init() {
        sessionFactory = buildSessionFactory()
    }

    private fun buildSessionFactory(): SessionFactory {

        val configuration = Configuration()
        val properties = Properties()

        properties[Environment.DIALECT] = "org.hibernate.dialect.MariaDBDialect"
        properties[Environment.HBM2DDL_AUTO] = "update"
        properties[Environment.DRIVER] = "com.mysql.cj.jdbc.Driver"
        properties[Environment.USER] = username
        properties[Environment.PASS] = password
        properties[Environment.SHOW_SQL] = false
        properties[Environment.LOG_SESSION_METRICS] = false
        properties["hibernate.connection.provider_class"] = "org.hibernate.hikaricp.internal.HikariCPConnectionProvider"
        properties[Environment.URL] = jdbcUrl

        configuration.properties = properties

        configuration.addAnnotatedClass(User::class.java)
        configuration.addAnnotatedClass(CommandCooldown::class.java)

        val registry = StandardServiceRegistryBuilder().applySettings(configuration.properties).build()
        return configuration.buildSessionFactory(registry)
    }

    fun shutdown() {
        if(this::sessionFactory.isInitialized) {
            sessionFactory.close()
        }

    }
}