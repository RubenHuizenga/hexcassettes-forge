package com.proton.rubenhuizenga.hexcassettes.client

import at.petrak.hexcasting.client.render.PatternColors
import at.petrak.hexcasting.client.render.PatternRenderer
import at.petrak.hexcasting.client.render.WorldlyPatternRenderHelpers
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import org.lwjgl.glfw.GLFW
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class CassetteScreen : Screen(Component.translatable("screen.hexical.cassette")) {
	private var lastUpdateTime = System.currentTimeMillis()
	private var interpolatedIndex = 0f

	init {
		if (ClientStorage.ownedCassettes != 0)
			ClientStorage.selectedCassette = Math.floorMod(ClientStorage.selectedCassette, ClientStorage.ownedCassettes)
		interpolatedIndex = ClientStorage.selectedCassette.toFloat()
	}

	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		when (keyCode) {
			GLFW.GLFW_KEY_A -> ClientStorage.selectedCassette -= 1
			GLFW.GLFW_KEY_S -> ClientStorage.selectedCassette += 1
			GLFW.GLFW_KEY_H -> ClientStorage.selectedCassette -= 1
			GLFW.GLFW_KEY_L -> ClientStorage.selectedCassette += 1
			GLFW.GLFW_KEY_LEFT -> ClientStorage.selectedCassette -= 1
			GLFW.GLFW_KEY_RIGHT -> ClientStorage.selectedCassette += 1
		}
		return super.keyPressed(keyCode, scanCode, modifiers)
	}

	override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
		super.render(guiGraphics, mouseX, mouseY, delta)

		guiGraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680)

		if (ClientStorage.ownedCassettes == 0)
			return

		val poseStack = guiGraphics.pose()
		val centerX = this.width / 2f
		val centerY = this.height / 2f

		val currentTime = System.currentTimeMillis()
		val elapsedTime = (currentTime - lastUpdateTime) / 1000.0f
		lastUpdateTime = currentTime

		val diff = circularDiff(ClientStorage.selectedCassette.toFloat(), interpolatedIndex, ClientStorage.ownedCassettes)
		interpolatedIndex += diff * 0.15f * elapsedTime * 60

		val trueIndex = Math.floorMod(ClientStorage.selectedCassette, ClientStorage.ownedCassettes)
		(0 until ClientStorage.ownedCassettes).sortedBy { i -> -abs(i - trueIndex) }.forEach { i ->
			val radians = ((i - interpolatedIndex) / ClientStorage.ownedCassettes) * 2 * PI
			val x = centerX + getRadius() * sin(radians)
			val y = centerY + getRadius() * cos(radians) * SQUASH

			val scale = 1f + 2.5f * (1 + cos(radians)) / 2
			val skew = Mth.clamp(sin(radians) * 0.3f, -0.3f, 0.3f)

			poseStack.pushPose()
			poseStack.translate(x, y + sin(currentTime.toDouble() / 1000f + i * 10f).toFloat() * 5f, 0f)
			poseStack.scale(scale, scale, 1f)
			poseStack.mulPose(com.mojang.math.Axis.ZP.rotation(skew))
			guiGraphics.blit(ResourceLocation("hexcassettes", "textures/cassette.png"), -16, -8, 0, 0f, 0f, 32, 16, 32, 16)
			poseStack.popPose()
		}

		poseStack.pushPose()
		poseStack.translate(centerX, centerY, 0f)
		poseStack.scale(75f, 75f, 75f)
		poseStack.translate(-0.5f, -0.5f, 0f)
		if (trueIndex < ClientStorage.activeCassettes.size) {
			PatternRenderer.renderPattern(ClientStorage.activeCassettes[trueIndex], poseStack, null, WorldlyPatternRenderHelpers.WORLDLY_SETTINGS_WOBBLY, PatternColors.SLATE_WOBBLY_PURPLE_COLOR, 0.0, 10)
		}
		poseStack.popPose()
	}

	private fun getRadius(): Float {
		val horizontalRadius = (width - 40f) / 2f
		val verticalRadius = (height - 40f) / (2f * SQUASH)
		return min(min(horizontalRadius, verticalRadius), BASE_RADIUS.toFloat())
	}

	private fun circularDiff(a: Float, b: Float, size: Int): Float {
		val diff = (a - b) % size
		return when {
			diff < -size / 2f -> diff + size
			diff > size / 2f -> diff - size
			else -> diff
		}
	}

	companion object {
		private const val PI = 3.1415927f
		private const val BASE_RADIUS = 175
		private const val SQUASH = 0.5f
	}
}