package net.onelitefeather.stardust.command.commands;

import net.kyori.adventure.text.Component;
import net.onelitefeather.stardust.StardustPlugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.io.File;
import java.net.InetSocketAddress;

class IPSameCommandTest {

    private ServerMock server;

    private StardustPlugin plugin;
    
    private IPSameCommand ipSameCommand;

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
        ipSameCommand = new IPSameCommand(plugin);
    }

    @AfterEach
    void tearDown() {
        // Unmock MockBukkit
        MockBukkit.unmock();
    }

    @Test
    void testIsPlayerIPSame_WithMatchingIP() {

        // Create test players
        var sender = server.addPlayer("CommandSender");
        var targetPlayer = server.addPlayer("TargetPlayer");
        var playerWithSameIP = server.addPlayer("PlayerWithSameIP");
        var playerWithDifferentIP = server.addPlayer("PlayerWithDifferentIP");

        // Set up IP addresses
        InetSocketAddress targetAddress = new InetSocketAddress("192.168.1.1", 12345);
        InetSocketAddress sameAddress = new InetSocketAddress("192.168.1.1", 54321);
        InetSocketAddress differentAddress = new InetSocketAddress("192.168.1.2", 12345);

        // Use reflection to set the addresses since MockBukkit doesn't provide a direct way
        targetPlayer.setAddress(targetAddress);
        playerWithSameIP.setAddress(sameAddress);
        playerWithDifferentIP.setAddress(differentAddress);
        // Act
        ipSameCommand.isPlayerIPSame(sender, targetPlayer);

        // Assert
        // Verify that sender received the target IP message
        Assertions.assertEquals(sender.nextComponentMessage(), Component.translatable("commands.ipsame.target-ip")
                .arguments(plugin.getPrefix(), targetPlayer.displayName(), Component.text("192.168.1.1")));

        // Verify that sender received a message about playerWithSameIP having the same IP
        Assertions.assertEquals(sender.nextComponentMessage(), Component.translatable("commands.ipsame.show")
                .arguments(plugin.getPrefix(), targetPlayer.displayName(), playerWithSameIP.displayName()));

        // Verify that sender did not receive a message about playerWithDifferentIP
        Assertions.assertNotEquals(sender.nextComponentMessage(), Component.translatable("commands.ipsame.show")
                .arguments(plugin.getPrefix(), targetPlayer.displayName(), playerWithDifferentIP.displayName()));
    }

    @Test
    void testIsPlayerIPSame_NoMatchingIP() {
        // Create test players
        var sender = server.addPlayer("CommandSender");
        var targetPlayer = server.addPlayer("TargetPlayer");
        var playerWithDifferentIP = server.addPlayer("PlayerWithDifferentIP");

        // Set up IP addresses
        InetSocketAddress targetAddress = new InetSocketAddress("192.168.1.1", 12345);
        InetSocketAddress differentAddress = new InetSocketAddress("192.168.1.2", 12345);

        // Use reflection to set the addresses since MockBukkit doesn't provide a direct way
        targetPlayer.setAddress(targetAddress);
        playerWithDifferentIP.setAddress(differentAddress);
        // Act
        ipSameCommand.isPlayerIPSame(sender, targetPlayer);

        // Assert
        // Verify that sender received the target IP message
        Assertions.assertEquals(sender.nextComponentMessage(), Component.translatable("commands.ipsame.target-ip")
                .arguments(plugin.getPrefix(), targetPlayer.displayName(), Component.text("192.168.1.1")));

        // Verify that sender did not receive any message about other players having the same IP
        Assertions.assertNull(sender.nextComponentMessage());

    }

    @Test
    void testIsPlayerIPSame_TargetIsCommandSender() {
        // Create test players
        var sender = server.addPlayer("CommandSender");
        // Act
        ipSameCommand.isPlayerIPSame(sender, sender);
        
        // Assert
        // Verify that no messages were sent
        Assertions.assertNull(sender.nextComponentMessage(), "Expected no messages to be sent when target is the command sender");
    }

    @Test
    void testIsPlayerIPSame_NullAddress() {
        // Create test players
        var sender = server.addPlayer("CommandSender");
        var targetPlayer = server.addPlayer("TargetPlayer");

        // Set up IP addresses
        InetSocketAddress targetAddress = new InetSocketAddress("192.168.1.1", 12345);

        // Use reflection to set the addresses since MockBukkit doesn't provide a direct way
        targetPlayer.setAddress(targetAddress);
        // Arrange
        // Set target player's address to null
        targetPlayer.setAddress(null);
        
        // Act
        ipSameCommand.isPlayerIPSame(sender, targetPlayer);
        
        // Assert
        // Verify that no messages were sent
        Assertions.assertNull(sender.nextComponentMessage(), "Expected no messages to be sent when target player's address is null");
    }

    @Test
    void testIsPlayerIPSame_OtherPlayerNullAddress() {

        // Create test players
        var sender = server.addPlayer("CommandSender");
        var targetPlayer = server.addPlayer("TargetPlayer");
        var playerWithSameIP = server.addPlayer("PlayerWithSameIP");
        var playerWithDifferentIP = server.addPlayer("PlayerWithDifferentIP");

        // Set up IP addresses
        InetSocketAddress targetAddress = new InetSocketAddress("192.168.1.1", 12345);
        InetSocketAddress differentAddress = new InetSocketAddress("192.168.1.2", 12345);

        // Use reflection to set the addresses since MockBukkit doesn't provide a direct way
        targetPlayer.setAddress(targetAddress);
        playerWithSameIP.setAddress(null);
        playerWithDifferentIP.setAddress(differentAddress);

        // Act
        ipSameCommand.isPlayerIPSame(sender, targetPlayer);

        // Assert
        // Verify that sender received the target IP message
        Assertions.assertEquals(sender.nextComponentMessage(), Component.translatable("commands.ipsame.target-ip")
                .arguments(plugin.getPrefix(), targetPlayer.displayName(), Component.text("192.168.1.1")));

        // Verify that no message was sent about playerWithSameIP (which has a null address)
        Assertions.assertNotEquals(sender.nextComponentMessage(), Component.translatable("commands.ipsame.show")
                .arguments(plugin.getPrefix(), targetPlayer.displayName(), playerWithSameIP.displayName()));
    }
}