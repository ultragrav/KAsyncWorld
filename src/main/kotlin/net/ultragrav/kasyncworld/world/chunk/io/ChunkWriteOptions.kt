package net.ultragrav.kasyncworld.world.chunk.io

data class ChunkWriteOptions(
    val appendEntities: Boolean = false,
    val heightmapWriteType: HeightmapWriteType = HeightmapWriteType.RECALCULATE,
    val writePersistentContainer: Boolean = true,
    val sendPackets: Boolean = true,
    val relight: Boolean = true,
)

enum class HeightmapWriteType {
    OVERWRITE,
    MERGE,
    RECALCULATE,
}