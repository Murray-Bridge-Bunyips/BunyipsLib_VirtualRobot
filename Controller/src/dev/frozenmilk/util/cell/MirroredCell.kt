package dev.frozenmilk.util.cell

import java.lang.reflect.Field
import java.util.function.Supplier

/**
 * a cell that manages a reference through reflection
 */
@Suppress("UNCHECKED_CAST")
open class MirroredCell<T>
/**
 * @param field should be set to be accessible
 */
(
        private val parent: Any,
        private val field: Field,
) : LateInitCell<T>(null, "Attempted to obtain a null value from a mirrored cell") // we actually don't care about the ref in this circumstance
{
    constructor(parent: Any, field: String) : this(parent, recurseFindField(parent::class.java, field)) {
        this.field.isAccessible = true;
    }

    override var ref: T?
        get() = this.field.get(parent) as? T?
        set(value) { this.field.set(parent, value) }
}

@Suppress("UNCHECKED_CAST")
open class SupplierMirroredCell<T>(
        private val parentSupplier: Supplier<Any>,
        private val field: Field,
) : LateInitCell<T>(null, "Attempted to obtain a null value from a mirrored cell") {
    constructor(parentSupplier: Supplier<Any>, field: String) : this(parentSupplier, recurseFindField(parentSupplier::class.java, field))
    init {
        field.isAccessible = true;
    }

    override var ref: T?
        get() = this.field.get(parentSupplier.get()) as? T?
        set(value) { this.field.set(parentSupplier.get(), value) }
}

@Throws(IllegalStateException::class)
private fun recurseFindField(clazz: Class<*>?, field: String): Field {
    if (clazz == null) throw IllegalStateException("unable to find field \"$field\" after searching all classes and super classes")
    return try {
        clazz.getDeclaredField(field)
    } catch (_: Exception) {
        recurseFindField(clazz.superclass, field)
    }
}
