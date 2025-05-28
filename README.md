# AsyncWorld

## Purpose
The purpose of this project is to provide a representation of a minecraft world that can be modified and processed asynchronously and off of the main thread. Furthermore it is intended to provide a way
to edit large parts of the world blazingly fast, and without causing lag. I also needed this project to provide an API for creating in-memory worlds, which are worlds in the game that are not backed
by any files or folders on the host machine, but are stored completely in-memory and are volatile, leaving no trace behind after unloading. This project provides such an API and also provides a custom
codec to serialize and deserialize these in-memory worlds in milliseconds, ensuring quick saving and loading functionality.

## Features
- Asynchronous manipulation of worlds
- Fast write-back to real world (~40 million blocks/sec)
- In-memory world creation and handling
- Blazingly fast custom world codec for in-memory worlds

## Usage
Let's start with some examples first.
This is a snippet from the source code of MidnightSky (some parts omitted for brevity)
```kotlin
val world = adv.map.world
val aw = AW.createAsyncWorld(world, AsyncWorld.EditType.SPARSE)

val sc = ..
val pasteAt = ..
Schematics.paste(sc, aw, pasteAt.x, pasteAt.y, pasteAt.z)

box = ..

// Kick out the people inside
..

aw.syncFlush()
```

This is another snippet from MidnightSky
```kotlin
fun destroyPrepStation() {
    val poi = map.data.poiList.first { it.type == WarMapPoiType.PREP_STATION }

    val dims = ..
    val x = poi.x.toInt() - dims.x / 2
    val y = poi.y.toInt() - 1
    val z = poi.z.toInt() - dims.z / 2

    val aw = AW.createAsyncWorld(world.bukkitWorld, AsyncWorld.EditType.SPARSE)
    val corner1 = Vec3i(x, y, z)
    val corner2 = corner1 + dims
    val box = corner1 box corner2

    aw.setBox(box, Material.AIR.createBlockData())
    aw.flush()
}
```

As you can see it is extremely simple to use. You might notice that aw.syncFlush() is used in the first snippet yet aw.flush() in the following one. The difference is that aw.flush() adds the changes to a queue
 that will ~eventually~ write the changes back to the real world (usually quite fast), the benefit though, is that in the case that the edit is large, the write-back will be split across multiple game ticks so as to 
 avoid lagging the server. syncFlush() however, guarantees that the edits are written back completely before it returns.

 The following snippet shows how you can create an in-memory world.

 ```kotlin
customWorld = AW.inMemoryWorldProvider.createWorld(
    "island-$croppedIslandIdStr-${type.name.lowercase()}",
    InMemoryWorldOptions(
        0 until type.size,
        0 until type.size,
        worldEnv,
        codec = AW.compressedCodec
    ),
    currentSave
)
```

Where currentSave is of type PackedWorld and can be obtained as such:
```kotlin
customWorld.unload(true) // true for save
currentSave = customWorld.saveAndPack()
```

saveAndPack() can also be called while the world is loaded. It is unloaded first in this case to avoid any modifications to the world being missed before it is unloaded.

## How it works
AsyncWorld objects comprise of a map of AsyncChunk objects, each of which contain AsyncChunkSection objects which store the real changes. 

### Write-back
All AsyncChunk objects can be written back to the world in the write-back process.
- Blocks are transferred from AsyncChunkSection to Minecraft Chunk Section while still compressed to maximize transfer speed.
- There are numerous other optimizations in the chunk write process that I will not cover here.
- When multiple chunks must be written back from a single AsyncWorld on a multi-core system, KAsyncWorld will write multiple chunks in parallel by multi-threading the writing of non-overlapping chunk edits.
- Write-back speeds are fast, usually **0.25-1ms** per chunk during normal operation.

### AsyncChunkSection
- Blocks are stored compressed, taking up as little as 4 bits per block and depending on the number of unique block types in a section (16x16x16 cube).
- The block information is stored in the exact format Minecraft internally stores blocks, this is done intentionally to make write-back as fast as possible.
- Sections not only track what blocks they are comprised of but also what blocks were actually changed
- Sections can be iterated over and will only iterate over the blocks that have actually been modified. Depending on the type of section,
this may default to every block, but for most types (like what is given to you when you use EditType.SPARSE), the iteration order will be in the order the blocks were modified and furthermore only cover
modified blocks, as the changes are tracked (in one of three ways depending on usage).

#### LinkedIterationStrategy
When you modify a block in an AsyncWorld that supports change tracking using Linked Iteration (most of them), if the block you changed has not already been modified, it will be linked into a 4096-entry array that acts
as space for a linked list. When iterating over the modified section, the iterator will walk this linked list, covering the modified blocks in the order they were modified. This strategy
is the default because it is the most performant in the average case. It is very quick for write-back as only changed blocks are iterated over and there is no seek time for finding the next
changed block as there is with Flag Iteration which we will cover below.

#### FlagIterationStrategy
When using Flag Iteration, iteration is not ordered, and the changes are tracked using a bitmap of size 4096, stored as 512 longs. No guarantees are made on the iteration order, but only modified blocks
will be iterated over. This strategy uses less memory than Linked Iteration but is often less performant in write-back because the whole bitmap must be iterated over.

#### NormalIterationStrategy
This is the simplest, iteration covers every block, modified or not, in a predictable order.

### In-Memory Worlds
- In-memory worlds are created with speed by skipping much of the setup that Minecraft usually performs, on the basis that much of it is unneeded and irrelevant for worlds used by projects that this library
is meant to serve
- There is no chunk generation by default in these worlds.
- Chunks that are not in use by the Minecraft internals are cached in an encoded format (and potentially compressed) to save memory.
- Chunk loading by Minecraft internals is extremely fast as the chunks need not be loaded from disk, and are instead quickly uncompressed and decoded from memory.
- The chunk codec can be customized but a compact and fast implementation is provided as AW.compressedCodec.
