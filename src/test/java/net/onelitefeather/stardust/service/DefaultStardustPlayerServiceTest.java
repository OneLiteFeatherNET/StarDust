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



    @Test
    void testPlayerNotExists() {
        DatabaseConnectionService connectionService = new DatabaseConnectionService(Path.of("src/test/resources/test-connection.cfg.xml"));
        try {
            StardustPlayerService playerService = new DefaultStardustPlayerService(connectionService.getSessionFactory().orElse(null));
            assertFalse(playerService.playerExists(UUID.fromString("df7c66a6-7876-44c2-9abd-9bd33f7d3d9e")));
        } finally {
            connectionService.close();
        }
    }

    @Test
    void testRepeatedUpdatesUseCurrentEntityVersion() {
        DatabaseConnectionService connectionService = new DatabaseConnectionService(Path.of("src/test/resources/test-connection.cfg.xml"));
        try {
            StardustPlayerService playerService = new DefaultStardustPlayerService(connectionService.getSessionFactory().orElse(null));
            UUID playerUuid = UUID.randomUUID();
            Instant now = Instant.now();
            var player = new StardustPlayer(null, playerUuid, "Player",
                    Map.of("vanished", new PlayerSetting("false", 1, now)), now, now);

            playerService.saveOrUpdate(player);
            var savedPlayer = playerService.findById(playerUuid);
            assertNotNull(savedPlayer);

            playerService.saveOrUpdate(new StardustPlayer(savedPlayer.getId(), playerUuid, "Updated",
                    Map.of("vanished", new PlayerSetting("true", 1, now)), savedPlayer.getCreatedAt(), now));
            playerService.saveOrUpdate(new StardustPlayer(savedPlayer.getId(), playerUuid, "Updated again",
                    Map.of("vanished", new PlayerSetting("false", 1, now)), savedPlayer.getCreatedAt(), now));

            var updatedPlayer = playerService.findById(playerUuid);
            assertNotNull(updatedPlayer);
            assertEquals("Updated again", updatedPlayer.getName());
            assertEquals("false", updatedPlayer.getSettings().get("vanished").value());
        } finally {
            connectionService.close();
        }
    }
}
