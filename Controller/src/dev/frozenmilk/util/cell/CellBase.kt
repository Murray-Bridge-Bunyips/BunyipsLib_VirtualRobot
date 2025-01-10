package dev.frozenmilk.util.cell

import kotlin.math.max

sealed class CellBase<T> : Cell<T> {
	protected var lastGet = System.nanoTime()
	/**
	 * in seconds
	 */
	val timeSincelastGet : Double
		get() = (System.nanoTime() - lastGet) / 1E9

	protected var lastSet = System.nanoTime()
	/**
	 * in seconds
	 */
	val timeSincelastSet : Double
		get() = (System.nanoTime() - lastSet) / 1E9

	/**
	 * in seconds
	 */
	val timeSinceLastAccess: Double
		get() = max(timeSincelastGet, timeSincelastSet)

	abstract override fun toString(): String
}