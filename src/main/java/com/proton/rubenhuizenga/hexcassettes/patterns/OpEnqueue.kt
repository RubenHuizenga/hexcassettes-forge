package com.proton.rubenhuizenga.hexcassettes.patterns

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.casting.math.EulerPathFinder
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.casting.mishaps.MishapBadCaster
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import com.proton.rubenhuizenga.hexcassettes.CassetteCastEnv
import com.proton.rubenhuizenga.hexcassettes.HexcassettesMain
import com.proton.rubenhuizenga.hexcassettes.PlayerMinterface
import com.proton.rubenhuizenga.hexcassettes.data.QueuedHex
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.DyeColor

class OpEnqueue : Action {
	override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
		if (env !is PlayerBasedCastEnv)
			throw MishapBadCaster()
		if (env is CassetteCastEnv)
			HexcassettesMain.QUINE.trigger(env.castingEntity as ServerPlayer)
		if (image.stack.size < 2)
			throw MishapNotEnoughArgs(2, image.stack.size)
		val cassetteState = (env.castingEntity as PlayerMinterface).getCassetteState()

		val stack = image.stack.toMutableList()
		val delay = stack.removeAt(stack.lastIndex)
		if (delay !is DoubleIota || delay.double <= 0)
			throw MishapInvalidIota.of(delay, 0, "double.positive")
		val delayValue = delay.double.toInt()

		when (val next = stack.removeAt(stack.lastIndex)) {
			is ListIota -> {
				val index = if (env is CassetteCastEnv) env.pattern else EulerPathFinder.findAltDrawing(HexPattern.fromAngles("qeqwqwqwqwqeqaweqqqqqwweeweweewqdwwewewwewweweww", HexDir.EAST), env.world.gameTime)
				if (cassetteState.queuedHexes.keys.size >= HexcassettesMain.MAX_CASSETTES)
					throw NoFreeCassettes()
				cassetteState.queuedHexes[index] = QueuedHex(IotaType.serialize(next), delayValue)
				stack.add(PatternIota(index))
			}
			is PatternIota -> {
				val key = next.pattern
				if (!cassetteState.queuedHexes.keys.contains(key) && cassetteState.queuedHexes.keys.size >= HexcassettesMain.MAX_CASSETTES)
					throw NotEnoughCassettes()

				val hex = stack.removeAt(stack.lastIndex)
				if (hex !is ListIota)
					throw MishapInvalidIota.ofType(hex, 2, "list")

				cassetteState.queuedHexes[key] = QueuedHex(IotaType.serialize(hex), delayValue)
			}
			else -> throw MishapInvalidIota.of(next, 1, "hex_or_key")
		}

		return OperationResult(image.copy(stack = stack), listOf(), continuation, HexEvalSounds.SPELL)
	}
}

class NoFreeCassettes : Mishap() {
	override fun accentColor(ctx: CastingEnvironment, errorCtx: Context) = dyeColor(DyeColor.RED)
	override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) = error(HexcassettesMain.MOD_ID + ":no_free_cassettes")
	override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {}
}

class NotEnoughCassettes : Mishap() {
	override fun accentColor(ctx: CastingEnvironment, errorCtx: Context) = dyeColor(DyeColor.RED)
	override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) = error(HexcassettesMain.MOD_ID + ":not_enough_cassettes")
	override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
		val caster = env.castingEntity
		if (caster is ServerPlayer)
			(caster as PlayerMinterface).getCassetteState().queuedHexes.clear()
	}
}