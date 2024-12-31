package dev.frozenmilk.sinister.apphooks

import android.content.Context
import android.view.Menu
import dev.frozenmilk.sinister.Preload

@Preload
@FunctionalInterface
fun interface OnCreateMenu {
	fun onCreateMenu(context: Context, menu: Menu?)
}

@Suppress("unused")
object OnCreateMenuFilter : HookFilter<OnCreateMenu>(OnCreateMenu::class.java) {
	@JvmStatic
	fun onCreateMenu(context: Context, menu: Menu?) {
		allHooks.forEach { it.onCreateMenu(context, menu) }
	}
}