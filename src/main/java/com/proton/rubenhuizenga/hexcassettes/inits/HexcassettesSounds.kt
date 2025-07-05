package com.proton.rubenhuizenga.hexcassettes.inits

import com.proton.rubenhuizenga.hexcassettes.HexcassettesMain
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject
import net.minecraftforge.eventbus.api.IEventBus

object HexcassettesSounds {
    private val SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, HexcassettesMain.MOD_ID)

    val CASSETTE_EJECT: RegistryObject<SoundEvent> = SOUNDS.register("cassette_eject") {
        SoundEvent.createVariableRangeEvent(HexcassettesMain.id("cassette_eject"))
    }
    val CASSETTE_FAIL: RegistryObject<SoundEvent> = SOUNDS.register("cassette_fail") {
        SoundEvent.createVariableRangeEvent(HexcassettesMain.id("cassette_fail"))
    }
    val CASSETTE_INSERT: RegistryObject<SoundEvent> = SOUNDS.register("cassette_insert") {
        SoundEvent.createVariableRangeEvent(HexcassettesMain.id("cassette_insert"))
    }
    val CASSETTE_LOOP: RegistryObject<SoundEvent> = SOUNDS.register("cassette_loop") {
        SoundEvent.createVariableRangeEvent(HexcassettesMain.id("cassette_loop"))
    }

    fun init(bus: IEventBus) {
        SOUNDS.register(bus)
    }
}