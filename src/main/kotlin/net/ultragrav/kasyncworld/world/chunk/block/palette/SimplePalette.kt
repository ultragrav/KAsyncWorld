package net.ultragrav.kasyncworld.world.chunk.block.palette

class SimplePalette<T>(private val globalPalette: Palette<T>? = null) : Palette<T> {
    private val blockDataToIdMap = mutableMapOf<T, Int>()
    private val idToBlockDataMap = mutableMapOf<Int, T>()
    private var currentId = 0

    override val size: Int
        get() = blockDataToIdMap.size

    override fun getId(type: T): Int {
        return blockDataToIdMap.getOrPut(type) {
            val newId = currentId++
            idToBlockDataMap[newId] = type
            newId
        }
    }

    override fun getState(id: Int): T {
        return idToBlockDataMap[id] ?: throw IllegalArgumentException("No Type found for ID: $id")
    }

    override fun isMapped(type: T): Boolean {
        return blockDataToIdMap.containsKey(type)
    }

    override fun isMapped(id: Int): Boolean {
        return idToBlockDataMap.containsKey(id)
    }

    override fun globalPalette(): Palette<T> = globalPalette ?: this

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
