package net.onelitefeather.stardust.service;

import net.onelitefeather.stardust.DatabaseConnectionService;
import net.onelitefeather.stardust.api.StardustPlayerService;
import net.onelitefeather.stardust.api.player.StardustPlayer;
import net.onelitefeather.stardust.api.player.setting.PlayerSetting;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultStardustPlayerServiceTest {

    private static final Path CONFIG_PATH = Path.of("src/test/resources/test-connection.cfg.xml");
    private static final DatabaseConnectionService CONNECTION_SERVICE = new DatabaseConnectionService(CONFIG_PATH);
    private static final StardustPlayerService PLAYER_SERVICE = new DefaultStardustPlayerService(CONNECTION_SERVICE.getSessionFactory().orElse(null));

    private static final StardustPlayer TEST_PLAYER = new StardustPlayer(null,
            UUID.fromString("df7c66a6-7876-44c2-9abd-9bd33f7d3d9e"),
            "theShadowsDust",
            Map.of("vanished", new PlayerSetting("false", 1, Instant.now())),
            Instant.now(), Instant.now());

    @Test
    void testPlayerNotExists() {
        assertFalse(PLAYER_SERVICE.playerExists(TEST_PLAYER.getUniqueId()));
    }

    @Test
    void testDeletePlayer() {
        PLAYER_SERVICE.saveOrUpdate(TEST_PLAYER);
        assertTrue(PLAYER_SERVICE.playerExists(TEST_PLAYER.getUniqueId()));
        PLAYER_SERVICE.delete(TEST_PLAYER);
        assertFalse(PLAYER_SERVICE.playerExists(TEST_PLAYER.getUniqueId()));
    }

    @Test
    void testFindPlayer() {
        var player = PLAYER_SERVICE.findById(TEST_PLAYER.getUniqueId());
        assertNotNull(player);
    }

    @Test
    void testListPlayers() {
        PLAYER_SERVICE.saveOrUpdate(TEST_PLAYER);
        var players = PLAYER_SERVICE.findAll(5);
        assertFalse(players.isEmpty());
    }
}
