package net.onelitefeather.stardust.command.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.specifier.Quoted
import cloud.commandframework.annotations.specifier.Range
import cloud.commandframework.annotations.suggestions.Suggestions
import cloud.commandframework.context.CommandContext
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.extenstions.miniMessage
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

class SpawnMobCommand(private val stardustPlugin: StardustPlugin) {

    private val entityTypes =
        EntityType.values().filter { entityType: EntityType -> entityType.isAlive && entityType.isSpawnable }


    @CommandMethod("spawnmob <type> <amount>")
    @CommandPermission("stardust.command.spawnmob")
    @CommandDescription("Spawn a Mob with the given Type.")
    fun handleCommand(
        player: Player,
        @Argument(value = "entity_types") @Quoted entityType: String,
        @Range(min = "1", max = "25") @Argument(value = "amount") amount: Int
    ) {

        val location = player.location
        if (!entityType.contains(",")) {
            spawn(location, EntityType.valueOf(entityType), amount)
            player.sendMessage(miniMessage {
                stardustPlugin.i18nService.getMessage(
                    "commands.spawnmob.success",
                    *arrayOf(
                        stardustPlugin.i18nService.getPluginPrefix(),
                        entityType,
                        amount
                    )
                )
            })
        } else {

            val strings = entityType.split(",").dropLastWhile { it.isEmpty() }.toTypedArray()
            var spawnedMob: Entity? = null
            var spawnedMount: Entity

            for (i in strings.indices) {
                if (i == 0) {
                    spawnedMob = location.world.spawnEntity(
                        location,
                        EntityType.valueOf(strings[0].uppercase())
                    )
                }
                val next = i + 1
                if (next < strings.size) {
                    val nextType = EntityType.valueOf(strings[next].uppercase())
                    if (spawnedMob != null) {
                        spawnedMount = location.world.spawnEntity(location, nextType)
                        spawnedMob.addPassenger(spawnedMount)
                        spawnedMob = spawnedMount
                    }
                }
            }

            player.sendMessage(miniMessage {
                stardustPlugin.i18nService.getMessage(
                    "commands.spawnmob.success", *arrayOf(
                        stardustPlugin.i18nService.getPluginPrefix(),
                        strings[0], 1
                    )
                )
            })
        }
    }

    @Suggestions("entity_types")
    fun getEntityTypes(context: CommandContext<CommandSender>, input: String): List<String> =
        entityTypes.filter { StringUtil.startsWithIgnoreCase(it.name.lowercase(), input.lowercase()) }.map { it.name.lowercase() }

    private fun spawn(location: Location, type: EntityType, amount: Int) {
        for (i in 0 until amount) {
            location.world.spawnEntity(location, type)
        }
    }
}