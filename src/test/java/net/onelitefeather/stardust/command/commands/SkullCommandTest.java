package net.onelitefeather.stardust.command.commands;

import net.kyori.adventure.text.Component;
import net.onelitefeather.stardust.StardustPlugin;
import org.bukkit.Material;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.io.File;

@SuppressWarnings("removal")
public class SkullCommandTest {

    private @NotNull ServerMock server;
    private StardustPlugin plugin;
    private SkullCommand skullCommand;

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
        plugin = MockBukkit.load(net.onelitefeather.stardust.command.commands.RenameCommandTest.MockStardustPlugin.class);

        // Create the command instance
        skullCommand = new SkullCommand(plugin);
    }

    @AfterEach
    void tearDown() {
        // Unmock MockBukkit
        MockBukkit.unmock();
    }
    @Test
    void testCommandWithOfflineSkull() {
        PlayerMock player = server.addPlayer();
        skullCommand.handleCommand(player, "Seelenretterin");
        server.getScheduler().performOneTick();
        // Then assert the expected behavior, such as checking if the skull item was added to the player's inventory
        Assertions.assertNotNull(skullCommand);
        Assertions.assertTrue(player.getInventory().contains(Material.PLAYER_HEAD),
                "Player should have a player head in their inventory.");
        Assertions.assertEquals(player.nextComponentMessage(), Component.translatable("commands.skull.success")
                .arguments(plugin.getPrefix(), Component.text("Seelenretterin")));
    }
    @Test
    void testCommandWithPlayerSkullSelf() {
        PlayerMock player = server.addPlayer("Seelenretterin");
        skullCommand.handleCommand(player, player.getName());
        server.getScheduler().performOneTick();
        // Then assert the expected behavior, such as checking if the skull item was added to the player's inventory
        Assertions.assertNotNull(skullCommand);
        Assertions.assertTrue(player.getInventory().contains(Material.PLAYER_HEAD),
                "Player should have a player head in their inventory.");
        Assertions.assertEquals(player.nextComponentMessage(), Component.translatable("commands.skull.success")
                .arguments(plugin.getPrefix(), Component.text(player.getName())));
    }
    @Test
    void testCommandWithPlayerSkullOther() {
        PlayerMock player = server.addPlayer("Seelenretterin");
        PlayerMock otherPlayer = server.addPlayer("TheMeinerLP");
        skullCommand.handleCommand(player, otherPlayer.getName());
        server.getScheduler().performOneTick();
        // Then assert the expected behavior, such as checking if the skull item was added to the player's inventory
        Assertions.assertNotNull(skullCommand);
        Assertions.assertTrue(player.getInventory().contains(Material.PLAYER_HEAD),
                "Player should have a player head in their inventory.");
        Assertions.assertEquals(player.nextComponentMessage(), Component.translatable("commands.skull.success")
                .arguments(plugin.getPrefix(), Component.text(otherPlayer.getName())));
    }
    @Test
    void testCommandWithPlayerSkullNull() {
        PlayerMock player = server.addPlayer("Seelenretterin");
        skullCommand.handleCommand(player, null);
        server.getScheduler().performOneTick();
        // Then assert the expected behavior, such as checking if the skull item was added to the player's inventory
        Assertions.assertNotNull(skullCommand);
        Assertions.assertTrue(player.getInventory().contains(Material.PLAYER_HEAD),
                "Player should have a player head in their inventory.");
        Assertions.assertEquals(player.nextComponentMessage(), Component.translatable("commands.skull.success")
                .arguments(plugin.getPrefix(), Component.text(player.getName())));
    }
    @Test
    void testCommandWithPlayerSkullEmpty() {
        PlayerMock player = server.addPlayer("Seelenretterin");
        skullCommand.handleCommand(player, "");
        server.getScheduler().performOneTick();
        // Then assert the expected behavior, such as checking if the skull item was added to the player's inventory
        Assertions.assertNotNull(skullCommand);
        Assertions.assertTrue(player.getInventory().contains(Material.PLAYER_HEAD),
                "Player should have a player head in their inventory.");
        Assertions.assertEquals(player.nextComponentMessage(), Component.translatable("commands.skull.success")
                .arguments(plugin.getPrefix(), Component.text("")));
    }
}
