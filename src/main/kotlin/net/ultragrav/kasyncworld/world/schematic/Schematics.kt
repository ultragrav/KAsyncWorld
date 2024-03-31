package net.ultragrav.kasyncworld.world.schematic

import net.minecraft.world.level.ChunkPos
import net.minecraft.world.phys.Vec3
import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.entityPosition
import net.ultragrav.kasyncworld.setEntityPosition
import net.ultragrav.kasyncworld.shape.CuboidRegion
import net.ultragrav.kasyncworld.shape.ShapedRegion
import net.ultragrav.kasyncworld.world.chunk.block.position.AWBlockPosition
import net.ultragrav.kasyncworld.world.chunk.codec.ChunkCodec
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.chunk.io.ChunkReadOptions
import net.ultragrav.kasyncworld.world.contract.AsyncWorld
import net.ultragrav.kasyncworld.world.schematic.impl.SchematicImplV0
import org.bukkit.World

object Schematics : SchematicsApi {

    override fun create(dimensions: Dimensions): Schematic {
        return SchematicImplV0(dimensions)
    }

    override fun paste(schematic: Schematic, world: AsyncWorld, x: Int, y: Int, z: Int) {

        fun convertToChunkRelative(vec: Vec3): Pair<AsyncChunk, Vec3> {
            val gp = vec.add(x.toDouble(), y.toDouble(), z.toDouble())
            val cx = gp.x.toInt() shr 4
            val cz = gp.z.toInt() shr 4
            val chunk = world.getChunk(cx, cz)
            val rx = gp.x - (cx shl 4)
            val rz = gp.z - (cz shl 4)
            return chunk to Vec3(rx, gp.y, rz)
        }

        fun convertToChunkRelative(vec: AWBlockPosition): Pair<AsyncChunk, AWBlockPosition> {
            val gp = vec + AWBlockPosition(x, y, z)
            val cx = gp.x shr 4
            val cz = gp.z shr 4
            val chunk = world.getChunk(cx, cz)
            val rx = gp.x and 0xF
            val rz = gp.z and 0xF
            return chunk to AWBlockPosition(rx, gp.y, rz)
        }

        // Write entities
        schematic.entities().forEach {
            val pos = it.entityPosition()
            val (chunk, rp) = convertToChunkRelative(pos)
            val cpy = it.copy()
            cpy.setEntityPosition(Vec3(rp.x, rp.y, rp.z))
            chunk.addEntity(it)
        }

        // Write block entities
        schematic.blockEntities().forEach { (pos, ent) ->
            val (chunk, rp) = convertToChunkRelative(pos)
            chunk.setBlockEntity(rp.x, rp.y, rp.z, ent)
        }

        // Write blocks
        schematic.iterator().forEach { (bx, by, bz, state) ->
            world.setBlock(
                bx + x,
                by + y,
                bz + z,
                state
            )
        }
    }


    override fun save(world: World, region: ShapedRegion): Schematic {
        val opts = ChunkReadOptions(
            readBiomes = false,
            readPersistentContainer = false,
            readHeightmaps = false,
            readLight = false,
            readTicks = false,
        )
        val aw = importBlocks(world, region.boundingBox, opts)

        return save(aw, region)
    }

    override fun save(aw: AsyncWorld, region: ShapedRegion): Schematic {
        val schematic = SchematicImplV0(region.boundingBox.dimensions)

        val bx = region.boundingBox.min.x
        val by = region.boundingBox.min.y
        val bz = region.boundingBox.min.z

        region.forEach { (x, y, z) ->
            val block = aw.getBlock(x, y, z)
            schematic.setBlock(
                x - bx,
                y - by,
                z - bz,
                block
            )
            val be = aw.getBlockEntity(x, y, z) ?: return@forEach
            schematic.setBlockEntity(
                x - bx,
                y - by,
                z - bz,
                be
            )
        }

        return schematic
    }

    override fun importBlocks(world: World, region: CuboidRegion, readOptions: ChunkReadOptions): AsyncWorld {
        // DENSE doesn't matter as we will be making our own chunks
        val aw = AW.createAsyncWorld(world, AsyncWorld.EditType.DENSE)

        val factory = AW.storageChunkFactory
        val chunkRangeX = (region.min.x shr 4)..(region.max.x shr 4)
        val chunkRangeZ = (region.min.z shr 4)..(region.max.z shr 4)
        val sectionRangeY = (region.min.y shr 4)..(region.max.y shr 4)

//        runBlocking(AW.parallelDispatcher) {
//
//            val waitFor = mutableListOf<Deferred<Runnable>>()
//            for (cx in chunkRangeX) {
//                for (cz in chunkRangeZ) {
//                    val runnable: Deferred<Runnable> = async {
//                        val bukkitChunk = world.getChunkAt(cx, cz)
//                        val chunk = AW.chunkIO.readChunk(
//                            bukkitChunk,
//                            factory,
//                            readOptions.copy(sectionMask = sectionRangeY)
//                        )
//
//                        Runnable {
//                            aw.setChunk(cx, cz, chunk)
//                        }
//                    }
//                    waitFor.add(runnable)
//                }
//            }
//
//            waitFor.awaitAll().forEach { it.run() }
//
//        }

        for (cx in chunkRangeX) {
            for (cz in chunkRangeZ) {
                val bukkitChunk = world.getChunkAt(cx, cz)
                val chunk = AW.chunkIO.readChunk(
                    bukkitChunk,
                    factory,
                    readOptions.copy(sectionMask = sectionRangeY)
                )
                aw.setChunk(cx, cz, chunk)
            }
        }

        return aw
    }

    override fun serialize(schematic: Schematic): ByteArray {
        require(schematic is SchematicImplV0) { "Only schematics of type SchematicImplV0 can be serialized" }

        val writer = AW.createWriter()
        val codec = AW.compressedCodec
        writer.writeByte(0)
        writer.writeString(codec.id)
        writer.writeInt(codec.version)
        writer.writeInt(schematic.dimensions.x)
        writer.writeInt(schematic.dimensions.y)
        writer.writeInt(schematic.dimensions.z)

        schematic.chunks.forEach { (pos, chunk) ->
            writer.writeByte(1)
            writer.writeLong(pos.toLong())
            codec.encode(writer, chunk)
        }
        writer.writeByte(0)

        return writer.toByteArray()
    }

    override fun deserialize(bytes: ByteArray): Schematic {
        val reader = AW.createReader(bytes)
        var codec: ChunkCodec = AW.compressedCodec

        reader.readByte() // Version

        val codecId = reader.readString()
        val codecVersion = reader.readInt()
        require(codecId == codec.id) { "Unsupported codec $codecId" }
        while (codec.version != codecVersion) codec =
            codec.earlierVersion() ?: error("Unsupported codec version $codecVersion")

        val dimX = reader.readInt()
        val dimY = reader.readInt()
        val dimZ = reader.readInt()

        val schematic = SchematicImplV0(Dimensions(dimX, dimY, dimZ))
        while (reader.readByte() == 1.toByte()) {
            val pos = ChunkPos(reader.readLong())
            val chunk = codec.decode(reader, AW.editingChunkFactory)
            schematic.chunks[pos] = chunk
        }

        return schematic
    }
}