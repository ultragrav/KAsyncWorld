package net.ultragrav.kasyncworld.world.inmemory

interface IMWorldProvider {
    fun createWorld(name: String, options: InMemoryWorldOptions): InMemoryWorld
    fun createWorld(name: String, options: InMemoryWorldOptions, packed: PackedWorld): InMemoryWorld
}