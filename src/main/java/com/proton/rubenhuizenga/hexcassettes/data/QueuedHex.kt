package com.proton.rubenhuizenga.hexcassettes.data

import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.utils.putCompound
import at.petrak.hexcasting.api.utils.serializeToNBT
import com.proton.rubenhuizenga.hexcassettes.CassetteCastEnv
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand

data class QueuedHex(val hex: CompoundTag, var delay: Int) {
	fun serialize(): CompoundTag {
		val compound = CompoundTag()
		compound.putCompound("hex", hex)
		compound.putInt("delay", delay)
		return compound
	}

	fun cast(player: ServerPlayer, pattern: HexPattern) {
		val hand = if (!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty && player.getItemInHand(InteractionHand.OFF_HAND).isEmpty) InteractionHand.OFF_HAND else InteractionHand.MAIN_HAND
		val harness = CastingVM.empty(CassetteCastEnv(player, hand, pattern))
		val hexIota = IotaType.deserialize(hex, player.serverLevel() )
		if (hexIota is ListIota)
			harness.queueExecuteAndWrapIotas(hexIota.list.toList(), player.serverLevel())
	}

	companion object {
		fun deserialize(compound: CompoundTag) =
			QueuedHex(compound.getCompound("hex"), compound.getInt("delay"))
	}
}