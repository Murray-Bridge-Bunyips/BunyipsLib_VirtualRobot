package dev.frozenmilk.util.cell

import java.util.function.BiFunction
import java.util.function.Supplier

open class InvalidatingCell<T>(supplier: Supplier<T>, var invalidator: BiFunction<InvalidatingCell<T>, T, Boolean>) : LazyCell<T>(supplier) {
	override fun get(): T {
		val ref = ref
		if (ref != null && invalidator.apply(this, ref)) invalidate()
		return super.get()
	}
}