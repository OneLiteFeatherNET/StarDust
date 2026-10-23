package net.onelitefeather.stardust;

import net.onelitefeather.stardust.util.ThreadHelper;
import org.hibernate.SessionFactory;
import net.onelitefeather.stardust.persistence.GsonFormatMapper;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.MappingSettings;

import java.util.Optional;
import java.util.Properties;

public class DatabaseConnectionService implements ThreadHelper {

    private SessionFactory factory;

    public DatabaseConnectionService(StardustPlugin plugin) {
        syncThreadForServiceLoader(() -> {
            factory = new Configuration()
                    .configure()
                    .configure(plugin.getDataFolder().toPath().resolve("hibernate.cfg.xml").toFile())
                    .addProperties(jsonProperties())
                    .buildSessionFactory();
        });
    }

    private static Properties jsonProperties() {
        // an instance (not a class name) avoids plugin classloader lookups inside Hibernate
        Properties properties = new Properties();
        properties.put(MappingSettings.JSON_FORMAT_MAPPER, new GsonFormatMapper());
        return properties;
    }

    public Optional<SessionFactory> getSessionFactory() {
        return Optional.ofNullable(this.factory);
    }

    public void close() {
        if (factory == null) return;
        factory.close();
    }
}
