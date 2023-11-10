package net.ultragrav.kasyncworld.data

interface DataWriter {
    fun writeInt(value: Int)
    fun writeByte(value: Byte)
    fun writeShort(value: Short)
    fun writeLong(value: Long)
    fun writeFloat(value: Float)
    fun writeDouble(value: Double)
    fun writeBoolean(value: Boolean)
    fun writeString(value: String)
    fun writeByteArray(value: ByteArray)
    fun writeChar(value: Char)
    fun writeIntArray(value: IntArray)
    fun writeLongArray(value: LongArray)
    fun toByteArray(): ByteArray
}