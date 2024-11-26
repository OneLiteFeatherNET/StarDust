package net.onelitefeather.stardust.command.mapper

import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.BlockCommandSender
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.incendo.cloud.SenderMapper

@Suppress("UnstableApiUsage")
class BukkitSenderMapper : SenderMapper<CommandSourceStack, CommandSender> {
    private val consoleSenderMapper = ConsoleSenderMapper()

    override fun map(base: CommandSourceStack): CommandSender {
        if (base.sender is Player) {
            return base.sender
        } else if (base.sender is BlockCommandSender) {
            return base.sender
        }

        return Bukkit.getConsoleSender()
    }

    override fun reverse(mapped: CommandSender): CommandSourceStack {
        if (mapped is ConsoleCommandSender) return consoleSenderMapper
        if (mapped is Player) return PlayerSenderMapper(mapped)
        return BlockSenderMapper(mapped as BlockCommandSender)
    }

    @Suppress("NonExtendableApiUsage")
    private data class BlockSenderMapper(val blockCommandSender: BlockCommandSender) : CommandSourceStack {
        override fun getLocation(): Location {
            return blockCommandSender.block.location
        }

        override fun getSender(): CommandSender {
            return this.blockCommandSender
        }

        override fun getExecutor(): Entity? {
            return null
        }
    }

    @Suppress("NonExtendableApiUsage")
    private class ConsoleSenderMapper : CommandSourceStack {
        override fun getLocation(): Location {
            return Bukkit.getWorlds().first().spawnLocation
        }

        override fun getSender(): CommandSender {
            return Bukkit.getConsoleSender()
        }

        override fun getExecutor(): Entity? {
            return null
        }
    }

    @Suppress("NonExtendableApiUsage")
    private data class PlayerSenderMapper(val player: Player) : CommandSourceStack {
        override fun getLocation(): Location {
            return player.location
        }

        override fun getSender(): CommandSender {
            return this.player
        }

        override fun getExecutor(): Entity {
            return this.player
        }
    }
}
