package dev.frozenmilk.sinister.util.flag

import dev.frozenmilk.sinister.configurable.Configurable

interface Flag : Configurable {
	var flag: Boolean
}