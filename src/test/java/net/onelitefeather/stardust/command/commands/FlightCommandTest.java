package net.onelitefeather.stardust.command.commands;

import net.kyori.adventure.text.Component;
import net.onelitefeather.stardust.StardustPlugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.io.File;

class FlightCommandTest {

    private @NotNull ServerMock server;
    private StardustPlugin plugin;
    private FlightCommand command;

    @SuppressWarnings("removal")
    public static class MockStardustPlugin extends StardustPlugin {

        public MockStardustPlugin() {
        }

        public MockStardustPlugin(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
            super(loader, description, dataFolder, file);
        }

        @Override
        public void onEnable() {
            // Mock enable logic if needed
        }

        @Override
        public void onDisable() {

        }

        @Override
        public Component getPrefix() {
            return Component.text("[Stardust]");
        }
    }


    @BeforeEach
    void setUp() {
        // Initialize MockBukkit
        server = MockBukkit.mock();
        plugin = MockBukkit.load(MockStardustPlugin.class);

        // Create the command instance
        command = new FlightCommand(plugin);
    }

    @AfterEach
    void tearDown() {
        // Unmock MockBukkit
        MockBukkit.unmock();
    }

    @Disabled
    @Test
    void testFlightCommand() {
        // Create a mock player
        var player = server.addPlayer();

        // Check initial flight state
        Assertions.assertFalse(player.getAllowFlight(), "Player should not be able to fly initially");

        // Execute the command to toggle flight
        command.handleFlightCommand(player, null);

        // Check if the player is now able to fly
        Assertions.assertTrue(player.getAllowFlight(), "Player should be able to fly after command execution");

        // Execute the command again to toggle back
        command.handleFlightCommand(player, null);

        // Check if the player is no longer able to fly
        Assertions.assertFalse(player.getAllowFlight(), "Player should not be able to fly after toggling back");
    }

    @Disabled
    @Test
    void testGlowCommandWithTarget() {
        // Create a mock player
        var player = server.addPlayer();
        player.addAttachment(plugin, "stardust.command.flight.others", true);
        var target = server.addPlayer();
        target.setAllowFlight(false);
        // Check initial flight state
        Assertions.assertFalse(target.getAllowFlight(), "Target player should not be able to fly initially");
        // Execute the command to toggle flight for the target
        command.handleFlightCommand(player, target);
        // Check if the target player is now able to fly
        Assertions.assertTrue(target.getAllowFlight(), "Target player should be able to fly after command execution");
        // Execute the command again to toggle back
        command.handleFlightCommand(player, target);
        // Check if the target player is no longer able to fly
        Assertions.assertFalse(target.getAllowFlight(), "Target player should not be able to fly after toggling back");
    }

    @Disabled
    @Test
    void testFlightCommandWithoutPermission() {
        // Create a mock player without permission
        var player = server.addPlayer();
        player.addAttachment(plugin, "stardust.command.flight.others", false);
        var target = server.addPlayer();

        // Attempt to execute the command with a target
        command.handleFlightCommand(player, target);

        // Check if the target is still not able to fly
        Assertions.assertFalse(target.getAllowFlight(), "Target player should not be able to fly after command execution without permission");
    }

}
