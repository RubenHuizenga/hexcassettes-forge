package com.proton.rubenhuizenga.hexcassettes

import com.proton.rubenhuizenga.hexcassettes.data.CassetteState

interface PlayerMinterface {
	fun getCassetteState(): CassetteState
}