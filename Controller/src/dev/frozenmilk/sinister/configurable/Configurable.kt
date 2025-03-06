package dev.frozenmilk.sinister.configurable

import dev.frozenmilk.sinister.loading.Preload

@Preload
interface Configurable {
	/**
	 * configures this
	 */
	fun configure() {
		ConfigurableScanner.configure(this)
	}

	/**
	 * if this is allowed to be left un-configured if no configurations are available
	 *
	 * if false, then [configure] will throw an exception if no configuration can be found
	 */
	val allowNone
		get() = true
}
