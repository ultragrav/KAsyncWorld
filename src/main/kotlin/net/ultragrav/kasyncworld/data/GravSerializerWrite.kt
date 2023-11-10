package net.ultragrav.kasyncworld.data

import net.ultragrav.serializer.GravSerializer

class GravSerializerWrite(private val ser: GravSerializer) : DataWriter {
    override fun writeInt(value: Int) {
        ser.writeInt(value)
    }

    override fun writeByte(value: Byte) {
        ser.writeByte(value)
    }

    override fun writeShort(value: Short) {
        ser.writeShort(value)
    }

    override fun writeLong(value: Long) {
        ser.writeLong(value)
    }

    override fun writeFloat(value: Float) {
        ser.writeFloat(value)
    }

    override fun writeDouble(value: Double) {
        ser.writeDouble(value)
    }

    override fun writeBoolean(value: Boolean) {
        ser.writeBoolean(value)
    }

    override fun writeString(value: String) {
        ser.writeString(value)
    }

    override fun writeByteArray(value: ByteArray) {
        ser.writeByteArray(value)
    }

    override fun writeChar(value: Char) {
        ser.writeChar(value)
    }

    override fun writeIntArray(value: IntArray) {
        writeInt(value.size) // Write the length of the array first
        value.forEach { writeInt(it) }
    }

    override fun writeLongArray(value: LongArray) {
        writeInt(value.size) // Write the length of the array first
        value.forEach { writeLong(it) }
    }

    override fun toByteArray(): ByteArray {
        return ser.toByteArray()
    }
}
