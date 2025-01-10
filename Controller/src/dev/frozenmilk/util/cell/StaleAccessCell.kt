package dev.frozenmilk.util.cell

import java.util.function.Supplier

/**
 * @param timeOut time in seconds after which the contents of the cell are no-longer valid, and so become stale and invalid, this timer gets reset when the contents are accessed in any manner
 */
class StaleAccessCell<T>(var timeOut: Double, supplier: Supplier<T>) : InvalidatingCell<T>(supplier, { self, _ ->
	self.timeSinceLastAccess > timeOut
})