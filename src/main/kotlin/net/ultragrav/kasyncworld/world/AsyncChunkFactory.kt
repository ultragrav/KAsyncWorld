package net.ultragrav.kasyncworld.world

interface AsyncChunkFactory {
    fun createChunk(): AsyncChunk
}