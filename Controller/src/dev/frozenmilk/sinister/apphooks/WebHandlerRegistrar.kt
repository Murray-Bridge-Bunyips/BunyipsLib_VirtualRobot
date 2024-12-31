package dev.frozenmilk.sinister.apphooks

import android.content.Context
import com.qualcomm.robotcore.util.WebHandlerManager
import dev.frozenmilk.sinister.Preload

@Preload
@FunctionalInterface
fun interface WebHandlerRegistrar {
	fun webHandlerRegistrar(context: Context, webHandlerManager: WebHandlerManager?)
}

@Suppress("unused")
object WebHandlerRegistrarFilter : HookFilter<WebHandlerRegistrar>(WebHandlerRegistrar::class.java) {
	@JvmStatic
	fun webHandlerRegistrar(context: Context, webHandlerManager: WebHandlerManager?) {
		allHooks.forEach { it.webHandlerRegistrar(context, webHandlerManager) }
	}
}