package com.proton.rubenhuizenga.hexcassettes.patterns

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv
import at.petrak.hexcasting.api.casting.getPattern
import at.petrak.hexcasting.api.casting.getPositiveIntUnder
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadCaster
import com.proton.rubenhuizenga.hexcassettes.HexcassettesMain
import com.proton.rubenhuizenga.hexcassettes.PlayerMinterface

class OpForetell : ConstMediaAction {
	override val argc = 1
	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		if (env !is PlayerBasedCastEnv)
			throw MishapBadCaster()
		val queuedHexes = (env.castingEntity as PlayerMinterface).getCassetteState().queuedHexes
		val pattern = args.getPattern(0, argc)
		return queuedHexes[pattern]?.delay?.asActionResult ?: null.asActionResult
	}
}