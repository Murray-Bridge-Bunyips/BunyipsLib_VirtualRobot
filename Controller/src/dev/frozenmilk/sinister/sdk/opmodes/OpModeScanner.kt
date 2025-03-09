package dev.frozenmilk.sinister.sdk.opmodes

import com.qualcomm.robotcore.eventloop.opmode.AnnotatedOpModeManager
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import dev.frozenmilk.sinister.Scanner
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
import virtual_robot.controller.VirtualRobotController

/**
 * this is a utility class for a Scanner that registers opmodes in some fashion
 * (usually detecting annotations on classes, or calling static methods, etc)
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
abstract class OpModeScanner : Scanner {
    @Suppress("leakingThis")
    override val loadAdjacencyRule = afterConfiguration()
    @Suppress("leakingThis")
    override val unloadAdjacencyRule = beforeConfiguration()

    //
    // State
    //

    private val found = mutableMapOf<ClassLoader, MutableList<OpModeMeta>>()
    private var registrationHelper = RegistrationHelper

    //
    // Registration
    //
    
    // shimmy shimmy ya shimmy ya shimmy ya drake
    object RegistrationHelper : AnnotatedOpModeManager {
        override fun register(opModeClass: Class<*>?) {
            VirtualRobotController.extraOpModes[opModeClass] = opModeClass?.simpleName to null
        }

        override fun register(name: String?, opModeClass: Class<out OpMode>?) {
            VirtualRobotController.extraOpModes[opModeClass] = name to null
        }

        override fun register(name: OpModeMeta?, opModeClass: Class<out OpMode>?) {
            VirtualRobotController.extraOpModes[opModeClass] = name?.displayName to name?.group
        }

        override fun register(name: String?, opModeInstance: OpMode?) {
            VirtualRobotController.extraOpModes[opModeInstance?.javaClass] = name to null
        }

        override fun register(name: OpModeMeta?, opModeInstance: OpMode?) {
            VirtualRobotController.extraOpModes[opModeInstance?.javaClass] = name?.displayName to name?.group
        }
    }

//    open class RegistrationHelper(protected val metas: MutableList<OpModeMeta>) {
//        fun register(meta: OpModeMeta, supplier: Supplier<out OpMode>) {
////            SinisterRegisteredOpModes.register(meta, supplier)
//            metas.add(meta)
//        }
//        fun register(meta: OpModeMeta, cls: Class<out OpMode>)  {
//            TeleopAutonomousOpModeScanner.checkOpModeClass(cls)?.let {
//                RobotLog.e(it)
//                RobotLog.setGlobalErrorMsg(it)
//                return
//            }
//            register(meta) { cls.newInstance() }
//        }
//        fun register(meta: OpModeMeta, instance: OpMode) = register(meta) { instance }
//
//        fun unregister(meta: OpModeMeta) {
////            SinisterRegisteredOpModes.unregister(meta)
//        }
//    }

    //
    // Scanner
    //

    final override fun beforeScan(loader: ClassLoader) {
//        registrationHelper = RegistrationHelper(
//            found.getOrPut(loader) { mutableListOf() }
//        )
    }

    final override fun scan(loader: ClassLoader, cls: Class<*>) = scan(loader, cls, registrationHelper)
    final override fun afterScan(loader: ClassLoader) {
    }

    final override fun beforeUnload(loader: ClassLoader) {}
    final override fun unload(loader: ClassLoader, cls: Class<*>) {}
    final override fun afterUnload(loader: ClassLoader) {
        found.remove(loader)?.forEach {  }
    }

    protected abstract fun scan(loader: ClassLoader, cls: Class<*>, registrationHelper: RegistrationHelper)
}