package net.ultragrav.kasyncworld.world.versionio

data class ChunkWriteOptions(
    val appendEntities: Boolean = false,
    val heightmapWriteType: HeightmapWriteType = HeightmapWriteType.OVERWRITE,
    val sendPackets: Boolean = true,
)

enum class HeightmapWriteType {
    OVERWRITE,
    MERGE,
    IGNORE,
}