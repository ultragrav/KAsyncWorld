package net.ultragrav.kasyncworld.world.chunk.block.storage

import org.bukkit.block.data.BlockData

data class IndexedBlockState(
    val index: Int,
    val block: BlockData
)
