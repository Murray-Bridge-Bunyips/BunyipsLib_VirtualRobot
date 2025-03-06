package dev.frozenmilk.sinister.loading

/**
 * denotes an illegally loaded class, usually bc it should have been pinned but was dynamically loaded
 */
class IllegalLoadException @JvmOverloads constructor(override val message: String? = null) : RuntimeException()