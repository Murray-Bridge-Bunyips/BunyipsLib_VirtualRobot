package dev.frozenmilk.sinister.targeting

/**
 * Searches nothing by default, all targets must be included by hand
 */
open class EmptySearch : SearchTarget(Inclusion.EXCLUDE)