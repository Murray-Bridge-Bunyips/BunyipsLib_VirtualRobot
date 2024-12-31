package dev.frozenmilk.sinister.apphooks

import android.content.Context
import com.qualcomm.ftccommon.FtcEventLoop
import dev.frozenmilk.sinister.Preload

@Preload
@FunctionalInterface
fun interface OnCreateEventLoop {
	fun onCreateEventLoop(context: Context, ftcEventLoop: FtcEventLoop)
}

object OnCreateEventLoopFilter : HookFilter<OnCreateEventLoop>(OnCreateEventLoop::class.java) {
	@JvmStatic
	fun onCreateEventLoop(context: Context, ftcEventLoop: FtcEventLoop) {
		allHooks.forEach {
			it.onCreateEventLoop(context, ftcEventLoop)
		}
	}
}