package dev.frozenmilk.util.modifier

@FunctionalInterface
fun interface Modifier<T> {
	fun modify(t: T): T
}

@FunctionalInterface
fun interface BiModifier<T, U> {
	fun modify(t: T, u: U): T
}
