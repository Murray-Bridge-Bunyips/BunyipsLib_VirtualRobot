package dev.frozenmilk.sinister.targeting

import dev.frozenmilk.util.tree.Tree

/**
 * a class that represents a hierarchical include / exclude search pattern
 *
 * i.e.: if a [SearchTarget] is set up to include targets by default
 * and [exclude] is applied to the package `com.example`, applying [include] to `com.example.Demo` will then result in that class being included
 * ```
 * FullSearch()
 * 	.exclude("com.example")
 * 	.include("com.example.Demo")
 */
open class SearchTarget(
		/**
		 * may not be [Inclusion.INHERIT]
		 */
		default: Inclusion
) {
	enum class Inclusion {
		INCLUDE,
		EXCLUDE,
		INHERIT
	}

	private val targets = Tree<String, Inclusion>(default.apply { if (this == Inclusion.INHERIT) throw IllegalArgumentException("Default inclusion status may not be INHERIT") })

	/**
	 * includes this package or class
	 */
	fun include(target: String): SearchTarget {
		setStatus(target, Inclusion.INCLUDE)
		return this
	}

	/**
	 * excludes this package or class
	 */
	fun exclude(target: String): SearchTarget {
		setStatus(target, Inclusion.EXCLUDE)
		return this
	}

	private fun setStatus(target: String, status: Inclusion) {
		targets.getOrDefault(target.split('.', '$'), Inclusion.INHERIT).contents = status
	}

	fun determineInclusion(path: Sequence<String>) : Boolean {
		var tree = targets
		var inclusion = tree.contents
		path.forEach {
			if (tree.contents != Inclusion.INHERIT) inclusion = tree.contents
			tree = tree.getChild(it) ?: return run {
				inclusion == Inclusion.INCLUDE
			}
		}
		return inclusion == Inclusion.INCLUDE
	}

	fun determineInclusion(path: String) = determineInclusion(path.splitToSequence('.', '$'))
}
