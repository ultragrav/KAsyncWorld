package net.ultragrav.kasyncworld.world.inmemory.impl

import net.minecraft.server.level.ServerLevel
import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.world.inmemory.chunk.AsyncChunkProvider
import net.ultragrav.kasyncworld.world.inmemory.InMemoryWorld
import net.ultragrav.kasyncworld.world.inmemory.InMemoryWorldOptions
import net.ultragrav.kasyncworld.world.inmemory.pack.PackedWorld
import net.ultragrav.kasyncworld.world.chunk.io.ChunkReadOptions
import org.bukkit.Bukkit
import org.bukkit.World
import kotlin.system.measureNanoTime

class PaperMemoryWorld(
    private val world: ServerLevel,
    override val name: String,
    override val options: InMemoryWorldOptions,
    override val chunkProvider: AsyncChunkProvider,
) : InMemoryWorld {

    override val bukkitWorld: World
        get() = world.world

    init {
        AW.debug("World has dimension id: ${world.dimensionTypeRegistration().unwrapKey().get()} ${world.dimensionType()}")
    }

    override fun unload(save: Boolean) {
        require(Bukkit.getWorld(world.uuid) != null) {
            "World $name is not loaded"
        }
        if (!Bukkit.unloadWorld(bukkitWorld, save)) error("Failed to unload world $name (there are ${bukkitWorld.playerCount} players in the world)")
    }

    override fun saveAndPack(): PackedWorld {
        if (Bukkit.getWorld(bukkitWorld.uid) != null) {
            bukkitWorld.loadedChunks
                .filter { it.x in chunkProvider.boundsX }
                .filter { it.z in chunkProvider.boundsZ }
                .forEach { bukkitChunk ->
                    val time = measureNanoTime {
                        val readChunk = AW.chunkIO.readChunk(
                            bukkitChunk,
                            chunkProvider.factory,
                            ChunkReadOptions()
                        )
                        chunkProvider.storeChunk(bukkitChunk.x, bukkitChunk.z, readChunk)
                    }
                    val millis = time / 1000000.0
                    AW.debug("Saved chunk ${bukkitChunk.x} ${bukkitChunk.z} in $millis ms")
                }
        }
        return PackedWorld(chunkProvider.getChunks())
    }

}