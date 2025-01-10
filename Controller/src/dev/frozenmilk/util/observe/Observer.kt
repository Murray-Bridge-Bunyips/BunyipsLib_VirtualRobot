package dev.frozenmilk.util.observe

@FunctionalInterface
fun interface Observer<T> {
	/**
	 * called when [new] is published
	 *
	 * [new] should not be equal to the last [new] value
	 */
	fun update(new: T)
}