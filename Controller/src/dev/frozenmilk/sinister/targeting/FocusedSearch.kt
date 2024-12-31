package dev.frozenmilk.sinister.targeting

open class FocusedSearch : NarrowSearch() {
	init {
		exclude("dev.frozenmilk")
	}
}