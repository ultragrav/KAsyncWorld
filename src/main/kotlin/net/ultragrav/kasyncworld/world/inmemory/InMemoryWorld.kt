package net.ultragrav.kasyncworld.world.inmemory

import org.bukkit.World

interface InMemoryWorld {

    val name: String
    val options: InMemoryWorldOptions

    val chunkProvider: AsyncChunkProvider

    val bukkitWorld: World

    fun unload(save: Boolean)
}