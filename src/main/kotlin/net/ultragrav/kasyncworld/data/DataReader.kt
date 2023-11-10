package net.ultragrav.kasyncworld.data

interface DataReader {
    fun readInt(): Int
    fun readByte(): Byte
    fun readShort(): Short
    fun readLong(): Long
    fun readFloat(): Float
    fun readDouble(): Double
    fun readBoolean(): Boolean
    fun readString(): String
    fun readByteArray(): ByteArray
    fun readChar(): Char
    fun readIntArray(): IntArray
    fun readLongArray(): LongArray
}