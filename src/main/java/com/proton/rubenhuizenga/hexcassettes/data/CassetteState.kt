package com.proton.rubenhuizenga.hexcassettes.data

import at.petrak.hexcasting.api.casting.math.HexPattern
import io.netty.buffer.Unpooled
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.network.PacketDistributor
import com.proton.rubenhuizenga.hexcassettes.HexcassettesMain
import com.proton.rubenhuizenga.hexcassettes.data.QueuedHex
import com.proton.rubenhuizenga.hexcassettes.inits.HexcassettesNetworking

class CassetteState {
    var ownedSlots = 0
    val queuedHexes: MutableMap<HexPattern, QueuedHex> = mutableMapOf()
    private var previouslyActiveSlots: Set<String> = emptySet()

    fun sync(player: ServerPlayer) {
        val buf = FriendlyByteBuf(Unpooled.buffer())
        buf.writeInt(ownedSlots)
        val keys = queuedHexes.keys
        buf.writeInt(keys.size)
        keys.forEach { buf.writeUtf(HexcassettesMain.serializeKey(it)) }
        HexcassettesNetworking.CHANNEL.send(PacketDistributor.PLAYER.with { player }, buf)
    }

    fun tick(player: ServerPlayer) {
        queuedHexes.forEach { (_, queuedHex) -> queuedHex.delay -= 1 }

        val iterator = queuedHexes.iterator()
        while (iterator.hasNext()) {
            val (pattern, queuedHex) = iterator.next()
            if (queuedHex.delay <= 0) {
                iterator.remove()
                queuedHex.cast(player, pattern)
            }
        }

        val activeIndices = queuedHexes.keys.map { HexcassettesMain.serializeKey(it) }.toSet()
        if (previouslyActiveSlots != activeIndices) {
            sync(player)
        }
        previouslyActiveSlots = activeIndices
    }

    fun serialize(): CompoundTag {
        val compound = CompoundTag()
        compound.putInt("owned", ownedSlots)
        val hexes = CompoundTag()
        queuedHexes.forEach { (pattern, queuedHex) -> 
            hexes.put(HexcassettesMain.serializeKey(pattern), queuedHex.serialize()) 
        }
        compound.put("hexes", hexes)
        return compound
    }

    companion object {
        @JvmStatic
        fun deserialize(compound: CompoundTag): CassetteState {
            val state = CassetteState()
            state.ownedSlots = compound.getInt("owned")

            val hexes = compound.getCompound("hexes")
            hexes.allKeys.forEach { pattern ->
                val hexCompound = hexes.getCompound(pattern)
                val hex = QueuedHex.deserialize(hexCompound.getCompound("hex"))
                state.queuedHexes[HexcassettesMain.deserializeKey(pattern)] = hex
            }

            return state
        }
    }
}