package net.ultragrav.kasyncworld.world.inmemory

import net.ultragrav.kasyncworld.world.inmemory.chunk.AsyncChunkProvider
import net.ultragrav.kasyncworld.world.inmemory.pack.PackedWorld
import org.bukkit.World

interface InMemoryWorld {

    val name: String
    val options: InMemoryWorldOptions

    val chunkProvider: AsyncChunkProvider

    val bukkitWorld: World

    fun unload(save: Boolean)

    fun saveAndPack(): PackedWorld
}