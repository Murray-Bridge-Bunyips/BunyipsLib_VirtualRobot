package dev.frozenmilk.sinister.loading

import dev.frozenmilk.sinister.Sinister

@Pinned
fun interface LoadEventHandlerInterface {
	/**
	 * polled when an operation is staged on the implementation of [Sinister]
	 *
	 * this is the only way that [LoadEvent]s are published
	 */
	fun handleEventStaging(loadEvent: LoadEvent<*>)
}