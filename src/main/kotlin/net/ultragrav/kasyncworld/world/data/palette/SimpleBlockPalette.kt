package net.ultragrav.kasyncworld.world.data.palette

import org.bukkit.block.data.BlockData

class SimpleBlockPalette(private val globalPalette: BlockPalette? = null) : BlockPalette {
    private val blockDataToIdMap = mutableMapOf<BlockData, Int>()
    private val idToBlockDataMap = mutableMapOf<Int, BlockData>()
    private var currentId = 0

    override val size: Int
        get() = blockDataToIdMap.size

    override fun getId(block: BlockData): Int {
        return blockDataToIdMap.getOrPut(block) {
            val newId = currentId++
            idToBlockDataMap[newId] = block
            newId
        }
    }

    override fun getState(id: Int): BlockData {
        return idToBlockDataMap[id] ?: throw IllegalArgumentException("No BlockData found for ID: $id")
    }

    override fun globalPalette(): BlockPalette = globalPalette ?: this

    override fun localToGlobal(): Map<Int, Int> {
        return idToBlockDataMap.mapValues { (_, blockData) -> globalPalette().getId(blockData) }
    }

    fun load(localToGlobal: Map<Int, Int>) {
        require(globalPalette() != this) { "Cannot load palette from self. Global palette must be configured." }
        localToGlobal.forEach { (localId, globalId) ->
            val blockData = globalPalette().getState(globalId)
            blockDataToIdMap[blockData] = localId
            idToBlockDataMap[localId] = blockData
        }
        currentId = localToGlobal.size
    }
}
