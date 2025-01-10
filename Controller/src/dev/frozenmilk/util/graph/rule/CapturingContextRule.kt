package dev.frozenmilk.util.graph.rule

import dev.frozenmilk.util.Box
import java.lang.FunctionalInterface
import java.lang.Runnable
import java.util.function.Consumer

@FunctionalInterface
//@JvmDefaultWithoutCompatibility
fun interface CapturingContextRule<RES: Any, CTX: Any> : ContextRule<RES, CTX> {
	override fun invokeAndResolve(context: CTX) = this(context)?.let { resolve(it); true } ?: run { fail(); false }

	infix fun <AND : Any> and(and: CapturingContextRule<AND, in CTX>): CapturingContextRule<Pair<RES, AND>, CTX> = object : CapturingContextRule<Pair<RES, AND>, CTX> {
		override fun invoke(context: CTX) = this@CapturingContextRule(context)?.let { res ->
			and.invoke(context)?.let { andRes ->
				res to andRes
			}
		}

		override fun resolve(result: Pair<RES, AND>) {
			this@CapturingContextRule.resolve(result.first)
			and.resolve(result.second)
		}

		override fun fail() {
			this@CapturingContextRule.fail()
			and.fail()
		}
	}

	infix fun <OR : Any> or(or: CapturingContextRule<OR, in CTX>): CapturingContextRule<Pair<RES?, OR?>, CTX> = object : CapturingContextRule<Pair<RES?, OR?>, CTX> {
		override fun invoke(context: CTX) =
			this@CapturingContextRule(context)?.let { res -> res to null } ?:
			or(context)?.let { res -> null to res }

		override fun resolve(result: Pair<RES?, OR?>) {
			result.first?.let {
				this@CapturingContextRule.resolve(it)
				or.fail()
			}
			result.second?.let {
				this@CapturingContextRule.fail()
				or.resolve(it)
			}
		}

		override fun fail() {
			this@CapturingContextRule.fail()
			or.fail()
		}
	}

	infix fun and(and: NonCapturingContextRule<in CTX>): CapturingContextRule<RES, CTX> = object : CapturingContextRule<RES, CTX> {
		override fun invoke(context: CTX) = this@CapturingContextRule(context)?.let { res -> and.invoke(context)?.let { res } }

		override fun resolve(result: RES) {
			this@CapturingContextRule.resolve(result)
			and.resolve()
		}

		override fun fail() {
			this@CapturingContextRule.fail()
			and.fail()
		}
	}

	infix fun or(or: NonCapturingContextRule<in CTX>): CapturingContextRule<Box<RES?>, CTX> = object : CapturingContextRule<Box<RES?>, CTX> {
		override fun invoke(context: CTX): Box<RES?>? =
			this@CapturingContextRule(context)?.let { res -> Box<RES?>(res) } ?:
			or(context)?.let { Box<RES?>(null) }

		override fun resolve(result: Box<RES?>) {
			result.ref?.let {
				this@CapturingContextRule.resolve(it)
				or.fail()
			} ?: run {
				this@CapturingContextRule.fail()
				or.resolve()
			}
		}

		override fun fail() {
			this@CapturingContextRule.fail()
			or.fail()
		}
	}

	/**
	 * calls all [onResolve] consumers
	 */
	fun resolve(result: RES) {}
	fun onResolve(receiver: Consumer<RES>): CapturingContextRule<RES, CTX> = object : CapturingContextRule<RES, CTX> {
		override fun invoke(context: CTX) = this@CapturingContextRule(context)

		override fun resolve(result: RES) {
			this@CapturingContextRule.resolve(result)
			receiver.accept(result)
		}

		override fun fail() = this@CapturingContextRule.fail()
	}

	/**
	 * calls all [onFail] consumers
	 */
	fun fail() {}
	fun onFail(receiver: Runnable): CapturingContextRule<RES, CTX> = object : CapturingContextRule<RES, CTX> {
		override fun invoke(context: CTX) = this@CapturingContextRule(context)

		override fun resolve(result: RES) = this@CapturingContextRule.resolve(result)

		override fun fail() {
			this@CapturingContextRule.fail()
			receiver.run()
		}
	}
}