package net.ultragrav.kasyncworld

import net.ultragrav.kasyncworld.world.SpigotAsyncWorld
import org.bukkit.World
import org.bukkit.plugin.Plugin

object AWApi {
    private val worlds = mutableMapOf<String, SpigotAsyncWorld>()

    fun initialize(plugin: Plugin) {

    }

    fun getWorld(world: World): SpigotAsyncWorld {
        return worlds[world.name] ?: SpigotAsyncWorld(world).also { worlds[world.name] = it }
    }
}