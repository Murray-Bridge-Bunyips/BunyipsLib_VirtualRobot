package dev.frozenmilk.sinister.loading

import java.lang.annotation.Inherited

/**
 * Prevents a class from being unloaded by [dev.frozenmilk.sinister.Sinister].
 *
 * This means that it will only ever be loaded from the base apk.
 *
 * Changing a file to add or remove `@Pinned` and then loading it dynamically is undefined behaviour
 *
 * If a class inherits this annotation at all, it will be loaded from the base apk, this includes via transitive interface.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Inherited
annotation class Pinned