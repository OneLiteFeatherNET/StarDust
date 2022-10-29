package net.onelitefeather.stardust.util

import org.bukkit.HeightMap
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import java.util.Random
import java.util.function.Consumer


fun generateSecureRandomTeleport(world: World, radius: Int, tries: Int, locationConsumer: Consumer<Location?>) {
    var currentTries = tries
    if (currentTries > 0) {
        currentTries--
        if (world.environment == World.Environment.NORMAL) {
            val x: Int = Random().nextInt(radius * 2) - radius
            val z: Int = Random().nextInt(radius * 2) - radius
            val block: Block = world.getHighestBlockAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES)
            if (block.type.isSolid) {
                val material: Material = block.getRelative(BlockFace.UP).type
                if (material.isAir || material.isEmpty) {
                    locationConsumer.accept(block.location.add(0.5, 1.0, 0.5))
                    return
                }
            }
            generateSecureRandomTeleport(world, radius, currentTries, locationConsumer)
        } else if (listOf(World.Environment.NETHER, World.Environment.THE_END).contains(world.environment)) {
            val x: Int = Random().nextInt(radius * 2) - radius
            val z: Int = Random().nextInt(radius * 2) - radius
            for (y in 120 downTo 1) {
                val block: Block = world.getBlockAt(x, y, z)
                if (block.type.isSolid) {
                    val material: Material = block.getRelative(BlockFace.UP).type
                    if (material.isAir || material.isEmpty) {
                        locationConsumer.accept(block.location.add(0.5, 1.0, 0.5))
                        return
                    }
                }
            }
            generateSecureRandomTeleport(world, radius, currentTries, locationConsumer)
        }
    }
}
