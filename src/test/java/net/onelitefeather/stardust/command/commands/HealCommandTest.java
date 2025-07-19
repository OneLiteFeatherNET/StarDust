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
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.io.File;

class HealCommandTest {


    private @NotNull ServerMock server;
    private StardustPlugin plugin;
    private HealCommand healCommand;

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
        plugin = MockBukkit.load(IPSameCommandTest.MockStardustPlugin.class);

        // Create the command instance
        healCommand = new HealCommand(plugin);
    }

    @AfterEach
    void tearDown() {
        // Unmock MockBukkit
        MockBukkit.unmock();
    }

    @Test
    void test_heal() {
       PlayerMock player = server.addPlayer();
        player.setHealth(10.0);
        player.setFoodLevel(10);
        player.setSaturation(2.0f);
        player.setFireTicks(100);

        // Execute the heal command
        healCommand.onCommand(player, player);
        // Verify that the player's health, food level, saturation, and fire ticks are reset
        Assertions.assertEquals(20.0, player.getHealth(), "Player health should be reset to max.");
        Assertions.assertEquals(20, player.getFoodLevel(), "Player food level should be reset to max.");
        Assertions.assertEquals(5.0f, player.getSaturation(), "Player saturation should be reset to max.");
        Assertions.assertEquals(0, player.getFireTicks(), "Player fire ticks should be reset to 0.");

    }

    @Test
    void test_heal_other_player() {
        PlayerMock sender = server.addPlayer("CommandSender");
        sender.addAttachment(plugin, "stardust.command.heal.others", true);
        PlayerMock target = server.addPlayer("TargetPlayer");

        // Set initial health and food level for the target player
        target.setHealth(10.0);
        target.setFoodLevel(10);
        target.setSaturation(2.0f);
        target.setFireTicks(100);

        // Execute the heal command as the sender
        healCommand.onCommand(sender, target);

        // Verify that the target player's health, food level, saturation, and fire ticks are reset
        Assertions.assertEquals(20.0, target.getHealth(), "Target player health should be reset to max.");
        Assertions.assertEquals(20, target.getFoodLevel(), "Target player food level should be reset to max.");
        Assertions.assertEquals(5.0f, target.getSaturation(), "Target player saturation should be reset to max.");
        Assertions.assertEquals(0, target.getFireTicks(), "Target player fire ticks should be reset to 0.");
    }

    @Test
    void test_heal_no_permission() {
        PlayerMock sender = server.addPlayer("CommandSender");
        PlayerMock target = server.addPlayer("TargetPlayer");

        // Set initial health and food level for the target player
        target.setHealth(10.0);
        target.setFoodLevel(10);
        target.setSaturation(2.0f);
        target.setFireTicks(100);

        // Execute the heal command as the sender without permission
        healCommand.onCommand(sender, target);

        // Verify that the target player's health, food level, saturation, and fire ticks are not reset
        Assertions.assertEquals(10.0, target.getHealth(), "Target player health should not be reset.");
        Assertions.assertEquals(10, target.getFoodLevel(), "Target player food level should not be reset.");
        Assertions.assertEquals(2.0f, target.getSaturation(), "Target player saturation should not be reset.");
        Assertions.assertEquals(100, target.getFireTicks(), "Target player fire ticks should not be reset.");
    }
}
