package net.ultragrav.kasyncworld

import net.minecraft.core.Vec3i
import net.minecraft.nbt.*
import net.minecraft.world.phys.Vec3
import net.ultragrav.kasyncworld.world.chunk.block.position.AWBlockPosition
import java.io.ByteArrayOutputStream
import java.io.DataInput
import java.io.DataInputStream
import java.io.DataOutput
import java.io.DataOutputStream

fun getChunkKey(x: Int, z: Int): Long {
    return (x.toLong() and 0xFFFFFFFFL) or ((z.toLong() and 0xFFFFFFFFL) shl 32)
}

fun getChunkX(key: Long): Int {
    return (key and 0xFFFFFFFFL).toInt()
}

fun getChunkZ(key: Long): Int {
    return (key ushr 32).toInt()
}

fun ceilLog2(num: Int): Int {
    return 32 - Integer.numberOfLeadingZeros(num - 1)
}

internal fun serializeNBT(tag: CompoundTag): ByteArray {
    val bos = ByteArrayOutputStream()
    val dos = DataOutputStream(bos)
    NbtIo.write(tag, dos)
    dos.close()
    return bos.toByteArray()
}

internal fun deserializeNBT(arr: ByteArray): CompoundTag {
    val bis = arr.inputStream()
    val dis = DataInputStream(bis)
    return NbtIo.read(dis)
}

internal fun serializeNBTOrdered(tag: CompoundTag): ByteArray {
    val bos = ByteArrayOutputStream()
    val dos = DataOutputStream(bos)
    serializeNBTOrderedWriter(tag, dos)
    dos.close()
    return bos.toByteArray()
}

internal fun deserializeNBTOrdered(arr: ByteArray): CompoundTag {
    val bis = arr.inputStream()
    val dis = DataInputStream(bis)
    return deserializeNBTOrderedReader(dis, NbtAccounter.unlimitedHeap()) as CompoundTag
}

private fun serializeNBTOrderedWriter(tag: Tag, writer: DataOutput) {
    when (tag) {
        is CompoundTag -> {
            val tags = tag.tags
            writer.writeShort(tags.size)
            tags.entries.sortedBy { it.key }
                .forEach {
                    writer.writeByte(it.value.id.toInt())
                    writer.writeUTF(it.key)
                    serializeNBTOrderedWriter(it.value, writer)
                }
        }
        is ListTag -> {
            val list = tag.toList()
            writer.writeInt(list.size)
            list.forEach { serializeNBTOrderedWriter(it, writer) }
        }
        else -> tag.write(writer)
    }
}

private fun deserializeNBTOrderedReader(reader: DataInput, accounting: NbtAccounter): Tag {
    return when (val id = reader.readByte()) {
        Tag.TAG_COMPOUND -> {
            val comp = CompoundTag()
            val size = reader.readShort()
            for (i in 0..<size) {
                val key = reader.readUTF()
                val tag = deserializeNBTOrderedReader(reader, accounting)
                comp.put(key, tag)
            }
            comp
        }
        Tag.TAG_LIST -> {
            val size = reader.readInt()
            val list = ListTag()
            for (i in 0..<size) {
                list.add(deserializeNBTOrderedReader(reader, accounting))
            }
            list
        }
        else -> TagTypes.getType(id.toInt()).load(reader, accounting)
    }
}

fun CompoundTag.entityPosition(): Vec3 {
    val pos = this.getList("Pos", Tag.TAG_DOUBLE.toInt())
    val x = pos.getDouble(0)
    val y = pos.getDouble(1)
    val z = pos.getDouble(2)
    return Vec3(x, y, z)
}

fun CompoundTag.setEntityPosition(position: Vec3) {
    val pos = this.getList("Pos", Tag.TAG_DOUBLE.toInt())
    pos[0] = DoubleTag.valueOf(position.x)
    pos[1] = DoubleTag.valueOf(position.y)
    pos[2] = DoubleTag.valueOf(position.z)
}

fun Vec3.floor(): Vec3i {
    return Vec3i(this.x.toInt(), this.y.toInt(), this.z.toInt())
}

fun Vec3i.toAWBlockPos(): AWBlockPosition {
    return AWBlockPosition(this.x, this.y, this.z)
}