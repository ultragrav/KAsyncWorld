package net.ultragrav.kasyncworld.world.chunk.contract.section

interface AsyncChunkSectionFactory {
    fun createSection(): AsyncChunkSection
}