package dev.frozenmilk.sinister.util.notify

fun interface NotifierInterface {
	/**
	 * displays a notification message on the driver station
	 */
	fun notify(message: String)
}