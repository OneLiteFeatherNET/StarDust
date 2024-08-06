package net.onelitefeather.stardust.command.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.specifier.Greedy
import net.kyori.adventure.text.Component
import net.onelitefeather.stardust.StardustPlugin
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class GameModeCommand(private val stardustPlugin: StardustPlugin) {

    @CommandMethod("gamemode|gm <gamemode> [player]")
    @CommandPermission("stardust.command.gamemode")
    @CommandDescription("Change the GameMode of a player.")
    fun handleCommand(
        commandSender: CommandSender,
        @Argument(value = "gamemode") gameMode: GameMode,
        @Greedy @Argument(value = "player") target: Player?
    ) {

        try {
            val gameModeName = Component.translatable(String.format("gameMode.%s", gameMode.name.lowercase()));

            if (target == null) {
                val player = commandSender as Player
                player.gameMode = gameMode
                commandSender.sendMessage(Component.translatable("commands.gamemode.success.self").arguments(gameModeName))
            } else {

                if (commandSender != target) {
                    if (!commandSender.hasPermission("stardust.command.gamemode.others")) {
                        commandSender.sendMessage(Component.translatable("plugin.not-enough-permissions").arguments(stardustPlugin.getPluginPrefix()))
                        return
                    }

                    target.sendMessage(Component.translatable("gameMode.changed").arguments(gameModeName))
                }

                target.gameMode = gameMode
                commandSender.sendMessage(
                    Component.translatable("commands.gamemode.success.other").arguments(target.displayName(), gameModeName)
                )
            }
        } catch (e: Exception) {
            this.stardustPlugin.getLogger().throwing(GameModeCommand::class.java.simpleName, "handleCommand", e)
        }
    }
}