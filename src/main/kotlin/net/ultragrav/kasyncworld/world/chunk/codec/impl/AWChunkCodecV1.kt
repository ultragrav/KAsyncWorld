package net.ultragrav.kasyncworld.world.chunk.codec.impl

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.ticks.SavedTick
import net.ultragrav.kasyncworld.data.DataReader
import net.ultragrav.kasyncworld.data.DataWriter
import net.ultragrav.kasyncworld.deserializeNBT
import net.ultragrav.kasyncworld.serializeNBT
import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions
import net.ultragrav.kasyncworld.world.chunk.codec.ChunkCodec
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunkFactory
import net.ultragrav.kasyncworld.world.chunk.getSectionIndexMB
import org.bukkit.block.Biome
import org.bukkit.craftbukkit.v1_20_R2.block.CraftBiome

/**
 * This codec improves on the previous version by adding flags
 * so that we don't have to serialize empty sections.
 */
class AWChunkCodecV1 : ChunkCodec {

    override val id: String
        get() = "aw"
    override val version: Int
        get() = 1

    override fun earlierVersion() = AWChunkCodecV0()

    override fun encode(writer: DataWriter, chunk: AsyncChunk) {
        writer.writeInt(chunk.heightOptions.numSections)
        writer.writeInt(chunk.heightOptions.minSection)

        // Section mask
        chunk.sections
            .map { it != null }
            .forEach { writer.writeBoolean(it) }

        // Section data
        chunk.sections.filterNotNull().forEach { section ->
            val blocks = section.blocks
            val biomes = section.biomes

            val blocksEmpty = blocks.count(Blocks.AIR.defaultBlockState()) == blocks.size &&
                    blocks.iterationStrategy.count == blocks.size
            val biomesEmpty = biomes.count(CraftBiome.bukkitToMinecraftHolder(Biome.PLAINS)) == biomes.size &&
                    biomes.iterationStrategy.count == biomes.size

            writer.writeBoolean(blocksEmpty)
            writer.writeBoolean(biomesEmpty)

            if (!blocksEmpty) blocks.write(writer)
            if (!biomesEmpty) biomes.write(writer)
        }

        // Block Entities
        writer.writeInt(chunk.blockEntities.size)
        chunk.blockEntities.forEach {
            writer.writeByte(it.key.x.toByte())
            writer.writeShort(it.key.y.toShort())
            writer.writeByte(it.key.z.toByte())
            writer.writeByteArray(serializeNBT(it.value))
        }

        // Entities
        writer.writeInt(chunk.entities.size)
        chunk.entities.forEach { writer.writeByteArray(serializeNBT(it)) }

        // Persistent data
        writer.writeByteArray(serializeNBT(chunk.persistentData))

        // Block and fluid ticks
        writer.writeInt(chunk.blockTicks.size)
        chunk.blockTicks.map {
            it.save { key ->
                BuiltInRegistries.BLOCK.getKey(key).toString()
            }
        }.forEach { writer.writeByteArray(serializeNBT(it)) }

        writer.writeInt(chunk.fluidTicks.size)
        chunk.fluidTicks.map {
            it.save { key ->
                BuiltInRegistries.FLUID.getKey(key).toString()
            }
        }.forEach { writer.writeByteArray(serializeNBT(it)) }

        // Height maps
        writer.writeInt(chunk.heightMaps.size)
        chunk.heightMaps.forEach { (type, ahm) ->
            writer.writeInt(type.ordinal)
            for (x in 0..15) {
                for (z in 0..15) {
                    writer.writeShort(ahm.getHeight(x, z).toShort())
                }
            }
        }

    }

    override fun decode(reader: DataReader, factory: AsyncChunkFactory): AsyncChunk {
        val numSections = reader.readInt()
        val minSection = reader.readInt()

        val heightOptions = ChunkHeightOptions(numSections, minSection)
        val chunk = factory.createChunk(heightOptions)

        // Read sections
        val sectionExists = (0 until numSections).map { reader.readBoolean() }
        sectionExists.forEachIndexed { index, exists ->
            if (exists) {
                val section = chunk.createSection()

                val blocksEmpty = reader.readBoolean()
                val biomesEmpty = reader.readBoolean()

                if (!blocksEmpty) section.blocks.read(reader)
                else if (section.blocks.count(Blocks.AIR.defaultBlockState()) != section.blocks.size) {
                    section.blocks.fill(Blocks.AIR.defaultBlockState())
                } else {
                    section.blocks.iterationStrategy.setAll()
                }
                if (!biomesEmpty) section.biomes.read(reader)
                else if (section.biomes.count(CraftBiome.bukkitToMinecraftHolder(Biome.PLAINS)) != section.biomes.size) {
                    section.biomes.fill(CraftBiome.bukkitToMinecraftHolder(Biome.PLAINS))
                } else {
                    section.biomes.iterationStrategy.setAll()
                }
                chunk.setSection(heightOptions.getSectionIndexMB(index), section)
            }
        }

        // Read block entities
        val blockEntityCount = reader.readInt()
        repeat(blockEntityCount) {
            val x = reader.readByte().toInt()
            val y = reader.readShort().toInt()
            val z = reader.readByte().toInt()
            val nbt = deserializeNBT(reader.readByteArray())
            chunk.setBlockEntity(x, y, z, nbt)
        }

        // Read entities
        val entityCount = reader.readInt()
        repeat(entityCount) {
            val nbt = deserializeNBT(reader.readByteArray())
            chunk.addEntity(nbt)
        }

        // Read persistent data
        chunk.persistentData = deserializeNBT(reader.readByteArray())

        // Read block and fluid ticks
        val blockTickCount = reader.readInt()
        repeat(blockTickCount) {
            val tickNbt = deserializeNBT(reader.readByteArray())
            SavedTick.loadTick(tickNbt) {
                BuiltInRegistries.BLOCK.getOptional(ResourceLocation.tryParse(it))
            }.ifPresent {
                chunk.blockTicks.add(it)
            }
        }
        val fluidTickCount = reader.readInt()
        repeat(fluidTickCount) {
            val tickNbt = deserializeNBT(reader.readByteArray())
            SavedTick.loadTick(tickNbt) {
                BuiltInRegistries.FLUID.getOptional(ResourceLocation.tryParse(it))
            }.ifPresent {
                chunk.fluidTicks.add(it)
            }
        }

        // Read height maps
        val heightMapCount = reader.readInt()
        repeat(heightMapCount) {
            val typeOrdinal = reader.readInt()
            val type = Heightmap.Types.values()[typeOrdinal]
            val heightMap = chunk.getHeightMap(type)
            for (x in 0..15) {
                for (z in 0..15) {
                    heightMap.setHeight(x, z, reader.readShort().toInt())
                }
            }
        }

        return chunk
    }

}