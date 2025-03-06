package dev.frozenmilk.sinister.loading

import dev.frozenmilk.sinister.Sinister

/**
 * represents a staged load/unload operation on [Sinister]
 *
 * the operation is not performed until [release] is called
 */
class LoadEvent<LOADER: ClassLoader>(
	/**
	 * [LOADER] related to this load event
	 */
	val loader: LOADER
) {
	enum class Stage {
		/**
		 * may advance to [Cancelled] or [Released]
		 */
		Staged,
		/**
		 * will not advance further
		 */
		Cancelled,
		/**
		 * may advance to [Unloaded]
		 */
		Released,
		/**
		 * will not advance further
		 */
		Unloaded
	}

	/**
	 * current [Stage] of this load event
	 */
	var stage = Stage.Staged
		get() = synchronized(this) { field }
		private set(value) = synchronized(this) { field = value }

	private var onCancel: Runnable = Runnable {}
	/**
	 * adds an action to run if this is cancelled, before other actions
	 */
	fun beforeCancel(onCancel: Runnable): LoadEvent<LOADER> {
		synchronized(this) {
			val prev = this.onCancel
			this.onCancel = Runnable {
				onCancel.run()
				prev.run()
			}
		}
		return this
	}
	/**
	 * adds an action to run if this is cancelled, after other actions
	 */
	fun afterCancel(onCancel: Runnable): LoadEvent<LOADER> {
		synchronized(this) {
			val prev = this.onCancel
			this.onCancel = Runnable {
				prev.run()
				onCancel.run()
			}
		}
		return this
	}

	/**
	 * cancels this staged operation if [stage] is [Stage.Staged], otherwise does nothing
	 */
	fun cancel() {
		synchronized(this) {
			if (stage != Stage.Staged) return
			stage = Stage.Cancelled
			onCancel.run()
		}
	}

	private var onRelease: Runnable = Runnable {}
	/**
	 * adds an action to run if this is released, before other actions
	 */
	fun beforeRelease(onRelease: Runnable): LoadEvent<LOADER> {
		synchronized(this) {
			val prev = this.onRelease
			this.onRelease = Runnable {
				onRelease.run()
				prev.run()
			}
		}
		return this
	}
	/**
	 * adds an action to run if this is released, after other actions
	 */
	fun afterRelease(onRelease: Runnable): LoadEvent<LOADER> {
		synchronized(this) {
			val prev = this.onRelease
			this.onRelease = Runnable {
				prev.run()
				onRelease.run()
			}
		}
		return this
	}

	/**
	 * releases this staged operation if [stage] is [Stage.Staged], otherwise does nothing
	 */
	fun release() {
		synchronized(this) {
			if (stage != Stage.Staged) return
			stage = Stage.Released
			onRelease.run()
		}
	}

	private var onUnload: Runnable = Runnable {}
	/**
	 * adds an action to run if this is unloaded, before other actions
	 */
	fun beforeUnload(onUnload: Runnable): LoadEvent<LOADER> {
		synchronized(this) {
			val prev = this.onUnload
			this.onUnload = Runnable {
				onUnload.run()
				prev.run()
			}
		}
		return this
	}
	/**
	 * adds an action to run if this is unloaded, after other actions
	 */
	fun afterUnload(onUnload: Runnable): LoadEvent<LOADER> {
		synchronized(this) {
			val prev = this.onUnload
			this.onUnload = Runnable {
				prev.run()
				onUnload.run()
			}
		}
		return this
	}

	/**
	 * unloads this released operation if [stage] is [Stage.Released], otherwise does nothing
	 */
	fun unload() {
		synchronized(this) {
			if (stage != Stage.Released) return
			stage = Stage.Unloaded
			onUnload.run()
		}
	}
}
