package net.ultragrav.kasyncworld.world

import org.bukkit.Material
import org.bukkit.World

class SpigotAsyncWorld internal constructor(val world: World) {

    init {
        world.setBlockData(0, 0, 0, Material.ACACIA_STAIRS.createBlockData())
    }

    val chunkMap = mutableMapOf<Long, AsyncChunk>()
}