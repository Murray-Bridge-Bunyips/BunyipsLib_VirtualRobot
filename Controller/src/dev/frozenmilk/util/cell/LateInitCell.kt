package dev.frozenmilk.util.cell

import dev.frozenmilk.util.observe.Observer
import dev.frozenmilk.util.observe.Observerable
import java.util.function.Function

open class LateInitCell<T> @JvmOverloads constructor(protected open var ref: T? = null, protected val error: String = "Attempted to obtain a null value from an unsafe Cell") : CellBase<T>(), Observerable<T?> {
	override fun get(): T {
		lastGet = System.nanoTime()
		return ref ?: throw IllegalStateException(error)
	}

	/**
	 * returns null instead of throwing an error
	 */
	fun safeGet(): T? {
		lastGet = System.nanoTime()
		return ref
	}

	final override fun accept(p0: T) {
		lastSet = System.nanoTime()
		ref = p0
		observers.forEach {
			it.update(ref)
		}
	}

	/**
	 * causes the next attempt to get the contents of the cell to fail
	 */
	fun invalidate() {
		lastSet = System.nanoTime()
		ref = null
		observers.forEach {
			it.update(null)
		}
	}

	val initialised: Boolean get() = ref != null

	/**
	 * applies the function and returns the result if the internals are already initialised, else return null
	 *
	 * DOES NOT evaluate the contents of the cell, if they are in an [invalidate]d state
	 */
	fun <R> safeInvoke(fn: Function<T, R>): R? = ref?.let(fn::apply)

	private val observers = mutableSetOf<Observer<in T?>>()

	final override fun bind(observer: Observer<in T?>) {
		if (observer == this) throw IllegalArgumentException("Self binding is illegal")
		observers.add(observer)
		observer.update(ref)
	}

	final override fun unbind(observer: Observer<in T?>) = observers.remove(observer)

	final override fun update(new: T?) {
		if (new != ref) { // ensures no cyclic operations
			if (new == null) invalidate()
			else accept(new)
		}
	}

	final override fun toString() = "${javaClass.simpleName}|$ref|"
}