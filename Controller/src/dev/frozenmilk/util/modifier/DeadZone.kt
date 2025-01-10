package dev.frozenmilk.util.modifier

abstract class DeadZone<T: Comparable<T>> (val lowerDeadZone: T?, val upperDeadZone: T?) : Modifier<T> {
	protected abstract val zero: T
	override fun modify(t: T): T {
		if (t > zero && upperDeadZone != null && t < upperDeadZone) return zero
		if (t < zero && upperDeadZone != null && t > upperDeadZone) return zero
		return t
	}
}

class DoubleDeadZone private constructor(lowerDeadZone: Double?, upperDeadZone: Double?) : DeadZone<Double>(lowerDeadZone, upperDeadZone) {
	override val zero = 0.0
	companion object {
		@JvmStatic
		fun lowerDeadZone(lowerDeadZone: Double) = DoubleDeadZone(lowerDeadZone, null)
		@JvmStatic
		fun upperDeadZone(upperDeadZone: Double) = DoubleDeadZone(null, upperDeadZone)
		@JvmStatic
		fun deadZone(deadZone: Double) = DoubleDeadZone(-deadZone, deadZone)
		@JvmStatic
		fun deadZone(lowerDeadZone: Double, upperDeadZone: Double) = DoubleDeadZone(lowerDeadZone, upperDeadZone)
	}
}