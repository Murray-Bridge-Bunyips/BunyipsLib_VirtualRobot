package dev.frozenmilk.sinister.util.warn

import dev.frozenmilk.sinister.configurable.Configurable

@Suppress("unused")
object Warn : Configurable {
	var DELEGATE: WarnInterface = WarnImpl()
	/**
	 * registers [warnSource], then calls [WarnSource.onWarn] on it
	 *
	 * if [warnSource] was already registered, this should do nothing
	 */
	@JvmStatic
	fun addSource(warnSource: WarnSource) = DELEGATE.addSource(warnSource)
	/**
	 * notes that the [WarnSource.warning] of [warnSource] has changed
	 *
	 * if [warnSource] was not registered, this should do nothing
	 */
	@JvmStatic
	fun updateSource(warnSource: WarnSource) = DELEGATE.updateSource(warnSource)
	/**
	 * drops [warnSource], then calls [WarnSource.onDrop] on it
	 *
	 * if [warnSource] was already registered, this should do nothing
	 */
	@JvmStatic
	fun dropSource(warnSource: WarnSource) = DELEGATE.dropSource(warnSource)

	/**
	 * current warning
	 */
	@JvmStatic
	val warning: String
		get() = DELEGATE.warning
}