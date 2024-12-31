package dev.frozenmilk.sinister.apphooks

import android.content.Context
import dev.frozenmilk.sinister.Preload

@Preload
@FunctionalInterface
fun interface OnDestroy {
	fun onDestroy(context: Context)
}

@Suppress("unused")
object OnDestroyFilter : HookFilter<OnDestroy>(OnDestroy::class.java) {
	@JvmStatic
	fun onDestroy(context: Context) {
		allHooks.forEach { it.onDestroy(context) }
	}
}