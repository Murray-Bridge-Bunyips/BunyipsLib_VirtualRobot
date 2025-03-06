package dev.frozenmilk.sinister.util.warn

interface WarnInterface {
	/**
	 * registers [warnSource], then calls [WarnSource.onWarn] on it
	 *
	 * if [warnSource] was already registered, this should do nothing
	 */
	fun addSource(warnSource: WarnSource)

	/**
	 * notes that the [WarnSource.warning] of [warnSource] has changed
	 *
	 * if [warnSource] was not registered, this should do nothing
	 */
	fun updateSource(warnSource: WarnSource)

	/**
	 * drops [warnSource], then calls [WarnSource.onDrop] on it
	 *
	 * if [warnSource] was already registered, this should do nothing
	 */
	fun dropSource(warnSource: WarnSource)

	/**
	 * current warning
	 */
	val warning: String
}

