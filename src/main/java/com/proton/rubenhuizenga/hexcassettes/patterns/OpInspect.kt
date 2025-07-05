package com.proton.rubenhuizenga.hexcassettes.patterns

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getPattern
import at.petrak.hexcasting.api.casting.getPlayer
import at.petrak.hexcasting.api.casting.iota.Iota
import com.proton.rubenhuizenga.hexcassettes.PlayerMinterface

class OpInspect : ConstMediaAction {
	override val argc = 2
	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		val player = args.getPlayer(0, argc)
		val pattern = args.getPattern(1, argc)
		return (player as PlayerMinterface).getCassetteState().queuedHexes.containsKey(pattern).asActionResult
	}
}