package dev.frozenmilk.sinister.targeting

/**
 * Searches ALL classes (other than those that cannot be found in the compiled app), this is not recommend, and usually a [WideSearch] should be used
 */
open class FullSearch : SearchTarget(Inclusion.INCLUDE) {
	init {
		exclude("java")
		exclude("jdk")
		exclude("kotlin")
		exclude("android")
		exclude("androidx")
		exclude("com.android")
		exclude("com.journeyapps.barcodescanner.ScanContract")
		exclude("com.qualcomm.robotcore.wifi.WifiDirectAssistantAndroid10Extensions\$1")
		exclude("com.sun.tools.javac.model.AnnotationProxyMaker\$MirroredTypeExceptionProxy")
		exclude("com.sun.tools.javac.model.AnnotationProxyMaker\$MirroredTypesExceptionProxy")
		exclude("com.sun.tools.javac.model.AnnotationProxyMaker\$ValueVisitor\$1AnnotationTypeMismatchExceptionProxy")
		exclude("com.sun.tools.javac.nio.JavacPathFileManager\$1")
		exclude("org.openftc.easyopencv.OpenCvVuforiaPassthroughImpl")
		exclude("org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer\$Parameters")
	}
}