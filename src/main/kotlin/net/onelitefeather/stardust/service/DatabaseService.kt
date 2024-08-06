package net.onelitefeather.stardust.service

import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.command.CommandCooldown
import net.onelitefeather.stardust.user.User
import net.onelitefeather.stardust.user.UserProperties
import net.onelitefeather.stardust.user.UserProperty
import net.onelitefeather.stardust.util.ThreadHelper
import org.bukkit.Bukkit
import org.hibernate.SessionFactory
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.Configuration
import org.hibernate.cfg.Environment
import org.hibernate.dialect.MariaDBDialect
import org.hibernate.hikaricp.internal.HikariCPConnectionProvider
import org.hibernate.tool.schema.Action
import java.util.Properties

class DatabaseService(val stardustPlugin: StardustPlugin) : ThreadHelper {

    lateinit var sessionFactory: SessionFactory

    init {
        syncThreadForServiceLoader {
            try {
                sessionFactory = Configuration().configure().configure(stardustPlugin.dataFolder.toPath().resolve("hibernate.cfg.xml").toFile()).buildSessionFactory()
            } catch (e: Exception) {
                Bukkit.getPluginManager().disablePlugin(stardustPlugin)
            }
        }
    }

    fun shutdown() {
        if (this::sessionFactory.isInitialized) {
            sessionFactory.close()
        }

    }
}