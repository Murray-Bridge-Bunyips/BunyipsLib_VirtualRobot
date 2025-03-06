package dev.frozenmilk.sinister.sdk.opmodes

object BlocksOpModeRegistrar {}
//    private val blocksOpModeCons = BlocksOpMode::class.java.getDeclaredConstructor(String::class.java).apply {
//        isAccessible = true
//    }
//    private fun BlocksOpMode(name: String): BlocksOpMode = blocksOpModeCons.newInstance(name)
//    private val registered = mutableListOf<OpModeMeta>()
//    override fun onEvent(event: Int, file: File) {
//        if (event and (RecursiveFileObserver.CLOSE_WRITE or RecursiveFileObserver.DELETE or RecursiveFileObserver.MOVED_FROM or RecursiveFileObserver.MOVED_TO) != 0) {
//            Logger.v(javaClass.simpleName, "noting that Blocks changed, handling swap")
//            registered.forEach {
//				SinisterRegisteredOpModes.unregister(
//					it
//				)
//			}
//            registered.clear()
//            try {
//                registered.addAll(ProjectsUtil.fetchEnabledProjectsWithJavaScript())
//                registered.forEach {
//                    SinisterRegisteredOpModes.register(it, BlocksOpMode(it.name))
//                }
//            } catch (e: Exception) {
//				RobotLog.logStackTrace(e)
//            }
//        }
//    }
//}
//
//@Preload
//@Suppress("unused")
//private object BlocksOpModeMonitor : RecursiveFileObserver(
//	AppUtil.BLOCK_OPMODES_DIR,
//	CLOSE_WRITE or DELETE or MOVED_FROM or MOVED_TO,
//	Mode.NONRECURSVIVE,
//	BlocksOpModeRegistrar
//) {
//	init {
//		startWatching()
//	}
//}
