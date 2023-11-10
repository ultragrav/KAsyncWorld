package net.ultragrav.kasyncworld.world.chunk.io

data class ChunkReadOptions(
    val readEntities: Boolean = true,
    val readBlocksAndBiomes: Boolean = true,
    val readBlockEntities: Boolean = true,
    val readPersistentContainer: Boolean = true,
    val readHeightmaps: Boolean = true,
    val readLight: Boolean = true,
    val readTicks: Boolean = true,
)
