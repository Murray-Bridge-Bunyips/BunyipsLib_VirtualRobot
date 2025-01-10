package dev.frozenmilk.util.cell

import java.util.function.Supplier

/**
 * lazily loads a value when contents are null
 */
open class LazyCell<T> (private val supplier: Supplier<T>) : LateInitCell<T>(error = "Attempted to obtain a null value from a LazyCell") {

	/**
	 * the time, in seconds, since last time the contents of this cell started being lazily evaluated, due to an [invalidate]d state
	 */
	val timeSinceBeforeLastEval: Double
		get() {
			return (System.nanoTime() - timeBeforeLastEval) / 1E9
		}

	/**
	 * the time, in seconds, since last time the contents of this cell finished being lazily evaluated, due to an [invalidate]d state
	 */
	val timeSinceAfterLastEval: Double
		get() {
			return (System.nanoTime() - timeAfterLastEval) / 1E9
		}

	private var timeBeforeLastEval = 0L
	private var timeAfterLastEval = 0L

	/**
	 * evaluates the contents of this cell from [supplier]
	 */
	fun evaluate() {
		timeBeforeLastEval = System.nanoTime()
		accept(supplier.get())
		timeAfterLastEval = System.nanoTime()
	}

	/**
	 * [evaluate] but doesn't run if the cell has contents
	 */
	fun safeEvaluate() {
		if (initialised) return
		evaluate()
	}

	override fun get(): T {
		safeEvaluate()
		return super.get()
	}
}