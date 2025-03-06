package dev.frozenmilk.sinister.loading

import dev.frozenmilk.sinister.Sinister
import dev.frozenmilk.sinister.configurable.Configurable

@Suppress("unused")
object LoadEventHandler : Configurable {
	/**
	 * defaults eager-release
	 */
	var DELEGATE = LoadEventHandlerInterface { it.release() }
	/**
	 * polled when an operation is staged on the implementation of [Sinister]
	 *
	 * this is the only way that [LoadEvent]s are published
	 */
	@JvmStatic
	fun handleEventStaging(loadEvent: LoadEvent<*>) = DELEGATE.handleEventStaging(loadEvent)
}
