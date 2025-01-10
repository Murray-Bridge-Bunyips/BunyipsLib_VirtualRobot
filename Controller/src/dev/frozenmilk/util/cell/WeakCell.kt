package dev.frozenmilk.util.cell

import java.lang.ref.WeakReference

private class InternalWeakCell<T>(ref: T) : Cell<T?> {
	private var weakRef = WeakReference(ref)
	override fun get(): T? {
		return weakRef.get()
	}
	override fun accept(p0: T?) {
		this.weakRef = WeakReference(p0);
	}
}

/**
 * a cell that contains a weak reference, which gets automatically dropped by the garbage collector
 */
class WeakCell<T>(ref: T) : LateInitCell<T>(null, "attempted to access the dropped contents of a WeakCell") {
	override var ref: T?
		get() = weakRef.get()
		set(value) {
			weakRef = WeakReference(value)
		}
	private var weakRef = WeakReference(ref)
}