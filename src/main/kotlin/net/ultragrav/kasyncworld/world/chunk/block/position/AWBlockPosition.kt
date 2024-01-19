package net.ultragrav.kasyncworld.world.chunk.block.position

data class AWBlockPosition(val x: Int, val y: Int, val z: Int) {
    operator fun plus(other: AWBlockPosition): AWBlockPosition {
        return AWBlockPosition(x + other.x, y + other.y, z + other.z)
    }

    operator fun plus(num: Int): AWBlockPosition {
        return AWBlockPosition(x + num, y + num, z + num)
    }

    operator fun minus(other: AWBlockPosition): AWBlockPosition {
        return AWBlockPosition(x - other.x, y - other.y, z - other.z)
    }

    operator fun minus(num: Int): AWBlockPosition {
        return AWBlockPosition(x - num, y - num, z - num)
    }
}