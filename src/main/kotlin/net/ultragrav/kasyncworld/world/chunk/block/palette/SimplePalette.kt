package net.ultragrav.kasyncworld.world.chunk.block.palette

class SimplePalette<T>(private val globalPalette: Palette<T>? = null) : Palette<T> {
    private val blockDataToIdMap = mutableMapOf<T, Int>()
    private val idToBlockDataMap = mutableMapOf<Int, T>()
    private var currentId = 0

    override val size: Int
        get() = blockDataToIdMap.size

    override fun getId(subject: T): Int {
        return blockDataToIdMap.getOrPut(subject) {
            val newId = currentId++
            idToBlockDataMap[newId] = subject
            newId
        }
    }

    override fun getState(id: Int): T {
        return idToBlockDataMap[id] ?: throw IllegalArgumentException("No BlockData found for ID: $id")
    }

    override fun isMapped(subject: T): Boolean {
        return blockDataToIdMap.containsKey(subject)
    }

    override fun isMapped(id: Int): Boolean {
        return idToBlockDataMap.containsKey(id)
    }

    override fun globalPalette(): Palette<T> = globalPalette ?: this

    override fun localToGlobal(): Map<Int, Int> {
        return idToBlockDataMap.mapValues { (_, blockData) -> globalPalette().getId(blockData) }
    }

    override fun clone(): Palette<T> {
        val newPalette = SimplePalette(globalPalette)
        newPalette.blockDataToIdMap.putAll(blockDataToIdMap)
        newPalette.idToBlockDataMap.putAll(idToBlockDataMap)
        newPalette.currentId = currentId
        return newPalette
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
