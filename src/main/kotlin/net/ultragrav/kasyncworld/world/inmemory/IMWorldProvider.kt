package net.ultragrav.kasyncworld.world.inmemory

import net.ultragrav.kasyncworld.world.inmemory.pack.PackedWorld

interface IMWorldProvider {
    fun createWorld(name: String, options: InMemoryWorldOptions): InMemoryWorld
    fun createWorld(name: String, options: InMemoryWorldOptions, packed: PackedWorld): InMemoryWorld
}