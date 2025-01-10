package dev.frozenmilk.util.observe

/**
 * combines [Observer] and [Observable]
 */
interface Observerable<T> : Observable<T>, Observer<T> {
	/**
	 * [bind]s [observerable] to listen to this
	 *
	 * then [bind]s this to listen to [observerable]
	 *
	 * if [bind] is set up correctly, then [observerable] will gain this observable value
	 */
	fun bindBoth(observerable: Observerable<T>) {
		bind(observerable)
		observerable.bind(this)
	}

	/**
	 * [unbind]s [observerable] from this
	 *
	 * then [unbind]s this from [observerable]
	 *
	 * @return true if either binding was removed
	 */
	// note, not short circuiting or
	fun unbindBoth(observerable: Observerable<T>) = unbind(observerable) or observerable.unbind(this)
}