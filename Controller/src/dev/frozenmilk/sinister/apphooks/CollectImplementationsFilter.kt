package dev.frozenmilk.sinister.apphooks

import dev.frozenmilk.sinister.SinisterFilter
import dev.frozenmilk.sinister.staticInstancesOf
import dev.frozenmilk.sinister.targeting.NarrowSearch

abstract class CollectImplementationsFilter<T: Any>(private val clazz: Class<T>) : SinisterFilter {
	override val targets = NarrowSearch()
	protected val found = mutableSetOf<T>()
	override fun init() {
		found.clear()
	}
	override fun filter(clazz: Class<*>) {
		clazz.staticInstancesOf(this.clazz)
				.forEach {
					found.add(it)
				}
	}
}