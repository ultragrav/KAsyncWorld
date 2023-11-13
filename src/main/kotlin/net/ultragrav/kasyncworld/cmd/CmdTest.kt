package net.ultragrav.kasyncworld.cmd

import net.minecraft.world.level.biome.Biomes
import net.ultragrav.command.platform.SpigotCommand
import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.world.chunk.io.ChunkReadOptions
import net.ultragrav.kasyncworld.world.inmemory.*
import net.ultragrav.kasyncworld.world.inmemory.chunk.EncodedAsyncChunk
import net.ultragrav.kasyncworld.world.inmemory.pack.PackedWorld
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Biome
import java.util.*
import kotlin.system.measureTimeMillis

class CmdTest : SpigotCommand() {

    companion object {
        var currWorld: InMemoryWorld? = null
    }

    init {
        addAlias("test")
    }

    override fun perform() {

        // Copy chunk they're in
        val bukkitChunk = spigotPlayer.chunk

        // Save chunks up to 3 away
        val chunkList = mutableListOf<EncodedAsyncChunk>()

        val netherBiome = AW.globalBiomePalette.listIds().map { AW.globalBiomePalette.getState(it) }
            .first { it.`is`(Biomes.CRIMSON_FOREST) }

        val saveChunksMillis = measureTimeMillis {
            for (dx in 0..1) {
                for (dz in 0..1) {
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
                    chunk.sections.filterNotNull()
                        .forEach { c ->
                            for (i in 0 until c.biomes.size) {
                                c.biomes[i] = netherBiome
                            }
                        }
                    val encoded = EncodedAsyncChunk.fromChunk(AW.codec, chunk, dx, dz)
                    chunkList.add(encoded)
                }
            }
        }

        val packed = PackedWorld(chunkList)

        val millis = measureTimeMillis {

            currWorld = AW.inMemoryWorldProvider.createWorld(
                "Test-World-${UUID.randomUUID()}",
                InMemoryWorldOptions(
                    0..1,
                    0..1,
                    World.Environment.NORMAL,
                    Biome.CRIMSON_FOREST,
                    AW.compressedCodec
                ),
                packed
            )
        }

        tell("Saved chunks in $saveChunksMillis ms")
        tell("Created world in $millis ms")

        spigotPlayer.teleport(
            Location(
                currWorld!!.bukkitWorld,
                0.0,
                100.0,
                0.0
            )
        )
    }
}