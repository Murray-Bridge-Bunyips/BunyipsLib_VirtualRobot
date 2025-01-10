package dev.frozenmilk.util.graph

interface Graph<NODE: Any> {
	val size: Int

	/**
	 * the members of the graph
	 */
	val nodes: Set<NODE>

	/**
	 * returns the [AdjacencySet] for [node] if it exists
	 */
	operator fun get(node: NODE): AdjacencySet<in NODE>?

	interface AdjacencySet<NODE> {
		/**
		 * adds [node] to this
		 */
		operator fun plus(node: NODE): AdjacencySet<NODE>
		/**
		 * adds [nodes] to this
		 */
		operator fun plus(nodes: Collection<NODE>): AdjacencySet<NODE>
	}
}