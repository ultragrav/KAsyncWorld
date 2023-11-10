package net.ultragrav.kasyncworld.cmd

import kotlinx.coroutines.*
import net.kyori.adventure.text.Component
import net.minecraft.world.level.block.Blocks
import net.ultragrav.command.platform.SpigotCommand
import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.editSync
import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions
import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.contract.AsyncWorld
import net.ultragrav.kasyncworld.world.inmemory.*
import net.ultragrav.kasyncworld.world.versionio.ChunkReadOptions
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

        // Copy chunk they're in
        val bukkitChunk = spigotPlayer.chunk

        // Save chunks up to 3 away
        val chunkList = mutableListOf<LocatedCompressedChunk>()

        val saveChunksMillis = measureTimeMillis {
            for (dx in 0..3) {
                for (dz in 0..3) {
                    val cx = bukkitChunk.x + dx
                    val cz = bukkitChunk.z + dz
                    val chunk = AW.chunkIO.readChunk(
                        bukkitChunk.world.getChunkAt(
                            cx,
                            cz
                        ),
                        AW.storageChunkFactory,
                        ChunkReadOptions()
                    )
                    val compressed = SCompressedAsyncChunk(chunk, AW.codec)
                    chunkList.add(LocatedCompressedChunk(dx, dz, compressed))
                }
            }
        }

        val packed = PackedWorld(chunkList)

        val millis = measureTimeMillis {

            world = AW.inMemoryWorldProvider.createWorld(
                "Test-World-${UUID.randomUUID()}",
                InMemoryWorldOptions(
                    World.Environment.NORMAL,
                    0..3,
                    0..3,
                    ChunkHeightOptions(20, -4),
                    true,
                    AW.codec
                ),
                packed
            )
        }

        tell("Saved chunks in $saveChunksMillis ms")
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