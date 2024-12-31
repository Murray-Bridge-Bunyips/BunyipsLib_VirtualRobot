package dev.frozenmilk.sinister.targeting

open class NarrowSearch : WideSearch() {
	init {
		exclude("org.firstinspires")
		include("org.firstinspires.ftc.teamcode")
		exclude("com.qualcomm")
	}
}