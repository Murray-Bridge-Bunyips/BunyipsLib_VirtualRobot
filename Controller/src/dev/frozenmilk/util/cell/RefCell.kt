package dev.frozenmilk.util.cell

import dev.frozenmilk.util.observe.Observer
import dev.frozenmilk.util.observe.Observerable

open class RefCell<T> (protected var ref: T) : CellBase<T>(), Observerable<T> {
	override fun get(): T {
		lastGet = System.nanoTime()
		return ref
	}
	override fun accept(p0: T) {
		lastSet = System.nanoTime()
		ref = p0
		observers.forEach {
			it.update(ref)
		}
	}

	private val observers = mutableSetOf<Observer<in T>>()

	override fun bind(observer: Observer<in T>) {
		if (observer == this) throw IllegalArgumentException("Self binding is illegal")
		observers.add(observer)
		observer.update(ref)
	}

	override fun unbind(observer: Observer<in T>) = observers.remove(observer)

	override fun update(new: T) {
		if (new != ref) accept(new) // ensures no cyclic operations
	}

	override fun toString() = "${javaClass.simpleName}|$ref|"
}