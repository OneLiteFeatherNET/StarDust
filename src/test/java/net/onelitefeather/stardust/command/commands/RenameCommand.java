package net.onelitefeather.stardust.command.commands;

import net.kyori.adventure.text.Component;
import net.onelitefeather.stardust.StardustPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.io.File;

@SuppressWarnings("removal")
class RenameCommand {

    private @NotNull ServerMock server;
    private StardustPlugin plugin;
    private RenameCommand renameCommand;

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
        renameCommand = new RenameCommand(plugin);
    }
    @AfterEach
    void tearDown() {
        // Unmock MockBukkit
        MockBukkit.unmock();
    }

    @Test
    void testRenameItemNotInHand() {

        PlayerMock player = server.addPlayer();
        // Simulate the player not holding an item
        player.setItemInHand(new ItemStack(Material.AIR));
        player.performCommand("/itemrename"); //How?
        // Verify that the player received the correct message
        player.assertSaid(Component.translatable("commands.rename.invalid-item").arguments(plugin.getPrefix()));

    }
    @Test
    void testRenameItemInHand() {
        PlayerMock player = server.addPlayer();
        ItemStack itemInHand = new ItemStack(Material.DIAMOND_SWORD);
        player.setItemInHand(itemInHand);

        // Simulate the command execution
        String newName = "Excalibur";
        player.performCommand("/itemrename " + newName);

        // Verify that the item name was changed
        ItemStack updatedItem = player.getInventory().getItemInMainHand();
        assert updatedItem.getType() == Material.DIAMOND_SWORD;
        assert updatedItem.getItemMeta().displayName().equals(Component.text(newName));

        // Verify that the player received the success message
        player.assertSaid(Component.translatable("commands.rename.success").arguments(plugin.getPrefix(), Component.text(newName)));
    }

}
