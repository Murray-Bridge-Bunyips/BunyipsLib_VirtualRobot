@file:Suppress("DEPRECATION")
package dev.frozenmilk.sinister

import android.content.Context
import android.view.Menu
import au.edu.sa.mbhs.studentrobotics.bunyipslib.Dbg
import com.qualcomm.ftccommon.FtcEventLoop
import com.qualcomm.robotcore.eventloop.opmode.AnnotatedOpModeManager
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.util.ThreadPool
import dev.frozenmilk.sinister.Sinister.Companion.TAG
import dev.frozenmilk.sinister.loaders.RootClassLoader
import dev.frozenmilk.sinister.loading.LoadEvent
import dev.frozenmilk.sinister.loading.LoadEventHandler
import dev.frozenmilk.sinister.loading.Pinned
import dev.frozenmilk.sinister.loading.Preload
import dev.frozenmilk.sinister.sdk.apphooks.*
import dev.frozenmilk.sinister.targeting.FullSearch
import dev.frozenmilk.sinister.targeting.SearchTarget
import dev.frozenmilk.util.graph.emitGraph
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import virtual_robot.OpModeNotificationsFilter
import virtual_robot.controller.VirtualRobotController
import java.io.File
import java.lang.reflect.UndeclaredThrowableException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

// shimmed for virtual robot
@Suppress("unused")
object SinisterImpl : Sinister {
	private val rootSearch = FullSearch()
	val rootLoader = RootClassLoader(this.javaClass.classLoader!!)
	private lateinit var scanners: Set<Scanner>
	@Volatile
	private var run = false
	private val postBoot = CompletableFuture<Unit>()
	private lateinit var packageCodePath: String

	@JvmStatic
	fun doSinisterThings() { // executed on the JavaFX main method
		println("Initializing BunyipsLib virtual robot")
		val dummyContext = Context()
		if (run) {
			OnCreateScanner.CALLSITE.onCreate(dummyContext)
			return
		}
		selfBoot()
		run = true
		OnCreateScanner.CALLSITE.onCreate(dummyContext)
		fireWhatTheSdkWouldNormallyDoDearLordItsAllShimsAndNullHelpMeItsSoCursedButAtLeastItCompilesBesidesTheseHooksAreUselessHere()
	}

	private fun fireWhatTheSdkWouldNormallyDoDearLordItsAllShimsAndNullHelpMeItsSoCursedButAtLeastItCompilesBesidesTheseHooksAreUselessHere() {
		// meh
		OnCreateEventLoopScanner.CALLSITE.onCreateEventLoop(Context(), FtcEventLoop())
//		OnCreateMenuScanner.CALLSITE.onCreateMenu(Context(), null)
//		OnDestroyScanner.CALLSITE.onDestroy(Context())
//		WebHandlerRegistrarScanner.CALLSITE.webHandlerRegistrar(Context(), null)
		
//		SinisterOpModeRegistrar
		
	}
	

	private fun allClassNames(): List<String> {
		val reflections = Reflections("", SubTypesScanner(false))
		return reflections.allTypes.toMutableList() // bit hacky but works, since we dont have a dexfile - using the same
		// approach as virtual robot does for finding @TeleOp and @Autonomous
	}

	private fun selfBoot() {
		val allClasses = allClassNames().mapNotNull {
			if (!rootSearch.determineInclusion(it)) return@mapNotNull null
			try {
				Class.forName(it, false, javaClass.classLoader).also { cls ->
					cls.name
					if (cls.isPinned()) rootLoader.pin(it, cls)
				}
			}
			catch (e: Throwable) {
				rootSearch.exclude(it)
				null
			}
		}.toList()
		
		// note: we dont ignore any class since we physically cant fastload

		val preloaded = preload(rootLoader, allClasses)

		scanners = preloaded
			.flatMap { it.staticInstancesOf(Scanner::class.java) }
			.toSet()

		// run scanners on root
		try {
			scanLoad(rootLoader, allClasses)
		}
		catch (e: Throwable) {
			throw e
		}

	}

	private fun preload(loader: ClassLoader, classes: List<Class<*>>) =
		classes
			.filter {
				try {
					if (it.inheritsAnnotation(Preload::class.java)) {
						it.preload(loader)
						true
					}
					else false
				}
				catch (e: Throwable) {
					false
				}
			}

	private fun scanLoad(loader: ClassLoader, classes: List<Class<*>>) {
		val executor = ThreadPool.getDefault()

		val graph = scanners
			.emitGraph { it.loadAdjacencyRule }

		val tempRunRound = ArrayList<Scanner>(graph.size)
		val visited = LinkedHashSet<Scanner>(graph.size)

		val nodes = graph.nodes.toMutableSet()
		while (nodes.isNotEmpty()) {
			val iter = nodes.iterator()
			val size = nodes.size
			while (iter.hasNext()) {
				val node = iter.next()
				if (visited.containsAll(graph.map[node]!!.set)) {
					tempRunRound.add(node)
					iter.remove()
				}
			}
			check(nodes.size != size) { "Cycle detected in DAG, all remaining elements are in cycle(s). These were:\n$nodes" }
			// we'll consume each of the emitted scanners
			visited.addAll(tempRunRound)
			tempRunRound
				.also {
					val tasks = it.map { scanner ->
						spawnScannerLoad(scanner, loader, classes.iterator(), executor)
					}.toTypedArray()
					try {
						CompletableFuture.allOf(*tasks).join()
					}
					catch (e: Throwable) {
						throw e
					}
				}
				.clear()
		}
	}

	private fun scanUnload(loader: ClassLoader, classes: List<Class<*>>) {
		val executor = ThreadPool.getDefault()

		val graph = scanners
			.emitGraph { it.unloadAdjacencyRule }

		val tempRunRound = ArrayList<Scanner>(graph.size)
		val visited = LinkedHashSet<Scanner>(graph.size)

		val nodes = graph.nodes.toMutableSet()
		while (nodes.isNotEmpty()) {
			val iter = nodes.iterator()
			val size = nodes.size
			while (iter.hasNext()) {
				val node = iter.next()
				if (visited.containsAll(graph.map[node]!!.set)) {
					visited.add(node)
					tempRunRound.add(node)
					iter.remove()
				}
			}
			check(nodes.size != size) { "Cycle detected in DAG, all remaining elements are in cycle(s). These were:\n$nodes" }
			// we'll consume each of the emitted scanners
			tempRunRound
				.also {
					val tasks = it.map { scanner ->
						spawnScannerUnload(scanner, loader, classes.iterator(), executor)
					}.toTypedArray()
					try {
						CompletableFuture.allOf(*tasks).get()
					}
					catch (e: Throwable) {
						throw e
					}
				}
				// clear
				.clear()
		}
	}

	private fun Class<*>.isPinned(): Boolean = isAnnotationPresent(Pinned::class.java) || rootLoader.pinned(name) != null || interfaces.any { it?.isPinned() == true } || superclass?.isPinned() == true

	override fun <LOADER: ClassLoader> stageLoad(loader: LOADER, classNames: List<String>, apply: Consumer<LoadEvent<LOADER>>) {
		val event = LoadEvent(loader)
			.afterRelease {
				synchronized(this) {
					load(loader, classNames)
				}
			}
			.afterUnload {
				synchronized(this) {
					unload(loader, classNames)
				}
			}
		apply.accept(event)
		if (!run) postBoot.thenRun { LoadEventHandler.handleEventStaging(event) }
		else LoadEventHandler.handleEventStaging(event)
	}

	private fun load(loader: ClassLoader, classNames: List<String>) {
		val classes = classNames.mapNotNull {
			if (!rootSearch.determineInclusion(it)) return@mapNotNull null
			try {
				Class.forName(it, false, loader).also { cls ->
					if (cls.isPinned()) return@mapNotNull null
				}
			}
			catch (e: Throwable) {
				rootSearch.exclude(it)
				null
			}
		}

		// ensure that we enforce the preloading
		repeat(preload(loader, classes).count()) { }

		// run scanners
		scanLoad(loader, classes)
	}

	private fun unload(loader: ClassLoader, classNames: List<String>) {
		val classes = classNames.mapNotNull {
			if (!rootSearch.determineInclusion(it)) return@mapNotNull null
			try {
				Class.forName(it, false, loader).also { cls ->
					if (cls.isPinned()) return@mapNotNull null
				}
			}
			catch (e: Throwable) {
				rootSearch.exclude(it)
				null
			}
		}

		// run scanners
		scanUnload(loader, classes)
	}
}

private fun spawnScannerLoad(scanner: Scanner, loader: ClassLoader, classes: Iterator<Class<*>>, executor: ExecutorService): CompletableFuture<*> {
	return CompletableFuture.runAsync({
		// pre scan hook
		scanner.beforeScan(loader)
		// scanning classes
		classes.forEach { cls ->
			if (scanner.targets.determineInclusion(cls.name)) {
				try {
					scanner.scan(loader, cls)
				}
				catch (err: Throwable) {
					throw err
				}
			}
		}
		// post scan hook
		scanner.afterScan(loader)
	}, executor)
}

private fun spawnScannerUnload(scanner: Scanner, loader: ClassLoader, classes: Iterator<Class<*>>, executor: ExecutorService): CompletableFuture<*> {
	return CompletableFuture.runAsync({
		// pre unload hook
		scanner.beforeUnload(loader)
		// unloading classes
		classes.forEach { cls ->
			if (scanner.targets.determineInclusion(cls.name)) {
				try {
					scanner.unload(loader, cls)
				}
				catch (err: Throwable) {
					throw err
				}
			}
		}
		// post unload hook
		scanner.afterUnload(loader)
	}, executor)
}