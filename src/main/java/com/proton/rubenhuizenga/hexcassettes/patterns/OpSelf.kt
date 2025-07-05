package com.proton.rubenhuizenga.hexcassettes.patterns

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import com.proton.rubenhuizenga.hexcassettes.CassetteCastEnv

class OpSelf : ConstMediaAction {
	override val argc = 0
	override fun execute(args: List<Iota>, env: CastingEnvironment) = (env as? CassetteCastEnv)?.pattern?.asActionResult ?: listOf(NullIota())
}