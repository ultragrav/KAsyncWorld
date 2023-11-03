package net.ultragrav.kasyncworld.world.versionio

data class ChunkWriteOptions(
    val appendEntities: Boolean = false,
    val sendPackets: Boolean = true
)