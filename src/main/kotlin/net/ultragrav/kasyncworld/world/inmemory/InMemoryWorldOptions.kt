package net.ultragrav.kasyncworld.world.inmemory

import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions
import net.ultragrav.kasyncworld.world.chunk.codec.ChunkCodec
import org.bukkit.World.Environment
import org.bukkit.block.Biome
import org.bukkit.generator.ChunkGenerator

data class InMemoryWorldOptions(
    val chunkBoundsX: IntRange,
    val chunkBoundsZ: IntRange,
    val environment: Environment,
    val defaultBiome: Biome = Biome.PLAINS,
    val codec: ChunkCodec = AW.compressedCodec,
    val generator: ChunkGenerator? = null,
    val generateStructures: Boolean = true,
    val seed: Long = 0,
)