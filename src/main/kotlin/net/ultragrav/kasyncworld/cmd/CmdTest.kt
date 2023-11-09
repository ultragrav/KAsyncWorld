package net.ultragrav.kasyncworld.cmd

import kotlinx.coroutines.*
import net.kyori.adventure.text.Component
import net.minecraft.world.level.block.Blocks
import net.ultragrav.command.platform.SpigotCommand
import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.editSync
import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions
import net.ultragrav.kasyncworld.world.contract.AsyncWorld
import net.ultragrav.kasyncworld.world.inmemory.InMemoryWorld
import net.ultragrav.kasyncworld.world.inmemory.InMemoryWorldOptions
import org.bukkit.Location
import org.bukkit.World
import java.util.*
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

class CmdTest : SpigotCommand() {
    init {
        addAlias("test")
    }

    override fun perform() {

        val world: InMemoryWorld

        val millis = measureTimeMillis {
            world = AW.inMemoryWorldProvider.createWorld(
                "Test-World-${UUID.randomUUID()}",
                InMemoryWorldOptions(
                    World.Environment.NORMAL,
                    0..1,
                    0..1,
                    ChunkHeightOptions(20, -4),
                    false,
                    AW.codec
                )
            )
        }

        tell("Created world in $millis ms")

        spigotPlayer.teleport(
            Location(
                world.bukkitWorld,
                0.0,
                100.0,
                0.0
            )
        )
    }
}