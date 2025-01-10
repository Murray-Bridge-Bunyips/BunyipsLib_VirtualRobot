package dev.frozenmilk.util.graph

class GraphImpl<NODE: Any> @JvmOverloads constructor(val map: MutableMap<NODE, GraphImpl<NODE>.AdjacencySetImpl> = mutableMapOf()) : Graph<NODE> {
	fun initForSet(set: Set<NODE>) {
		map.keys.removeAll(map.keys - set)
		set.forEach {
			map.getOrPut(it) { AdjacencySetImpl(it, mutableSetOf()) }.set.clear()
		}
	}
	fun initForMap(map: Map<NODE, MutableSet<NODE>>) {
		this.map.keys.removeAll(this.map.keys - map.keys)
		map.forEach { (node, dependencies) -> this.map[node] = AdjacencySetImpl(node, dependencies) }
	}
	constructor(set: Set<NODE>): this(mutableMapOf<NODE, AdjacencySetImpl>()) {
		initForSet(set)
	}

	override val size = map.size

	/**
	 * the members of the graph
	 */
	override val nodes = map.keys as Set<NODE>

	/**
	 * returns the [AdjacencySet] for [node] if it exists
	 */
	override operator fun get(node: NODE) = map[node]

	inner class AdjacencySetImpl(val node: NODE, val set: MutableSet<NODE>): Graph.AdjacencySet<NODE> {
		override operator fun plus(node: NODE) = apply {
			if (node != this.node) set.add(node)
		}
		override operator fun plus(nodes: Collection<NODE>) = apply {
			if (nodes.contains(node)) set.addAll(nodes - node)
			else set.addAll(nodes)
		}
	}
}