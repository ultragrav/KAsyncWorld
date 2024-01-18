package net.ultragrav.kasyncworld.world.chunk.io

/**
 * Options for reading from a chunk
 * @param readEntities Whether to read entities
 * @param readBlocks Whether to read blocks
 * @param readBiomes Whether to read biomes
 * @param readBlockEntities Whether to read block entities
 * @param readPersistentContainer Whether to read persistent container
 * @param readHeightmaps Whether to read heightmaps
 * @param readLight Whether to read light
 * @param readTicks Whether to read block ticks
 * @param sectionMask A mask specifying which sections to read from. The mask applies to
 * blocks, biomes, ticks, light, block entities, and entities.
 */
data class ChunkReadOptions(
    val readEntities: Boolean = true,
    val readBlocks: Boolean = true,
    val readBiomes: Boolean = true,
    val readBlockEntities: Boolean = true,
    val readPersistentContainer: Boolean = true,
    val readHeightmaps: Boolean = true,
    val readLight: Boolean = true,
    val readTicks: Boolean = true,
    val sectionMask: IntRange = Integer.MIN_VALUE..Integer.MAX_VALUE,
)
