package com.proton.rubenhuizenga.hexcassettes

import com.proton.rubenhuizenga.hexcassettes.client.CassetteScreen
import com.proton.rubenhuizenga.hexcassettes.inits.HexcassettesNetworking
import net.minecraft.client.KeyMapping
import net.minecraftforge.client.event.RegisterKeyMappingsEvent
import net.minecraftforge.client.settings.KeyConflictContext
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.TickEvent.ClientTickEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import org.lwjgl.glfw.GLFW
import com.mojang.blaze3d.platform.InputConstants

@Mod.EventBusSubscriber(modid = HexcassettesMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
object HexcassettesClient {
	private val openCassettesKeybind = KeyMapping(
		"key.hexcassettes.view_cassettes",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_I,
		"key.categories.hexcassettes"
	)

	@SubscribeEvent
	fun onClientSetup(event: FMLClientSetupEvent) {
		HexcassettesNetworking.clientInit()
	}

	@SubscribeEvent
	fun registerKeybinds(event: RegisterKeyMappingsEvent) {
		event.register(openCassettesKeybind)
	}

	@Mod.EventBusSubscriber(modid = HexcassettesMain.MOD_ID)
	private object ClientEvents {
		@SubscribeEvent
		fun onClientTick(event: TickEvent.ClientTickEvent) {
			if (event.phase == TickEvent.Phase.END) {
				val mc = net.minecraft.client.Minecraft.getInstance()
				if (openCassettesKeybind.consumeClick() && mc.screen !is CassetteScreen) {
					mc.setScreen(CassetteScreen())
				}
			}
		}
	}
}