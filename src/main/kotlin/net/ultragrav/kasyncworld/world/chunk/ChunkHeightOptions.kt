package net.ultragrav.kasyncworld.world.chunk

data class ChunkHeightOptions(val numSections: Int, val minSection: Int) {
    val minBuildHeight: Int get() = minSection shl 4
    val maxBuildHeight: Int get() = height + minBuildHeight
    val height: Int get() = numSections shl 4
}

fun ChunkHeightOptions.getSectionIndexZB(sectionIndexMB: Int): Int {
    return sectionIndexMB - minSection
}

fun ChunkHeightOptions.getSectionIndexMB(sectionIndexZeroBased: Int): Int {
    return sectionIndexZeroBased + minSection
}