package dev.frozenmilk.sinister.loading

import java.lang.annotation.Inherited

/**
 * Causes a class to be loaded if it is found by the sinister implementation [dev.frozenmilk.sinister.Sinister].
 *
 * If a class inherits this annotation at all, it will be loaded, this includes via transitive interface
 *
 * @see dev.frozenmilk.sinister.preload
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Inherited
annotation class Preload
