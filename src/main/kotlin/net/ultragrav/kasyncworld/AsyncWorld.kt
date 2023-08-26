package net.ultragrav.kasyncworld

import org.bukkit.World
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld

class AsyncWorld internal constructor(val world: World) {
    internal val handle = (world as CraftWorld).handle
    val chunkMap = mutableMapOf<Long, AsyncChunk>()

    fun getChunk(x: Int, z: Int) {

    }

    private var lockedLevel = 0

    /**
     * Lock the world, this prevents it from being deleted if no chunk changes are present
     */
    fun lock() {
        lockedLevel++
    }

    /**
     * Unlock the world, should only be called exactly once after lock(), once fully unlocked, the world may be
     * removed from the cached AsyncWorld list if no more chunk updates are present
     */
    fun unlock() {
        lockedLevel--
    }
}