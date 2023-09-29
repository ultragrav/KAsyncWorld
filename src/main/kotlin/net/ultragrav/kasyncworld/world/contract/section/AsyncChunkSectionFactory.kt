package net.ultragrav.kasyncworld.world.contract.section

interface AsyncChunkSectionFactory {
    fun createSection(): AsyncChunkSection
}