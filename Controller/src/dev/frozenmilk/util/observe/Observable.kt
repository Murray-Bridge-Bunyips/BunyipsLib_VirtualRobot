package dev.frozenmilk.util.observe

interface Observable<T> {
	/**
	 * binds [observer] to this
	 *
	 * then calls [Observer.update]
	 */
	fun bind(observer: Observer<in T>)
	/**
	 * removes bind from [observer] to this
	 *
	 * @return true if there was a bind
	 */
	fun unbind(observer: Observer<in T>): Boolean
}