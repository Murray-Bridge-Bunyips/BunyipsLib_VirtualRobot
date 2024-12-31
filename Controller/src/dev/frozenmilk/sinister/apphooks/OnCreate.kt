package dev.frozenmilk.sinister.apphooks

import android.content.Context
import dev.frozenmilk.sinister.Preload

@Preload
@FunctionalInterface
fun interface OnCreate {
	fun onCreate(context: Context)
}

@Suppress("unused")
object OnCreateFilter : HookFilter<OnCreate>(OnCreate::class.java) {
	@JvmStatic
	fun onCreate(context: Context) {
		allHooks.forEach { it.onCreate(context) }
	}
}
