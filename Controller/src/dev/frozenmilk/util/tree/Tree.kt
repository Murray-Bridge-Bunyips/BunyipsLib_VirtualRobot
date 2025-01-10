package dev.frozenmilk.util.tree

import java.util.function.BiConsumer
import java.util.function.Function

open class Tree<K, V> (open var contents: V) {
	protected val children = mutableMapOf<K, Tree<K, V>>()
	operator fun get(vararg keys: K) = get(keys.toList())
	open operator fun get(key: K): V? = children[key]?.contents
	open operator fun get(keys: Collection<K>): V? {
		val k = keys.firstOrNull() ?: return contents
		return (children[k] ?: return null)[keys.drop(1)]
	}
	open fun getChild(key: K) = children[key]
	open operator fun set(key: K, value: V) {
		children[key]?.contents = value
	}

	/**
	 * returns the node at the end of the key collection
	 *
	 * empty nodes are set lazily via [orElse]
	 */
	open fun getOrElse(keys: Collection<K>, orElse: Function<in K, out V>) : Tree<K, V> {
		var treeWalker = this
		keys.forEach {
			treeWalker.computeIfAbsent(it, orElse)
			treeWalker = treeWalker.children[it]!!
		}
		return treeWalker
	}

	/**
	 * returns the node at the end of the key collection
	 *
	 * empty nodes are set to [default]
	 */
	open fun getOrDefault(keys: Collection<K>, default: V) : Tree<K, V> {
		var treeWalker = this
		keys.forEach {
			treeWalker.putIfAbsent(it, default)
			treeWalker = treeWalker.children[it]!!
		}
		return treeWalker
	}

	open fun remove(key: K): Tree<K, V>? = children.remove(key)
	open fun remove(keys: Collection<K>): Tree<K, V>? {
		return if (keys.isEmpty()) null
		else if (keys.size == 1) remove(keys.first())
		else children[keys.first()]?.remove( keys.drop(1) )
	}
	open fun containsKey(key: K): Boolean = children.containsKey(key)
	open fun containsKey(keys: Collection<K>): Boolean = children[keys.firstOrNull()]?.containsKey(keys.drop(1)) ?: false
	open fun computeIfAbsent(key: K, orElse: Function<in K, out V>) = children.computeIfAbsent(key) { Tree(orElse.apply(key)) }
	open fun putIfAbsent(key: K, value: V) = children.putIfAbsent(key, Tree(value))

	/**
	 *
	 */
	open fun forEachChildren(f: BiConsumer<K, Tree<K, V>>) {
		children.forEach { (key, node) ->
			f.accept(key, node)
			node.forEachChildren(f)
		}
	}

	/**
	 * in place replaces this with [other]
	 */
	open fun clone(other: Tree<K, V>) {
		contents = other.contents
		children.clear()
		children.putAll(other.children)
	}
	open operator fun set(key: K, subtree: Tree<K, V>) {
		children[key] = subtree
	}
}