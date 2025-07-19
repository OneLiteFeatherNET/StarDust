package net.onelitefeather.stardust;

import net.onelitefeather.stardust.util.ThreadHelper;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.Optional;

public class DatabaseConnectionService implements ThreadHelper {

    private SessionFactory factory;

    public DatabaseConnectionService(StardustPlugin plugin) {
        syncThreadForServiceLoader(() -> {
            factory = new Configuration()
                    .configure()
                    .configure(plugin.getDataFolder().toPath().resolve("hibernate.cfg.xml").toFile())
                    .buildSessionFactory();
        });
    }

    public Optional<SessionFactory> getSessionFactory() {
        return Optional.ofNullable(this.factory);
    }

    public void close() {
        if (factory == null) return;
        factory.close();
    }
}
