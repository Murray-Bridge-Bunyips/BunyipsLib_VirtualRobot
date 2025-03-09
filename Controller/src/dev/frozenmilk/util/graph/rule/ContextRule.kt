package dev.frozenmilk.util.graph.rule

@FunctionalInterface
sealed interface ContextRule<RTN: Any, CTX: Any> {
	operator fun invoke(context: CTX): RTN?

	fun invokeAndResolve(context: CTX): Boolean
}