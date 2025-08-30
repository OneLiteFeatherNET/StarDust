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

class GodmodeCommandTest {

    private @NotNull ServerMock server;
    private StardustPlugin plugin;
    private GodmodeCommand command;

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
        command = new GodmodeCommand(plugin);
    }

    @AfterEach
    void tearDown() {
        // Unmock MockBukkit
        MockBukkit.unmock();
    }

    @Test
    void test_is_invulnerable() {
        // Create a mock player
        var player = server.addPlayer();

        // Check initial invulnerability state
        Assertions.assertFalse(player.isInvulnerable(), "Player should not be invulnerable initially");

        // Execute the command to toggle invulnerability
        command.handleCommand(player, null);

        // Check if the player is now invulnerable
        Assertions.assertTrue(player.isInvulnerable(), "Player should be invulnerable after command execution");

        // Execute the command again to toggle back
        command.handleCommand(player, null);

        // Check if the player is no longer invulnerable
        Assertions.assertFalse(player.isInvulnerable(), "Player should not be invulnerable after toggling back");
    }

    @Test
    void test_invulnerability_others() {
        // Create a mock player and another player
        var player = server.addPlayer();
        player.addAttachment(plugin, "stardust.command.godmode.others", true);
        var target = server.addPlayer("target");

        // Check initial invulnerability state
        Assertions.assertFalse(target.isInvulnerable(), "Target should not be invulnerable initially");

        // Execute the command as the player to toggle invulnerability for the target
        command.handleCommand(player, target);

        // Check if the target is now invulnerable
        Assertions.assertTrue(target.isInvulnerable(), "Target should be invulnerable after command execution");

        // Execute the command again to toggle back
        command.handleCommand(player, target);

        // Check if the target is no longer invulnerable
        Assertions.assertFalse(target.isInvulnerable(), "Target should not be invulnerable after toggling back");
    }

    @Test
    void test_invulnerability_no_permission() {
        // Create a mock player and another player
        var player = server.addPlayer();
        var target = server.addPlayer("target");

        // Check initial invulnerability state
        Assertions.assertFalse(target.isInvulnerable(), "Target should not be invulnerable initially");

        // Execute the command as the player to toggle invulnerability for the target
        command.handleCommand(player, target);

        // Check if the target is still not invulnerable due to lack of permission
        Assertions.assertFalse(target.isInvulnerable(), "Target should not be invulnerable without permission");
    }

}
