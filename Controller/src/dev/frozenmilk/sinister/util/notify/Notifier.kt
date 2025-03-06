package dev.frozenmilk.sinister.util.notify

import dev.frozenmilk.sinister.configurable.Configurable

@Suppress("unused")
object Notifier : Configurable {
	var DELEGATE: NotifierInterface = NotifierInterface {}
	/**
	 * displays a notification message on the driver station
	 */
	@JvmStatic
	fun notify(message: String) = DELEGATE.notify(message)
}
