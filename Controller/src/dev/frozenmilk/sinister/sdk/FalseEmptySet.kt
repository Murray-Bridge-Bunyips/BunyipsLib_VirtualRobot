package dev.frozenmilk.sinister.sdk

import java.util.*

internal class FalseEmptySet<E> : MutableSet<E> by Collections.emptySet() {
	override fun add(element: E) = false
	override fun remove(element: E) = false
}