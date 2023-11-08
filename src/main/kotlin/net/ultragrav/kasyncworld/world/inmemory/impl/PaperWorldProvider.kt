package net.ultragrav.kasyncworld.world.inmemory.impl

import net.minecraft.server.level.ServerLevel
import net.ultragrav.kasyncworld.world.inmemory.*
import org.bukkit.World.Environment

class PaperWorldProvider : WorldProvider {

    private fun createWorld(name: String, environment: Environment, chunkProvider: AsyncChunkProvider): ServerLevel {
        ServerLevel
    }

    override fun createWorld(name: String, options: InMemoryWorldOptions): InMemoryWorld {

    }

    override fun createWorld(name: String, options: InMemoryWorldOptions, packed: PackedWorld): InMemoryWorld {

    }
}