package dev.frozenmilk.sinister

import dev.frozenmilk.sinister.targeting.SearchTarget

@Preload
interface SinisterFilter {
	val lock: Any
		get() = this
	val targets: SearchTarget
	fun init() {}
	fun filter(clazz: Class<*>)
}