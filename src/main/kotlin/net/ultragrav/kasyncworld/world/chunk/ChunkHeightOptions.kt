package net.ultragrav.kasyncworld.world.chunk

data class ChunkHeightOptions(val numSections: Int, val minSection: Int) {
    val minBuildHeightInclusive: Int get() = minSection shl 4
    val maxBuildHeightExclusive: Int get() = height + minBuildHeightInclusive
    val height: Int get() = numSections shl 4
    val buildableYRange: IntRange = minBuildHeightInclusive..<maxBuildHeightExclusive
}

fun ChunkHeightOptions.getSectionIndexZB(sectionIndexMB: Int): Int {
    return sectionIndexMB - minSection
}

fun ChunkHeightOptions.getSectionIndexMB(sectionIndexZeroBased: Int): Int {
    return sectionIndexZeroBased + minSection
}