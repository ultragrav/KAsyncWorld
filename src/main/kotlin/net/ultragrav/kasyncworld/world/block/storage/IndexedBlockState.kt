package net.ultragrav.kasyncworld.world.block.storage

import org.bukkit.block.data.BlockData

data class IndexedBlockState(
    val index: Int,
    val block: BlockData
)
