package dev.frozenmilk.util.graph.rule

import java.lang.FunctionalInterface

@FunctionalInterface
sealed interface ContextRule<RTN: Any, CTX: Any> {
	operator fun invoke(context: CTX): RTN?

	fun invokeAndResolve(context: CTX): Boolean
}