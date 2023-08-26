package net.ultragrav.kasyncworld

import org.bukkit.World
import org.bukkit.plugin.Plugin

object AWApi {
    private val worlds = mutableMapOf<String, AsyncWorld>()

    fun initialize(plugin: Plugin) {

    }

    fun getWorld(world: World): AsyncWorld {
        return worlds[world.name] ?: AsyncWorld(world).also { worlds[world.name] = it }
    }
}