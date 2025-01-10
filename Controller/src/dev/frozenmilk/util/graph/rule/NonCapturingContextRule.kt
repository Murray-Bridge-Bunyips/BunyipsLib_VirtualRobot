package dev.frozenmilk.util.graph.rule

import dev.frozenmilk.util.Box
import java.lang.FunctionalInterface
import java.lang.Runnable

@FunctionalInterface
//@JvmDefaultWithoutCompatibility
fun interface NonCapturingContextRule<CTX: Any> : ContextRule<Unit, CTX> {
	override fun invokeAndResolve(context: CTX) = this(context)?.let { resolve(); true } ?: run { fail(); false }

	infix fun <AND : Any> and(and: CapturingContextRule<AND, in CTX>): CapturingContextRule<AND, CTX> = object : CapturingContextRule<AND, CTX> {
		override fun invoke(context: CTX) = this@NonCapturingContextRule(context)?.let { and.invoke(context) }

		override fun resolve(result: AND) {
			this@NonCapturingContextRule.resolve()
			and.resolve(result)
		}

		override fun fail() {
			this@NonCapturingContextRule.fail()
			and.fail()
		}
	}

	infix fun <OR : Any> or(or: CapturingContextRule<OR, in CTX>): CapturingContextRule<Box<OR?>, CTX> = object : CapturingContextRule<Box<OR?>, CTX> {
		override fun invoke(context: CTX): Box<OR?>? = this@NonCapturingContextRule(context)?.let {
			Box<OR?>(
				null
			)
		} ?: or(context)?.let { res -> Box<OR?>(res) }

		override fun resolve(result: Box<OR?>) {
			result.ref?.let {
				this@NonCapturingContextRule.fail()
				or.resolve(it)
			} ?: run {
				this@NonCapturingContextRule.resolve()
				or.fail()
			}
		}

		override fun fail() {
			this@NonCapturingContextRule.fail()
			or.fail()
		}

	}

	infix fun and(and: NonCapturingContextRule<in CTX>): NonCapturingContextRule<CTX> = object : NonCapturingContextRule<CTX> {
		override fun invoke(context: CTX) = this@NonCapturingContextRule(context)?.let { and.invoke(context) }

		override fun resolve() {
			this@NonCapturingContextRule.resolve()
			and.resolve()
		}

		override fun fail() {
			this@NonCapturingContextRule.fail()
			and.fail()
		}
	}

	infix fun or(or: NonCapturingContextRule<in CTX>): CapturingContextRule<Boolean, CTX> = object : CapturingContextRule<Boolean, CTX> {
		override fun invoke(context: CTX): Boolean? = this@NonCapturingContextRule(context)?.let { true } ?: or(context)?.let { false }

		override fun resolve(result: Boolean) {
			if (result) {
				this@NonCapturingContextRule.resolve()
				or.fail()
			}
			else {
				this@NonCapturingContextRule.fail()
				or.resolve()
			}
		}

		override fun fail() {
			this@NonCapturingContextRule.fail()
			or.fail()
		}
	}


	/**
	 * calls all [onResolve] consumers
	 */
	fun resolve() {}
	fun onResolve(receiver: Runnable): NonCapturingContextRule<CTX> = object : NonCapturingContextRule<CTX> {
		override fun invoke(context: CTX) = this@NonCapturingContextRule(context)

		override fun resolve() {
			this@NonCapturingContextRule.resolve()
			receiver.run()
		}

		override fun fail() = this@NonCapturingContextRule.fail()
	}

	/**
	 * calls all [onFail] consumers
	 */
	fun fail() {}
	fun onFail(receiver: Runnable): NonCapturingContextRule<CTX> = object : NonCapturingContextRule<CTX> {
		override fun invoke(context: CTX) = this@NonCapturingContextRule(context)

		override fun resolve() = this@NonCapturingContextRule.resolve()

		override fun fail() {
			this@NonCapturingContextRule.fail()
			receiver.run()
		}
	}
}