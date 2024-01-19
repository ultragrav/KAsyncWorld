package net.ultragrav.kasyncworld.world.schematic

import net.ultragrav.kasyncworld.shape.CuboidRegion
import net.ultragrav.kasyncworld.shape.ShapedRegion
import net.ultragrav.kasyncworld.world.chunk.io.ChunkReadOptions
import net.ultragrav.kasyncworld.world.contract.AsyncWorld
import org.bukkit.World

interface SchematicsApi {
    fun paste(schematic: Schematic, world: AsyncWorld, x: Int, y: Int, z: Int)
    fun save(world: World, region: ShapedRegion): Schematic
    fun importBlocks(world: World, region: CuboidRegion, readOptions: ChunkReadOptions): AsyncWorld
    fun serialize(schematic: Schematic): ByteArray
    fun deserialize(bytes: ByteArray): Schematic
}