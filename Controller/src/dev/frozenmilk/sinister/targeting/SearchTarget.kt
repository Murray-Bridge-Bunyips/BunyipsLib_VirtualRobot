package dev.frozenmilk.sinister.targeting

abstract class SearchTarget(default: Inclusion) {
	enum class Inclusion {
		INCLUDE,
		EXCLUDE,
		INHERIT
	}

	private val targets = Tree<String, Inclusion>(default.apply { if (this == Inclusion.INHERIT) throw IllegalArgumentException("Default inclusion status may not be INHERIT") })

	fun include(target: String): SearchTarget {
		setStatus(target, Inclusion.INCLUDE)
		return this
	}

	fun exclude(target: String): SearchTarget {
		setStatus(target, Inclusion.EXCLUDE)
		return this
	}

	private fun setStatus(target: String, status: Inclusion) {
		targets.getOrDefault(target.split('.', '$'), Inclusion.INHERIT).contents = status
	}

	fun determineInclusion(path: Collection<String>) : Boolean {
		val inclusion = targets[path] ?: return determineInclusion(path.take(path.size - 1))
		if (inclusion == Inclusion.INHERIT) return determineInclusion(path.take(path.size - 1))
		return inclusion == Inclusion.INCLUDE
	}

	fun determineInclusion(path: String) = determineInclusion(path.split('.', '$'))
}
