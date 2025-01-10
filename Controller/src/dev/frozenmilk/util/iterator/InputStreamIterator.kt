package dev.frozenmilk.util.iterator

import java.io.InputStream

class InputStreamIterator(private val inputStream: InputStream) : IntIterator() {

	private var peeked = false
	private var current = EMPTY

	override fun hasNext(): Boolean {
		if (!peeked) {
			peeked = true
			current = inputStream.read()
		}
		return current != EMPTY
	}

	override fun nextInt(): Int {
		if (peeked) {
			peeked = false
			return current
		}
		current = inputStream.read()
		return current
	}

	fun available() = inputStream.available() != 0

	fun skip() {
		if (inputStream.available() == 0) return
		next()
	}

	private companion object {
		private const val EMPTY = -1
	}
}