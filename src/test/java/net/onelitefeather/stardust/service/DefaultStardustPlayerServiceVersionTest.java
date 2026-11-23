package net.onelitefeather.stardust.service;

import net.onelitefeather.stardust.api.player.StardustPlayer;
import net.onelitefeather.stardust.api.player.setting.PlayerSetting;
import net.onelitefeather.stardust.persistence.GsonFormatMapper;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.MappingSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DefaultStardustPlayerServiceVersionTest {

    private static final Path CONFIG_PATH = Path.of("src/test/resources/test-connection.cfg.xml");
    private static final UUID PLAYER_ID = UUID.fromString("df7c66a6-7876-44c2-9abd-9bd33f7d3d9e");
    private static final Instant FIXED_TIME = Instant.EPOCH;

    private SessionFactory sessionFactory;
    private DefaultStardustPlayerService service;

    @BeforeEach
    void openFreshDatabase() {
        Properties properties = new Properties();
        properties.put(MappingSettings.JSON_FORMAT_MAPPER, new GsonFormatMapper());
        sessionFactory = new Configuration()
                .configure()
                .configure(CONFIG_PATH.toFile())
                .setProperty("connection.url", "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1;MODE=PostgreSQL")
                .addProperties(properties)
                .buildSessionFactory();
        service = new DefaultStardustPlayerService(sessionFactory);
    }

    @AfterEach
    void closeDatabase() {
        sessionFactory.close();
    }

    private static StardustPlayer newPlayer(String name, String vanished) {
        Map<String, PlayerSetting> settings = new HashMap<>();
        settings.put("vanished", new PlayerSetting(vanished, 1, FIXED_TIME));
        return new StardustPlayer(null, PLAYER_ID, name, settings, FIXED_TIME, FIXED_TIME);
    }

    private static void setVanished(StardustPlayer player, String value) {
        player.getSettings().put("vanished", new PlayerSetting(value, 1, FIXED_TIME));
    }

    private static String vanished(StardustPlayer player) {
        return player.getSettings().get("vanished").value();
    }

    @Test
    void staleUpdateThrowsAndLeavesStoredRowUnchanged() {
        service.saveOrUpdate(newPlayer("Steve", "false"));
        var stale = service.findById(PLAYER_ID);
        var fresh = service.findById(PLAYER_ID);
        setVanished(fresh, "true");
        service.saveOrUpdate(fresh);

        setVanished(stale, "stale");

        assertThrows(StalePlayerDataException.class, () -> service.saveOrUpdate(stale),
                "an update based on an outdated version must be rejected");
        var stored = service.findById(PLAYER_ID);
        assertEquals("true", vanished(stored), "the rejected write must not change the stored settings");
        assertEquals(1, stored.getVersion(), "the rejected write must not change the stored version");
    }

    @Test
    void matchingVersionUpdateSucceedsAndAdvancesModelVersion() {
        service.saveOrUpdate(newPlayer("Steve", "false"));
        var model = service.findById(PLAYER_ID);
        setVanished(model, "true");

        service.saveOrUpdate(model);

        assertEquals(1, model.getVersion(), "the model must carry the new version after a successful update");
        assertEquals(1, service.findById(PLAYER_ID).getVersion(), "the stored version must have advanced");
    }

    @Test
    void consecutiveUpdatesThroughSameModelBothSucceed() {
        service.saveOrUpdate(newPlayer("Steve", "false"));
        var model = service.findById(PLAYER_ID);

        setVanished(model, "true");
        service.saveOrUpdate(model);
        model.getSettings().put("allow_flight", new PlayerSetting("true", 1, FIXED_TIME));
        service.saveOrUpdate(model);

        var stored = service.findById(PLAYER_ID);
        assertEquals(2, stored.getVersion(), "both updates must have been stored");
        assertEquals("true", vanished(stored));
        assertTrue(stored.getSettings().containsKey("allow_flight"), "the second update must be stored");
    }

    @Test
    void insertingExistingUuidKeepsStoredRowWithoutDuplicate() {
        service.saveOrUpdate(newPlayer("Steve", "false"));

        service.saveOrUpdate(newPlayer("Alex", "true"));

        assertEquals(1, service.findAll(10).size(), "a second insert for the same UUID must not create a duplicate row");
        var stored = service.findById(PLAYER_ID);
        assertEquals("Steve", stored.getName(), "the existing row must be loaded unchanged");
        assertEquals(0, stored.getVersion(), "the existing row must keep its version");
    }

    @Test
    void consecutiveCachedWritesOfOnlinePlayerBothPersist() {
        var writer = new CachingStardustPlayerServiceTest.ManualExecutor();
        writer.runOnExecute = true;
        Set<UUID> online = Set.of(PLAYER_ID);
        var cache = new CachingStardustPlayerService(service, writer, online::contains, () -> online);

        cache.saveOrUpdate(newPlayer("Steve", "false"));
        var model = cache.findById(PLAYER_ID);
        setVanished(model, "true");
        cache.saveOrUpdate(model);
        model.getSettings().put("allow_flight", new PlayerSetting("true", 1, FIXED_TIME));
        cache.saveOrUpdate(model);

        var stored = service.findById(PLAYER_ID);
        assertEquals(2, stored.getVersion(), "two consecutive writes of the same online player must both succeed");
        assertTrue(stored.getSettings().containsKey("allow_flight"), "the second write must be stored");
    }

    @Test
    void staleCachedWriteIsDroppedAndCacheReloadsDatabaseState() {
        service.saveOrUpdate(newPlayer("Steve", "false"));
        Set<UUID> online = Set.of(PLAYER_ID);
        var writer = new CachingStardustPlayerServiceTest.ManualExecutor();
        writer.runOnExecute = true;
        var cache = new CachingStardustPlayerService(service, writer, online::contains, () -> online);
        var cached = cache.findById(PLAYER_ID);

        var otherServer = service.findById(PLAYER_ID);
        setVanished(otherServer, "true");
        service.saveOrUpdate(otherServer);

        setVanished(cached, "stale");
        cache.saveOrUpdate(cached);

        var current = cache.findById(PLAYER_ID);
        assertEquals("true", vanished(current), "the cache must hold the database state after a conflict");
        assertEquals(1, current.getVersion(), "the cached state must carry the database version");

        setVanished(current, "false");
        cache.saveOrUpdate(current);
        assertEquals(2, service.findById(PLAYER_ID).getVersion(), "writes after a conflict must still be stored");
    }
}
