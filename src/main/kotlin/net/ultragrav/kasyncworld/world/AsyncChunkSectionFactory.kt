package net.ultragrav.kasyncworld.world

import net.ultragrav.kasyncworld.world.chunk.AsyncChunkSection

interface AsyncChunkSectionFactory {
    fun createSection(): AsyncChunkSection
}