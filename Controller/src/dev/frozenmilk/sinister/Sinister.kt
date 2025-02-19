package dev.frozenmilk.sinister

import android.content.Context
import com.qualcomm.ftccommon.FtcEventLoop
import com.qualcomm.robotcore.eventloop.opmode.AnnotatedOpModeManager
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.util.RobotLog
import com.qualcomm.robotcore.util.ThreadPool
import dev.frozenmilk.sinister.Sinister.TAG
import dev.frozenmilk.sinister.apphooks.*
import dev.frozenmilk.sinister.targeting.FullSearch
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import virtual_robot.controller.VirtualRobotController
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService

object Sinister {
	private val loader = this::class.java.classLoader
	private val rootSearch = FullSearch()
	private var run = false
	const val TAG = "Sinister"

	@JvmStatic
	fun doSinisterThings() { // executed on the JavaFX main method
		println("Initializing BunyipsLib virtual robot")
		val dummyContext = Context()
		if (run) {
			OnCreateFilter.onCreate(dummyContext)
			return
		}
		selfBoot()
		run = true
		OnCreateFilter.onCreate(dummyContext)
		fireWhatTheSdkWouldNormallyDoDearLordItsAllShimsAndNullHelpMeItsSoCursedButAtLeastItCompilesBesidesTheseHooksAreUselessHere()
	}

	private fun fireWhatTheSdkWouldNormallyDoDearLordItsAllShimsAndNullHelpMeItsSoCursedButAtLeastItCompilesBesidesTheseHooksAreUselessHere() {
		OnCreateEventLoopFilter.onCreateEventLoop(Context(), FtcEventLoop())
		OnCreateMenuFilter.onCreateMenu(Context(), null)
		OnDestroyFilter.onDestroy(Context())
		OpModeRegistrarFilter.registerOpModes(ExtraOpModeRegistrar)
		WebHandlerRegistrarFilter.webHandlerRegistrar(Context(), null)
	}
	
	object ExtraOpModeRegistrar : AnnotatedOpModeManager {
		override fun register(opModeClass: Class<*>?) {
			VirtualRobotController.extraOpModes.add(opModeClass)
		}

		override fun register(name: String?, opModeClass: Class<out OpMode>?) {
			VirtualRobotController.extraOpModes.add(opModeClass)
		}

		override fun register(name: OpModeMeta?, opModeClass: Class<out OpMode>?) {
			VirtualRobotController.extraOpModes.add(opModeClass)
		}

		override fun register(name: String?, opModeInstance: OpMode?) {
			VirtualRobotController.extraOpModes.add(opModeInstance!!.javaClass)
		}

		override fun register(name: OpModeMeta?, opModeInstance: OpMode?) {
			VirtualRobotController.extraOpModes.add(opModeInstance!!.javaClass)
		}
	}

	private fun allClassNames(): List<String> {
		val reflections = Reflections("", SubTypesScanner(false))
		return reflections.allTypes.toMutableList() // bit hacky but works, since we dont have a dexfile - using the same
		                                            // approach as virtual robot does for finding @TeleOp and @Autonomous
	}

	private fun allClasses(): List<Pair<Class<*>, String>> {
		return allClassNames().mapNotNull {
			if (!rootSearch.determineInclusion(it)) return@mapNotNull null

			try {
				return@mapNotNull Class.forName(it, false, loader) to it
			}
			catch (e: Throwable) {
				RobotLog.ee(TAG, "Error occurred while locating class: $e")
			}

			rootSearch.exclude(it)

			null
		}
	}

	private fun selfBoot() {
		val allClasses = allClasses()

		val preloaded = allClasses
				.mapNotNull {
					try {
						it.first.preload()
						return@mapNotNull it
					}
					catch (_: Throwable) {
						null
					}
				}

		val filters = preloaded
				.flatMap {
					it.first.staticInstancesOf(SinisterFilter::class.java)
				}

		filters.forEach {
			it.init()
		}

		val executor = ThreadPool.newFixedThreadPool(15, "sinister hacky classpath scanning")
		val tasks = filters.map {
			spawnFilter(it, allClasses.iterator(), executor)
		}.toTypedArray()
		try {
			tasks.forEach {
				var res = it.get()
				while (res != null) {
					res = res.get() as CompletableFuture<*>?
				}
			}
		}
		catch (e: Throwable) {
			RobotLog.ee(TAG, "Error occurred while running Sinister:\nError: $e\nStackTrace: ${e.stackTraceToString()}")
		}
	}
}

private fun spawnFilter(filter: SinisterFilter, allClasses: Iterator<Pair<Class<*>, String>>, executor: ExecutorService): CompletableFuture<CompletableFuture<*>?> {
	val (cls, name) = allClasses.next()
	return CompletableFuture.runAsync({
		if (filter.targets.determineInclusion(name)) {
			synchronized(filter.lock) {
				filter.filter(cls)
			}
		}
	}, executor)
		.handle { _, err ->
			if (err != null) RobotLog.ee(TAG, "Error occurred while running SinisterFilter: ${filter::class.simpleName} | ${filter}\nFiltering Class:${cls}\nError: $err\nStackTrace: ${err.stackTraceToString()}")
			return@handle if (allClasses.hasNext()) spawnFilter(filter, allClasses, executor)
			else null
		}
}