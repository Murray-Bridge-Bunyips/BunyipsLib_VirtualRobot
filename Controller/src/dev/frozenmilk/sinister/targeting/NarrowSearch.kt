package dev.frozenmilk.sinister.targeting

/**
 * a [WideSearch] that also excludes the SDK
 */
open class NarrowSearch : WideSearch() {
	init {
		exclude("org.firstinspires")
		// need to re-include team code
		include("org.firstinspires.ftc.teamcode")
		exclude("com.qualcomm")
	}
}