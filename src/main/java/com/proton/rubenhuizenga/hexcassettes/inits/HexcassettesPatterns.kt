package com.proton.rubenhuizenga.hexcassettes.inits

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.lib.hex.HexActions
import com.proton.rubenhuizenga.hexcassettes.HexcassettesMain
import com.proton.rubenhuizenga.hexcassettes.patterns.*
import com.proton.rubenhuizenga.hexcassettes.HexcassettesClient
import net.minecraft.core.Registry
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.registries.RegisterEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import org.apache.logging.log4j.LogManager

@Mod.EventBusSubscriber(modid = HexcassettesMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
object HexcassettesPatterns {
	private val LOGGER = LogManager.getLogger("Hexcassettes/Actions")

	@SubscribeEvent
	fun registerActions(event: RegisterEvent) {
		try {
			if (event.registryKey == HexActions.REGISTRY.key()) {
				LOGGER.info("Registering Hexcassettes patterns...")
				
				register(event, "enqueue", "qeqwqwqwqwqeqaweqqqqqwweeweweewqdwwewewwewweweww", HexDir.EAST, OpEnqueue())
				register(event, "dequeue", "eqeweweweweqedwqeeeeewwqqwqwqqweawwqwqwwqwwqwqww", HexDir.WEST, OpDequeue())
				register(event, "killall", "eqeweweweweqedwqeeeeewwqqwqwqqw", HexDir.WEST, OpKillAll())
				register(event, "specs", "qeqwqwqwqwqeqaweqqqqq", HexDir.EAST, OpSpecs())
				register(event, "busy", "qeqwqwqwqwqeqaweqqqqqaww", HexDir.EAST, OpBusy())
				register(event, "inspect", "eqeweweweweqedwqeeeee", HexDir.WEST, OpInspect())
				register(event, "foretell", "eqeweweweweqedwqeeeeedww", HexDir.WEST, OpForetell())
				register(event, "self", "qeqwqwqwqwqeqaweqqqqqwweeweweew", HexDir.EAST, OpSelf())
				
				LOGGER.info("Successfully registered all Hexcassettes actions")
			}
		} catch (e: Exception) {
			LOGGER.error("Failed to register Hexcassettes actions", e)
			throw e
		}
	}

	private fun register(
		event: RegisterEvent,
		name: String,
		signature: String,
		startDir: HexDir,
		action: Action
	) {
		val id = HexcassettesMain.id(name)
		LOGGER.debug("Registering action {} with pattern {}", id, signature)
		
		event.register(HexActions.REGISTRY.key(), id) {
			ActionRegistryEntry(HexPattern.fromAngles(signature, startDir), action).also {
				LOGGER.trace("Created ActionRegistryEntry for {}", id)
			}
		}
	}
}