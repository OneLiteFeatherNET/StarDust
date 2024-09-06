package net.onelitefeather.stardust.service

import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.util.ThreadHelper
import org.bukkit.Bukkit
import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration

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