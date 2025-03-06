package dev.frozenmilk.sinister.configurable

import dev.frozenmilk.sinister.loading.Preload
import dev.frozenmilk.util.graph.Graph
import dev.frozenmilk.util.graph.rule.AdjacencyRule
import dev.frozenmilk.util.graph.rule.dependedOn
import dev.frozenmilk.util.graph.rule.independent

@Preload
interface Configuration<CONFIGURABLE: Configurable> {
	/**
	 * used for runtime reflection
	 */
	val configurableClass: Class<CONFIGURABLE>

	/**
	 * configures [configurable]
	 */
	fun configure(configurable: CONFIGURABLE)

	/**
	 * use this to apply adjacencies to the configuration graph
	 *
	 * this allows for specifying overrides by declaring other
	 * [Configuration]s to depend on this using [dependedOn]
	 */
	val adjacencyRule: AdjacencyRule<Configuration<*>, Graph<Configuration<*>>>

	companion object {
		@JvmStatic
		@get:JvmName("INDEPENDENT")
		val INDEPENDENT = independent<Configuration<*>, Graph<Configuration<*>>>()
	}
}