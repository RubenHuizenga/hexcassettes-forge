package com.proton.rubenhuizenga.hexcassettes.patterns

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadCaster
import com.proton.rubenhuizenga.hexcassettes.PlayerMinterface
import net.minecraft.server.level.ServerPlayer

class OpKillAll : SpellAction {
	override val argc = 0
	override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
		if (env !is PlayerBasedCastEnv)
			throw MishapBadCaster()
		return SpellAction.Result(Spell(env.castingEntity as ServerPlayer), 0, listOf())
	}

	private data class Spell(val caster: ServerPlayer) : RenderedSpell {
		override fun cast(env: CastingEnvironment) {
			(env.castingEntity as PlayerMinterface).getCassetteState().queuedHexes.clear()
		}
	}
}