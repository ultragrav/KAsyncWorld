package net.ultragrav.kasyncworld.world.versionio

data class ChunkWriteOptions(
    val ignoreEmptySections: Boolean = false,
    val appendTiles: Boolean = false,
    val appendEntities: Boolean = false,
    val sendPackets: Boolean = true
)