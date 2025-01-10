package dev.frozenmilk.util.graph

import dev.frozenmilk.util.graph.rule.AdjacencyRule
import dev.frozenmilk.util.graph.rule.ContextRule
import java.util.function.Function

fun <CTX: Any, NODE: Any> Set<NODE>.reduceByContext(
	context: CTX,
	contextRuleSelector: Function<NODE, ContextRule<*, CTX>>,
): Set<NODE> {
	val copy = toMutableSet()
	val iter = copy.iterator()
	while (iter.hasNext()) {
		val next = iter.next()
		val contextRule: ContextRule<*, CTX> = contextRuleSelector.apply(next)
		if (!contextRule.invokeAndResolve(context)) {
			iter.remove()
		}
	}

	return copy
}

@JvmOverloads
fun <NODE: Any> Set<NODE>.emitGraph(graphImpl: GraphImpl<NODE> = GraphImpl(), adjacencyRuleSelector: Function<NODE, AdjacencyRule<NODE, in Graph<NODE>>>): GraphImpl<NODE> {
	graphImpl.initForSet(this)
	forEach { adjacencyRuleSelector.apply(it)(graphImpl) }
	return graphImpl
}

fun <NODE: Any> GraphImpl<NODE>.sort(): List<NODE> {
	val stack = ArrayList<NODE>(size)
	val visited = LinkedHashSet<NODE>(size)

	val nodes = this.nodes.toMutableSet()
	while (nodes.isNotEmpty()) {
		val iter = nodes.iterator()
		val size = nodes.size
		while (iter.hasNext()) {
			val node = iter.next()
			if (visited.containsAll(this.map[node]!!.set)) {
				visited.add(node)
				stack.add(node)

				iter.remove()
			}
		}
		check(nodes.size != size) { "Cycle detected in DAG, all remaining elements are in cycle(s). These were:\n$nodes" }
	}

	return stack
}

fun <NODE: Any> GraphImpl<NODE>.sortAndRemoveCycles(): List<NODE> {
	val stack = ArrayList<NODE>(size)
	val visited = LinkedHashSet<NODE>(size)

	val nodes = this.nodes.toMutableSet()
	while (nodes.isNotEmpty()) {
		val iter = nodes.iterator()
		val size = nodes.size
		while (iter.hasNext()) {
			val node = iter.next()
			if (visited.containsAll(this.map[node]!!.set)) {
				visited.add(node)
				stack.add(node)

				iter.remove()
			}
		}
		if (nodes.size == size) break
	}

	return stack
}
