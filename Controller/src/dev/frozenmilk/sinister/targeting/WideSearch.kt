package dev.frozenmilk.sinister.targeting

open class WideSearch : FullSearch() {
	init {
		exclude("com.google")
		exclude("com.qualcomm.robotcore.wifi")
		exclude("com.sun")
		exclude("gnu.kawa.swingviews")
		exclude("io.netty")
		exclude("kawa")
		exclude("org.apache")
		exclude("org.checkerframework")
		exclude("org.firstinspires.ftc.robotcore.internal.android")
		exclude("org.java_websocket")
		exclude("org.slf4j")
		exclude("org.tensorflow")
		exclude("org.threeten")
		exclude("com.journeyapps")
	}
}