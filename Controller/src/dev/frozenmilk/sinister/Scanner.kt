package dev.frozenmilk.sinister

import dev.frozenmilk.sinister.configurable.ConfigurableScanner
import dev.frozenmilk.sinister.loading.Pinned
import dev.frozenmilk.sinister.loading.Preload
import dev.frozenmilk.sinister.targeting.SearchTarget
import dev.frozenmilk.util.graph.Graph
import dev.frozenmilk.util.graph.rule.AdjacencyRule
import dev.frozenmilk.util.graph.rule.dependedOn
import dev.frozenmilk.util.graph.rule.dependsOn
import dev.frozenmilk.util.graph.rule.independent

/**
 * static implementations of this will get invoked at runtime by [dev.frozenmilk.sinister.Sinister]
 *
 * WARNING: all classes that implement [Scanner] are [Preload]ed by
 * [dev.frozenmilk.sinister.Sinister], this should most likely not cause issues
 */
@Preload
@Pinned
//@JvmDefaultWithoutCompatibility // TODO: oop
interface Scanner {
	/**
	 * allows this to depend on other [Scanner]s for loads
	 *
	 * the vast majority of [Scanner]s should use [afterConfiguration],
	 * or should include it
	 *
	 * dependency means that that [Scanner]'s full load cycle will
	 * finish before this [Scanner]'s load cycle starts
	 */
	val loadAdjacencyRule: AdjacencyRule<Scanner, Graph<Scanner>>

	/**
	 * allows this to depend on other [Scanner]s for unloads
	 *
	 * the vast majority of [Scanner]s should use [beforeConfiguration],
	 * or should include it
	 *
	 * dependency means that that [Scanner]'s full unload cycle will
	 * finish before this [Scanner]'s unload cycle starts
	 */
	val unloadAdjacencyRule: AdjacencyRule<Scanner, Graph<Scanner>>

	/**
	 * items that should be ignored
	 */
	val targets: SearchTarget

	/**
	 * gets run before [scan] is called for a round of scanning
	 */
	fun beforeScan(loader: ClassLoader) {}

	/**
	 * gets run for all classes as accepted by [targets]
	 *
	 * if this class records [cls] in anyway, it must ensure that it is
	 * capable of undoing this, to fulfil [unload], additionally, it must
	 * store the [ClassLoader] of [cls], so that it can unload the [ClassLoader]
	 *
	 * this method can be run in parallel with other instances of
	 * [Scanner], but this instance will not have [scan] called in
	 * parallel.
	 *
	 * this means that you should be careful when accessing shared resources.
	 *
	 * this helps to cut down on boot time, but also should prevent most
	 * synchronisation issues from appearing
	 */
	fun scan(loader: ClassLoader, cls: Class<*>)

	/**
	 * gets run after [scan] is called for a round of scanning
	 */
	fun afterScan(loader: ClassLoader) {}

	/**
	 * gets run before [unload] is called for a round of unloading
	 */
	fun beforeUnload(loader: ClassLoader) {}

	/**
	 * gets run for all classes as accepted by [targets] as they get unloaded.
	 *
	 * this method can be run in parallel with other instances of
	 * [Scanner], but this instance will not have [unload] called in
	 * parallel.
	 *
	 * this means that you should be careful when accessing shared resources.
	 *
	 * this helps to cut down on dynamic load time, but also should prevent
	 * most synchronisation issues from appearing
	 */
	fun unload(loader: ClassLoader, cls: Class<*>)

	/**
	 * gets run after [unload] is called for a round of unloading
	 */
	fun afterUnload(loader: ClassLoader) {}

	fun afterConfiguration(): AdjacencyRule<Scanner, Graph<Scanner>> = dependsOn(ConfigurableScanner)
	fun beforeConfiguration(): AdjacencyRule<Scanner, Graph<Scanner>> = dependedOn(ConfigurableScanner)

	companion object {
		@JvmStatic
		@get:JvmName("INDEPENDENT")
		val INDEPENDENT = independent<Scanner, Graph<Scanner>>()
	}
}