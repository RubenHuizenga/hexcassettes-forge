package com.proton.rubenhuizenga.hexcassettes.client

import at.petrak.hexcasting.api.casting.math.HexPattern

object ClientStorage {
	var ownedCassettes: Int = 0
	var selectedCassette: Int = 0
	val activeCassettes: MutableList<HexPattern> = mutableListOf()
}