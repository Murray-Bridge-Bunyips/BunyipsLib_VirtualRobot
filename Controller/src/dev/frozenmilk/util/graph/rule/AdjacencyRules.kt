@file:Suppress("unused")
@file:JvmName("AdjacencyRules")
package dev.frozenmilk.util.graph.rule

import dev.frozenmilk.util.graph.Graph
import java.lang.Class
import java.util.function.Supplier

/**
 * no-op
 */
fun <NODE: Any, GRAPH: Graph<NODE>> independent() = AdjacencyRule<NODE, GRAPH> {}

/**
 * [this] depends on [dependency]
 *
 * will crash if [dependency] doesn't exist in the graph
 */
fun <NODE: Any, GRAPH: Graph<NODE>> NODE.dependsOn(dependency: NODE) = AdjacencyRule<NODE, GRAPH> { graph ->
	checkNotNull(graph[dependency]) { "$dependency was not in graph" }
	requireNotNull(graph[this]) { "$this was not in graph" }.plus(dependency)
}

/**
 * [this] depends on [NODE] returned by [dependencySupplier]
 *
 * will crash if dependency doesn't exist in the graph
 */
fun <NODE: Any, GRAPH: Graph<NODE>> NODE.dependsOn(dependencySupplier: Supplier<NODE>) = AdjacencyRule<NODE, GRAPH> { graph ->
	val dependency = dependencySupplier.get()
	checkNotNull(graph[dependency]) { "$dependency was not in graph" }
	requireNotNull(graph[this]) { "$this was not in graph" }.plus(dependency)
}

/**
 * [dependant] depends on [this]
 *
 * will crash if [dependant] doesn't exist in the graph
 */
fun <NODE: Any, GRAPH: Graph<NODE>> NODE.dependedOn(dependant: NODE) = AdjacencyRule<NODE, GRAPH> { graph ->
	checkNotNull(graph[this]) { "$this was not in graph" }
	requireNotNull(graph[dependant]) { "$dependant was not in graph" }.plus(this)
}

/**
 * [this] depends on [dependency]
 */
fun <NODE: Any, GRAPH: Graph<NODE>> NODE.optionalDependsOn(dependency: NODE) = AdjacencyRule<NODE, GRAPH> { graph ->
	requireNotNull(graph[this]) { "$this was not in graph" }.let {
		if (graph[dependency] == null) return@AdjacencyRule
		it + dependency
	}
}

/**
 * [dependant] depends on [this]
 */
fun <NODE: Any, GRAPH: Graph<NODE>> NODE.optionalDependedOn(dependant: NODE) = AdjacencyRule<NODE, GRAPH> { graph ->
	checkNotNull(graph[this]) { "$this was not in graph" }
	graph[dependant]?.plus(this)
}

/**
 * [this] depends on all [NODE]s of type [dependencyClass]
 *
 * will crash if no [NODE]s of type [dependencyClass] exist in the graph
 */
fun <NODE: Any, GRAPH: Graph<NODE>> NODE.dependsOnClass(dependencyClass: Class<NODE>) = AdjacencyRule<NODE, GRAPH> { graph ->
	val instances = graph.nodes.filterIsInstance(dependencyClass)
	check(instances.isNotEmpty()) { "$dependencyClass was not in graph" }
	requireNotNull(graph[this]) { "$this was not in graph" }.plus(instances)
}

/**
 * all [NODE]s of type [dependantClass] depends on [this]
 *
 * will crash if no [NODE]s of type [dependantClass] exist in the graph
 */
fun <NODE: Any, GRAPH: Graph<NODE>> NODE.dependedOnClass(dependantClass: Class<NODE>) = AdjacencyRule<NODE, GRAPH> { graph ->
	checkNotNull(graph[this]) { "$this was not in graph" }
	val instances = graph.nodes.filterIsInstance(dependantClass)
	require(instances.isNotEmpty()) { "$dependantClass was not in graph" }
	instances.forEach { graph[it]!! + this }
}

/**
 * [this] depends on all [NODE]s of type [dependencyClass]
 */
fun <NODE: Any, GRAPH: Graph<NODE>> NODE.optionalDependsOnClass(dependencyClass: Class<NODE>) = AdjacencyRule<NODE, GRAPH> { graph ->
	requireNotNull(graph[this]) { "$this was not in graph" } +
			graph.nodes.filterIsInstance(dependencyClass)
}

/**
 * all [NODE]s of type [dependantClass] depends on [this]
 */
fun <NODE: Any, GRAPH: Graph<NODE>> NODE.optionalDependedOnClass(dependantClass: Class<NODE>) = AdjacencyRule<NODE, GRAPH> { graph ->
	checkNotNull(graph[this]) { "$this was not in graph" }
	graph.nodes.filterIsInstance(dependantClass).forEach { graph[it]?.plus(this) }
}
