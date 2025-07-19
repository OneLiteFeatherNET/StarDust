package net.onelitefeather.stardust.command.commands;

import net.kyori.adventure.text.Component;
import net.onelitefeather.stardust.StardustPlugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.io.File;

class GlowCommandTest {

    private @NotNull ServerMock server;
    private StardustPlugin plugin;
    private GlowCommand command;

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
        command = new GlowCommand(plugin);
    }

    @AfterEach
    void tearDown() {
        // Unmock MockBukkit
        MockBukkit.unmock();
    }

    @Test
    void testGlowCommand() {
        // Create a mock player
        var player = server.addPlayer();

        // Check initial invulnerability state
        Assertions.assertFalse(player.isGlowing(), "Player should not be glowing initially");

        // Execute the command to toggle invulnerability
        command.handleCommand(player, null);

        // Check if the player is now invulnerable
        Assertions.assertTrue(player.isGlowing(), "Player should be glowing after command execution");

        // Execute the command again to toggle back
        command.handleCommand(player, null);

        // Check if the player is no longer invulnerable
        Assertions.assertFalse(player.isGlowing(), "Player should not be glowing after toggling back");
    }

    @Test
    void testGlowCommandWithTarget() {
        // Create a mock player
        var player = server.addPlayer();
        player.addAttachment(plugin, "stardust.command.glow.others", true);
        var target = server.addPlayer();

        // Check initial glowing state
        Assertions.assertFalse(target.isGlowing(), "Target player should not be glowing initially");

        // Execute the command with a target
        command.handleCommand(player, target);

        // Check if the target is now glowing
        Assertions.assertTrue(target.isGlowing(), "Target player should be glowing after command execution");

        // Execute the command again to toggle back
        command.handleCommand(player, target);

        // Check if the target is no longer glowing
        Assertions.assertFalse(target.isGlowing(), "Target player should not be glowing after toggling back");
    }

    @Test
    void testGlowCommandWithPermission() {
        // Create a mock player with permission
        var player = server.addPlayer();
        player.addAttachment(plugin, "stardust.command.glow.others", true);
        var target = server.addPlayer("target");

        // Check initial glowing state
        Assertions.assertFalse(target.isGlowing(), "Target player should not be glowing initially");

        // Execute the command with a target
        command.handleCommand(player, target);

        // Check if the target is now glowing
        Assertions.assertTrue(target.isGlowing(), "Target player should be glowing after command execution");

        // Execute the command again to toggle back
        command.handleCommand(player, target);

        // Check if the target is no longer glowing
        Assertions.assertFalse(target.isGlowing(), "Target player should not be glowing after toggling back");
    }

}
