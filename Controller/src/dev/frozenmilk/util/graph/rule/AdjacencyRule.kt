package dev.frozenmilk.util.graph.rule

import dev.frozenmilk.util.graph.Graph
import java.lang.FunctionalInterface

@FunctionalInterface
fun interface AdjacencyRule<NODE: Any, GRAPH: Graph<NODE>> {
	operator fun invoke(graph: GRAPH)

	infix fun and(and: AdjacencyRule<in NODE, in GRAPH>): AdjacencyRule<NODE, GRAPH> = object : AdjacencyRule<NODE, GRAPH> {
		override fun invoke(graph: GRAPH) {
			this@AdjacencyRule.invoke(graph)
			and.invoke(graph)
		}
	}
}