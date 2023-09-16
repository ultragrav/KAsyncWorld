package net.ultragrav.kasyncworld.world.data.block

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.SoundGroup
import org.bukkit.block.*
import org.bukkit.block.data.BlockData
import org.bukkit.block.structure.Mirror
import org.bukkit.block.structure.StructureRotation
import org.bukkit.inventory.ItemStack

class EmptyBlockData(material: Material) : BlockData {

    override fun clone(): BlockData {
        return this
    }

    override fun getMaterial(): Material = material

    override fun getAsString(): String {
        return material.toString()
    }

    override fun getAsString(hideUnspecified: Boolean): String {
        return material.toString()
    }

    override fun merge(data: BlockData): BlockData {
        return this
    }

    override fun matches(data: BlockData?): Boolean {
        return data is EmptyBlockData && data.material == material
    }

    override fun getSoundGroup(): SoundGroup {
        throw UnsupportedOperationException()
    }

    override fun getLightEmission(): Int {
        throw UnsupportedOperationException()
    }

    override fun isOccluding(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun requiresCorrectToolForDrops(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun isPreferredTool(tool: ItemStack): Boolean {
        throw UnsupportedOperationException()
    }

    override fun getPistonMoveReaction(): PistonMoveReaction {
        throw UnsupportedOperationException()
    }

    override fun isSupported(block: Block): Boolean {
        throw UnsupportedOperationException()
    }

    override fun isSupported(location: Location): Boolean {
        throw UnsupportedOperationException()
    }

    override fun isFaceSturdy(face: BlockFace, support: BlockSupport): Boolean {
        throw UnsupportedOperationException()
    }

    override fun getPlacementMaterial(): Material {
        throw UnsupportedOperationException()
    }

    override fun rotate(rotation: StructureRotation) {
        throw UnsupportedOperationException()
    }

    override fun mirror(mirror: Mirror) {
        throw UnsupportedOperationException()
    }

    override fun createBlockState(): BlockState {
        throw UnsupportedOperationException()
    }

    override fun getDestroySpeed(itemStack: ItemStack, considerEnchants: Boolean): Float {
        throw UnsupportedOperationException()
    }

    override fun isRandomlyTicked(): Boolean {
        throw UnsupportedOperationException()
    }
}