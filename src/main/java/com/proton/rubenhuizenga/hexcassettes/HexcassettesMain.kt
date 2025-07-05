package com.proton.rubenhuizenga.hexcassettes

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import com.google.gson.JsonObject
import com.proton.rubenhuizenga.hexcassettes.inits.*
import net.minecraft.advancements.critereon.*
import net.minecraft.advancements.CriteriaTriggers
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity
import net.minecraft.world.level.Level
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegisterEvent
import org.spongepowered.asm.mixin.Mixins
import org.spongepowered.asm.mixin.MixinEnvironment
import org.spongepowered.asm.launch.MixinBootstrap
import org.apache.logging.log4j.LogManager

@Mod(HexcassettesMain.MOD_ID)
class HexcassettesMain {
	private val ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID)
	private val CASSETTE = ITEMS.register("cassette") { CassetteItem() }
	private val LOGGER = LogManager.getLogger("Hexcassettes")
	private val PORT_AUTHOR = "RubenHuizenga"

	init {
		LOGGER.info("Initializing Hexcellular (Forge Port by $PORT_AUTHOR)")

		MixinBootstrap.init()
        Mixins.addConfiguration("hexcassettes.mixins.json")
        MixinEnvironment.getDefaultEnvironment().side = MixinEnvironment.Side.CLIENT
        
		// Register all DeferredRegisters first
		ITEMS.register(MOD_BUS)
		HexcassettesSounds.init(MOD_BUS) // Assuming this uses DeferredRegister
		
		// Register event listeners
		MOD_BUS.addListener(this::onCreativeTabBuild)
		MinecraftForge.EVENT_BUS.addListener(this::onPlayerClone)
		
		// Other initialization
		HexcassettesNetworking.init()

		LOGGER.info("Loaded Hexcellular (Forge Port by $PORT_AUTHOR)")
	}

	@SubscribeEvent
	fun onCreativeTabBuild(event: BuildCreativeModeTabContentsEvent) {
		LOGGER.info("Checking tab: ${event.tabKey}")
		if (event.tabKey == ResourceKey.create(Registries.CREATIVE_MODE_TAB, HexAPI.modLoc("hexcasting"))) {
			LOGGER.info("Attempting to add cassette to tab")
			val item = CASSETTE.get()
			LOGGER.info("Cassette item: $item")
			event.accept(item)
		}
	}

	@SubscribeEvent
	fun onPlayerClone(event: PlayerEvent.Clone) {
		val oldPlayer = event.original
		val newPlayer = event.entity
		if (oldPlayer is PlayerMinterface && newPlayer is PlayerMinterface) {
			newPlayer.getCassetteState().ownedSlots = oldPlayer.getCassetteState().ownedSlots
		}
	}

	companion object {
		const val MOD_ID: String = "hexcassettes"
		const val MAX_CASSETTES: Int = 6
		fun id(string: String) = ResourceLocation(MOD_ID, string)

		@JvmStatic
		val QUINE: QuineCriterion by lazy { CriteriaTriggers.register(QuineCriterion()) }
		@JvmStatic
		val TAPE_WORM: TapeWormCriterion by lazy { CriteriaTriggers.register(TapeWormCriterion()) }
		@JvmStatic
		val FULL_ARSENAL: FullArsenalCriterion by lazy { CriteriaTriggers.register(FullArsenalCriterion()) }

		fun serializeKey(pattern: HexPattern) = pattern.startDir.toString() + ":" + pattern.anglesSignature()
		fun deserializeKey(string: String): HexPattern {
			val fragments = string.split(":")
			return HexPattern.fromAngles(fragments[1], HexDir.fromString(fragments[0]))
		}
	}
}

class CassetteItem : Item(Properties().stacksTo(1).rarity(Rarity.UNCOMMON).food(
	FoodProperties.Builder().alwaysEat().build()
)) {
	override fun getUseDuration(stack: ItemStack) = 100
	override fun getEatingSound() = HexcassettesSounds.CASSETTE_LOOP.get()

	override fun finishUsingItem(stack: ItemStack, world: Level, user: LivingEntity): ItemStack {
		if (world.isClientSide) {
			world.playLocalSound(user.x, user.y, user.z, HexcassettesSounds.CASSETTE_INSERT.get(), SoundSource.MASTER, 5f, 1f, false)
			return super.finishUsingItem(stack, world, user)
		}

		if (user !is ServerPlayer)
			return super.finishUsingItem(stack, world, user)

		val cassetteData = (user as PlayerMinterface).getCassetteState()
		if (cassetteData.ownedSlots < HexcassettesMain.MAX_CASSETTES) {
			HexcassettesMain.TAPE_WORM.trigger(user)
			cassetteData.ownedSlots += 1
			if (cassetteData.ownedSlots == HexcassettesMain.MAX_CASSETTES)
				HexcassettesMain.FULL_ARSENAL.trigger(user)
			cassetteData.sync(user)
		}
		return super.finishUsingItem(stack, world, user)
	}
}

class QuineCriterion : SimpleCriterionTrigger<QuineCriterion.Condition>() {
	companion object {
		val ID: ResourceLocation = HexcassettesMain.id("quinio")
	}

	override fun getId() = ID

	override fun createInstance(
		json: JsonObject,
		player: ContextAwarePredicate,
		deserializer: DeserializationContext
	) = Condition()

	fun trigger(player: ServerPlayer) = trigger(player) { true }

	class Condition : AbstractCriterionTriggerInstance(
		ID,
		ContextAwarePredicate.ANY
	)
}

class TapeWormCriterion : SimpleCriterionTrigger<TapeWormCriterion.Condition>() {
	companion object {
		val ID: ResourceLocation = HexcassettesMain.id("tape_worm")
	}

	override fun getId() = ID

	override fun createInstance(
		json: JsonObject,
		player: ContextAwarePredicate,
		deserializer: DeserializationContext
	) = Condition()

	fun trigger(player: ServerPlayer) = trigger(player) { true }

	class Condition : AbstractCriterionTriggerInstance(
		ID,
		ContextAwarePredicate.ANY
	)
}

class FullArsenalCriterion : SimpleCriterionTrigger<FullArsenalCriterion.Condition>() {
	companion object {
		val ID: ResourceLocation = HexcassettesMain.id("full_arsenal")
	}

	override fun getId() = ID

	override fun createInstance(
		json: JsonObject,
		player: ContextAwarePredicate,
		deserializer: DeserializationContext
	) = Condition()

	fun trigger(player: ServerPlayer) = trigger(player) { true }

	class Condition : AbstractCriterionTriggerInstance(
		ID,
		ContextAwarePredicate.ANY
	)
}