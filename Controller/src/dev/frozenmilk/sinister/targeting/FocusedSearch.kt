package dev.frozenmilk.sinister.targeting

/**
 * a [NarrowSearch] that also excludes Dairy libraries
 */
open class FocusedSearch : NarrowSearch() {
	init {
		exclude("dev.frozenmilk")
	}
}