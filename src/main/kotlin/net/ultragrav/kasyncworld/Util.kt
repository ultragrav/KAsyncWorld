package net.ultragrav.kasyncworld

fun getChunkKey(x: Int, z: Int): Long {
    return (x.toLong() and 0xFFFFFFFFL) or ((z.toLong() and 0xFFFFFFFFL) shl 32)
}

fun ceilLog2(num: Int): Int {
    return 32 - Integer.numberOfLeadingZeros(num - 1)
}