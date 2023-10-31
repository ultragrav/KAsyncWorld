package net.ultragrav.kasyncworld.world.chunk

data class ChunkHeightOptions(val numSections: Int, val minSection: Int) {
    val minBuildHeight: Int get() = minSection shl 4
    val maxBuildHeight: Int get() = height + minBuildHeight
    val height: Int get() = numSections shl 4
}