package dev.frozenmilk.sinister.util.warn

import dev.frozenmilk.util.cell.LazyCell

/**
 * Default impl, does nothing bc there is no DS for non android
 *
 * If we were running dash or smth then we could have an overriding implementation
 */
open class WarnImpl : WarnInterface {
	private val warnings = mutableSetOf<WarnSource>()

	override fun addSource(warnSource: WarnSource) {
		if (warnings.add(warnSource)) {
			warningMessageCell.invalidate()
			warnSource.onWarn()
		}
	}

	override fun updateSource(warnSource: WarnSource) {
		if (warnings.contains(warnSource)) warningMessageCell.invalidate()
	}

	override fun dropSource(warnSource: WarnSource) {
		if (warnings.remove(warnSource)) {
			warningMessageCell.invalidate()
			warnSource.onDrop()
		}
	}

	private val warningMessageCell = LazyCell {
		warnings.joinToString("\n") { it.warning }
	}

	override val warning by warningMessageCell
}