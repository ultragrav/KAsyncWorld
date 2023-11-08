package net.ultragrav.kasyncworld.world.inmemory

interface WorldProvider {
    fun createWorld(name: String, options: InMemoryWorldOptions): InMemoryWorld
    fun createWorld(name: String, options: InMemoryWorldOptions, packed: PackedWorld): InMemoryWorld
}