package virtual_robot

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerNotifier
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerNotifier.Notifications
import dev.frozenmilk.sinister.SinisterFilter
import dev.frozenmilk.sinister.staticInstancesOf
import dev.frozenmilk.sinister.targeting.FocusedSearch
import java.util.function.Consumer

// hooking into sinister to allow opmode notifications to work properly
// it does mean opmode manager notification listeners will actually never need to be registered through an OpModeManagerImpl,
// but we'll assume the user remembers to register their hooks when actually using the SDK
object OpModeNotificationsFilter : SinisterFilter {
    @JvmStatic
    val preInit = HashSet<Consumer<OpMode>>() // mutability is fine
    @JvmStatic
    val preStart = HashSet<Consumer<OpMode>>()
    @JvmStatic
    val postStop = HashSet<Consumer<OpMode>>()

    override val targets = FocusedSearch()

    override fun init() {
        preInit.clear()
        preStart.clear()
        postStop.clear()
    }

    override fun filter(clazz: Class<*>) {
        if (Notifications::class.java.isAssignableFrom(clazz)) {
            clazz.staticInstancesOf(Notifications::class.java).forEach {
                preInit.add{ o -> it.onOpModePreInit(o) }
                preStart.add { o -> it.onOpModePreStart(o) }
                postStop.add { o -> it.onOpModePostStop(o) }
            }
        }
    }
}