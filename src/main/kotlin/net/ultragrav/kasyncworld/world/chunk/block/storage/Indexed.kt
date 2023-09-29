package net.ultragrav.kasyncworld.world.chunk.block.storage

data class Indexed<T>(
    val index: Int,
    val subject: T
)
