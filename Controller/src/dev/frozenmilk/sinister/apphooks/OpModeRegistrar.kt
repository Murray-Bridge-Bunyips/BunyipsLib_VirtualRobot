package dev.frozenmilk.sinister.apphooks

import com.qualcomm.robotcore.eventloop.opmode.AnnotatedOpModeManager
import dev.frozenmilk.sinister.Preload

@Preload
@FunctionalInterface
fun interface OpModeRegistrar {
	fun registerOpModes(opModeManager: AnnotatedOpModeManager?)
}

@Suppress("unused")
object OpModeRegistrarFilter : HookFilter<OpModeRegistrar>(OpModeRegistrar::class.java) {
	@JvmStatic
	fun registerOpModes(opModeManager: AnnotatedOpModeManager?) {
		allHooks.forEach { it.registerOpModes(opModeManager) }
	}
}