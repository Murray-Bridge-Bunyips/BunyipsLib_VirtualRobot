package dev.frozenmilk.sinister.util.warn

interface WarnSource {
	/**
	 * if the output of this function changes,
	 * [this] must be passed to [Warn.updateSource]
	 */
	val warning: String

	/**
	 * called when this is registered as a source
	 */
	fun onWarn() {}

	/**
	 * called when this is dropped as a source
	 */
	fun onDrop() {}
}