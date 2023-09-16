package net.ultragrav.kasyncworld.world

import java.util.concurrent.locks.ReentrantLock

class AsyncChunk internal constructor(val world: SpigotAsyncWorld, val x: Int, val z: Int) {
}