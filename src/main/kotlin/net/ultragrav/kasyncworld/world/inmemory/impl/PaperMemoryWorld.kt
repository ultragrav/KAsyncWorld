package net.ultragrav.kasyncworld.world.inmemory.impl

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.ultragrav.kasyncworld.world.chunk.serialization.ChunkCodec
import net.ultragrav.kasyncworld.world.contract.AsyncChunkFactory
import net.ultragrav.kasyncworld.world.inmemory.AsyncChunkProvider
import net.ultragrav.kasyncworld.world.inmemory.CompressedAsyncChunk
import net.ultragrav.kasyncworld.world.inmemory.InMemoryWorld
import net.ultragrav.kasyncworld.world.inmemory.InMemoryWorldOptions
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator

class PaperMemoryWorld(
    private val world: ServerLevel,
    override val name: String,
    override val options: InMemoryWorldOptions,
    override val chunkProvider: AsyncChunkProvider,
) : InMemoryWorld {

    override val bukkitWorld: World
        get() = world.world

    override fun unload(save: Boolean) {
        require(Bukkit.getWorld(world.uuid) != null) {
            "World $name is not loaded"
        }
        Bukkit.unloadWorld(bukkitWorld, save)
    }

}