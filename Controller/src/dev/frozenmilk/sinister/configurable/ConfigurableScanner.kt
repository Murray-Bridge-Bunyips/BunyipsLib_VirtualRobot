package dev.frozenmilk.sinister.configurable

import dev.frozenmilk.sinister.Scanner
import dev.frozenmilk.sinister.staticInstancesOf
import dev.frozenmilk.sinister.targeting.EmptySearch
import dev.frozenmilk.sinister.targeting.WideSearch
import dev.frozenmilk.sinister.util.log.Logger
import dev.frozenmilk.sinister.util.warn.Warn
import dev.frozenmilk.sinister.util.warn.WarnSource
import dev.frozenmilk.util.graph.GraphImpl
import dev.frozenmilk.util.graph.emitGraph

internal object ConfigurableScanner : Scanner {
	override val loadAdjacencyRule = Scanner.INDEPENDENT
	override val unloadAdjacencyRule = Scanner.INDEPENDENT
	override val targets = WideSearch()
	private val dairySearch = EmptySearch().include("dev.frozenmilk")
	private val teamCodeSearch = EmptySearch().include("org.firstinspires.ftc.teamcode")

	private val configurations = mutableSetOf<Configuration<*>>()
	private val configurationCache = mutableMapOf<Class<out Configurable>, Configuration<*>?>()
	private val configurables = mutableSetOf<Configurable>()

	override fun scan(loader: ClassLoader, cls: Class<*>) {
		configurables.addAll(cls.staticInstancesOf(Configurable::class.java))
		configurations.addAll(cls.staticInstancesOf(Configuration::class.java))
	}

	private fun Any.level() =
		if (teamCodeSearch.determineInclusion(javaClass.name)) Level.TEAM_CODE
		else if (dairySearch.determineInclusion(javaClass.name)) Level.DAIRY
		else Level.LIBRARY

	private val graph = GraphImpl<Configuration<*>>()

	override fun afterScan(loader: ClassLoader) {
		configurations.emitGraph(graph) { configuration: Configuration<*> -> configuration.adjacencyRule }

		Logger.configure()
		Warn.configure()
		Logger.v(javaClass.simpleName, "Configured core logging and warn utilities")
		Logger.v(javaClass.simpleName, "Log: ${Logger.DELEGATE}")
		Logger.v(javaClass.simpleName, "Warn: ${Warn.DELEGATE}")

		configurables.forEach {
			Logger.v(javaClass.simpleName, "configuring $it")
			it.configure()
		}
	}

	override fun unload(loader: ClassLoader, cls: Class<*>) {
		configurables.removeAll(cls.staticInstancesOf(Configurable::class.java))
		configurations.removeAll(cls.staticInstancesOf(Configuration::class.java))
	}

	override fun afterUnload(loader: ClassLoader) {
		afterScan(loader)
	}

	enum class Level {
		DAIRY,
		LIBRARY,
		TEAM_CODE
	}

	private val warns = mutableMapOf<Configurable, WarnSource>()

	// single round of 'kahn' (pull out nodes that have no dependencies) sort (we don't care about the remaining ones)
	// res ->
	// 		one item: good!
	//		more than one ->
	//			try resolve with auto levels ->
	//				success: good!
	//				failure: error (no more merge and warn)
	// 		no items ->
	//			if no inputs, configurations should specify if they must be configured or not, might be fine or not
	//			if any inputs, cycle, throw error (we can't have one input)

	// this way we don't need to worry about cycles from ignored configurations, as long as we never hit them
	@Suppress("UNCHECKED_CAST")
	fun <CONFIGURABLE: Configurable> configure(configurable: CONFIGURABLE) {
		// drop warn if one existed
		synchronized(warns) {
			warns.remove(configurable)?.let {
				Warn.dropSource(it)
			}
		}

		val cls = configurable.javaClass

		val configuration = configurationCache.computeIfAbsent(cls) {
			this.graph.run {
				var nodes = mutableSetOf<Configuration<CONFIGURABLE>>()
				this.nodes.forEach {
					@Suppress("unchecked_cast")
					if (it.configurableClass.isAssignableFrom(cls)) nodes.add(it as Configuration<CONFIGURABLE>)
				}

				val configurationsAvailable = nodes.isNotEmpty()

				if (configurationsAvailable) {
					// removes all nodes that have a dependency in these nodes
					nodes = nodes.filterTo(mutableSetOf()) { this[it]!!.set.none { dependency -> dependency in nodes } }

					if (nodes.isEmpty()) {
						val msg = "cycle(s) detected during configuration collection for configuration of $configurable:\n$nodes"
						Logger.e("Configuration", msg)
						Warn.addSource(ConfigurationWarnSource(configurable, msg))
						return@run null
					}
				}

				when (nodes.size) {
					1 -> nodes.first()
					0 -> null
					else -> {
						val levels = nodes.groupBy { it.level() }
						val pool = if (levels[Level.TEAM_CODE]?.isNotEmpty() == true) levels[Level.TEAM_CODE]!!
						else if (levels[Level.LIBRARY]?.isNotEmpty() == true) levels[Level.LIBRARY]!!
						else levels[Level.DAIRY]!!

						if (pool.size == 1) pool.first()
						else {
							val msg = "unable to determine a single configuration to apply to $configurable, remaining options were $pool"
							Logger.e("Configuration", msg)
							Warn.addSource(ConfigurationWarnSource(configurable, msg))
							error(msg)
						}
					}
				}
			}
		} as? Configuration<CONFIGURABLE>

		if (!configurable.allowNone && configuration == null) {
			val msg = "unable to determine any configurations to apply to $configurable, which requires a configuration"
			Logger.e("Configuration", msg)
			Warn.addSource(ConfigurationWarnSource(configurable, msg))
			error(msg)
		}

		configuration?.let {
			try {
				it.configure(configurable)
			}
			catch (e: Throwable) {
				val msg = "Error was thrown while applying $it to $configurable:"
				Logger.e("Configuration", msg, e)
				Warn.addSource(ConfigurationWarnSource(configurable, msg))
			}
		}
	}

	private class ConfigurationWarnSource(private val configurable: Configurable, override val warning: String) :
		WarnSource {
		override fun onWarn() {
			synchronized(warns) {
				warns[configurable] = this
			}
		}
	}
}