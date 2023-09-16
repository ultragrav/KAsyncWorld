package net.ultragrav.kasyncworld.world.chunk.block.storage

import org.bukkit.block.data.BlockData

data class Indexed<T>(
    val index: Int,
    val subject: T
)
