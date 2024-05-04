package net.ultragrav.kasyncworld.world.schematic

import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.shape.CuboidRegion
import net.ultragrav.kasyncworld.shape.ShapedRegion
import net.ultragrav.kasyncworld.world.chunk.codec.ChunkCodec
import net.ultragrav.kasyncworld.world.chunk.io.ChunkReadOptions
import net.ultragrav.kasyncworld.world.contract.AsyncWorld
import net.ultragrav.kasyncworld.world.schematic.mask.Mask
import org.bukkit.World

interface SchematicsApi {
    fun create(dimensions: Dimensions): Schematic
    fun paste(schematic: Schematic, world: AsyncWorld, x: Int, y: Int, z: Int, mask: Mask? = null)
    fun save(world: World, region: ShapedRegion): Schematic
    fun save(aw: AsyncWorld, region: ShapedRegion): Schematic
    fun importBlocks(world: World, region: CuboidRegion, readOptions: ChunkReadOptions): AsyncWorld
    fun importBlocks(world: World, aw: AsyncWorld, region: CuboidRegion, readOptions: ChunkReadOptions)
    fun serialize(schematic: Schematic, codec: ChunkCodec = AW.compressedCodec): ByteArray
    fun deserialize(bytes: ByteArray, codecProvider: (String) -> ChunkCodec = AW.defaultCodecResolver): Schematic
}