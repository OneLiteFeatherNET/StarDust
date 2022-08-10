package net.onelitefeather.stardust.util;

import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Consumer;

public final class LocationUtil {

    public static void generateSecureRandomTeleport(World world, int radius, int tries, Consumer<Location> locationConsumer) {
        if(tries > 0) {
            tries--;
            if(world.getEnvironment().equals(World.Environment.NORMAL)) {
                int x = new Random().nextInt(radius * 2) - radius;
                int z = new Random().nextInt(radius * 2) - radius;
                Block block = world.getHighestBlockAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES);
                if(block.getType().isSolid()) {
                    Material material = block.getRelative(BlockFace.UP).getType();
                    if(material.isAir() || material.isEmpty()) {
                        locationConsumer.accept(block.getLocation().add(0.5, 1, 0.5));
                        return;
                    }
                }
                generateSecureRandomTeleport(world, radius, tries, locationConsumer);
            } else if(Arrays.asList(World.Environment.NETHER, World.Environment.THE_END).contains(world.getEnvironment())) {
                int x = new Random().nextInt(radius * 2) - radius;
                int z = new Random().nextInt(radius * 2) - radius;

                for (int y = 120; y > 0; y--) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType().isSolid()) {
                        Material material = block.getRelative(BlockFace.UP).getType();
                        if (material.isAir() || material.isEmpty()) {
                            locationConsumer.accept(block.getLocation().add(0.5, 1, 0.5));
                            return;
                        }
                    }
                }
                generateSecureRandomTeleport(world, radius, tries, locationConsumer);

            }
        }
    }



}
