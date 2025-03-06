package dev.frozenmilk.sinister

import dev.frozenmilk.sinister.loading.LoadEvent
import dev.frozenmilk.sinister.loading.LoadEventHandler
import java.util.function.Consumer

/**
 * implementations of this class must call the event handlers on [LoadEventHandler]
 */
interface Sinister {
	companion object {
		const val TAG = "Sinister"
	}
	/**
	 * stages to load [classNames] via [loader]
	 *
	 * [apply] is a function that the resulting [LoadEvent] is applied to,
	 * before the event is passed to [LoadEventHandler].
	 * This allows for the caller to apply additional callback modifications, and to extract the event to use later for unloading or cancelling.
	 *
	 * Only [LoadEventHandler] should [LoadEvent.release] this event. The caller of this function should not.
	 */
	fun <LOADER: ClassLoader> stageLoad(loader: LOADER, classNames: List<String>, apply: Consumer<LoadEvent<LOADER>>)
}