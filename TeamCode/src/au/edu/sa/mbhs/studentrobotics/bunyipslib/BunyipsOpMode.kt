package au.edu.sa.mbhs.studentrobotics.bunyipslib

import android.graphics.Color
import au.edu.sa.mbhs.studentrobotics.bunyipslib.executables.MovingAverageTimer
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Measure
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Time
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Units.*
import au.edu.sa.mbhs.studentrobotics.bunyipslib.hardware.Controller
import au.edu.sa.mbhs.studentrobotics.bunyipslib.integrated.OpModes
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.bases.Task
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Dashboard
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Dbg
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Exceptions
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Threads
import com.acmerobotics.roadrunner.Action
import com.acmerobotics.roadrunner.ftc.throwIfModulesAreOutdated
import com.qualcomm.ftccommon.SoundPlayer
import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.hardware.Blinker
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareDevice
import com.qualcomm.robotcore.hardware.LightSensor
import com.qualcomm.robotcore.hardware.RobotCoreLynxUsbDevice
import com.qualcomm.robotcore.hardware.ServoController
import org.firstinspires.ftc.robotcore.internal.ui.GamepadUser
import java.util.Optional
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import kotlin.math.abs


/**
 * [BunyipsOpMode]
 *
 * The gateway to the BunyipsLib framework, BunyipsOpMode is a wrapper around the FTC SDK's LinearOpMode.
 *
 * This class provides a structured way to manage the lifecycle of an OpMode, and provides a number of
 * utility functions to make development easier. This class is designed to be extended by the user to
 * create their own OpModes, and has native support for linked Driver Station & FtcDashboard telemetry ([DualTelemetry]),
 * native custom gamepads ([Controller]), timing utilities ([timer]), exception handling ([Exceptions]),
 * and more through the BunyipsLib suite of tools ([BunyipsSubsystem], [Scheduler], etc.)
 *
 * @see CommandBasedBunyipsOpMode
 * @see AutonomousBunyipsOpMode
 * @author Lucas Bubner, 2024
 * @since 1.0.0-pre
 */
abstract class BunyipsOpMode : BOMInternal() {
    /**
     * The moving average timer for the OpMode, which is used to calculate time
     * between hardware cycles. This is useful for debugging, performance monitoring, and calculating various
     * time-based values (deltaTime, loopCount, elapsedTime, etc.)
     *
     * This timer is automatically active during the `dynamic_init` and `running` phases of the OpMode, and is reset in between.
     * If you wish to get the *total* time since this OpMode was started, you can call the built-in [getRuntime] method.
     * @see MovingAverageTimer
     */
    lateinit var timer: MovingAverageTimer

    /**
     * BunyipsLib Gamepad 1: Driver
     * @see Controller
     */
    lateinit var gamepad1: Controller

    /**
     * BunyipsLib Gamepad 2: Operator
     * @see Controller
     */
    lateinit var gamepad2: Controller

    /**
     * BunyipsLib Driver Station & FtcDashboard Telemetry
     * @see DualTelemetry
     */
    lateinit var telemetry: DualTelemetry

    /**
     * Shorthand field alias for the [telemetry] ([DualTelemetry]) field.
     * Sometimes, this field is required to be used as the Kotlin compiler might have trouble distinguishing between
     * the overridden field and the base field. This method is a direct alias to the DualTelemetry field.
     * @see telemetry
     */
    lateinit var t: DualTelemetry

    /**
     * Set how fast the OpMode is able to loop in the [activeLoop] or [onInitLoop] methods in Loops per Time Unit.
     * This will dynamically adjust to be the target speed of how fast loops should be executed.
     * Measures of less than or equal to zero will be ignored, and the OpMode will run as fast as possible.
     */
    var loopSpeed: Measure<Time> = Seconds.zero()
        set(value) {
            // Warn the user if their target loop speed cannot be achieved
            telemetry.loopSpeedSlowAlert = field
            field = value
            Dbg.warn("BunyipsOpMode: init/active loop speed set to % ms!", value to Milliseconds)
        }

    /**
     * A list of all LynxModules (Control + Expansion Hub) modules on the robot.
     */
    val robotControllers: List<LynxModule> by lazy { hardwareMap.getAll(LynxModule::class.java) }

    private var operationsCompleted = false
    private var operationsPaused = false
    private var safeHaltHardwareOnStop = false

    private val runnables = mutableSetOf<Runnable>()
    private var initTask: Action? = null

    companion object {
        private var _instance: BunyipsOpMode? = null

        /**
         * The instance of the current [BunyipsOpMode]. This is set automatically by the [BunyipsOpMode] lifecycle.
         * This can be used instead of dependency injection to access the current OpMode, as it is a singleton.
         *
         * If you choose to access the current OpMode through this property, you must ensure that the OpMode
         * is actively running, otherwise this property will be null, and you will raise a full-crashing exception.
         * The OpMode is generally available upon construction of your OpMode, so exceptions should be rare if you
         * are using a [BunyipsOpMode].
         *
         * You may also choose to access the currently running base `OpMode` instance through the [BunyipsLib] class.
         *
         * @throws Exceptions.EmergencyStop If a [BunyipsOpMode] is not running, this exception will be raised with a cause of [UninitializedPropertyAccessException].
         * @return The instance of the current [BunyipsOpMode].
         */
        @JvmStatic
        val instance: BunyipsOpMode
            // If Kotlin throws an UninitializedPropertyAccessException, it will crash the DS and require a full
            // restart, so we will handle this exception ourselves and supply a more informative message.
            get() = _instance
                ?: throw Exceptions.EmergencyStop(
                    "Attempted to access a BunyipsOpMode that is not running! " +
                            "This is due to a `BunyipsOpMode.getInstance()` call that has been invoked without checking if there is an active BunyipsOpMode running. " +
                            "The most likely cause is that a component has tried to access a running BunyipsOpMode when none is running. " +
                            "For this component to function properly, it must be run during the execution of a BunyipsOpMode or initialisation should be delayed until an instance is available.",
                    UninitializedPropertyAccessException("`BunyipsOpMode._instance` field is null, due to access prior to the `configureObjects()` hook.")
                )

        /**
         * Whether a [BunyipsOpMode] is currently running. This is useful for checking if the OpMode singleton can be accessed
         * without raising an exception due to the field being null.
         *
         * @return Whether a [BunyipsOpMode] is currently running.
         */
        @JvmStatic
        val isRunning: Boolean
            get() = _instance != null

        /**
         * Run the supplied callback if a [BunyipsOpMode] is currently running. This chains an internal call to [BunyipsOpMode]
         * with a lambda supplied with the non-null instance of [BunyipsOpMode].
         *
         * The consumer will no-op if a [BunyipsOpMode] is not running.
         */
        @JvmStatic
        fun ifRunning(opModeConsumer: Consumer<BunyipsOpMode>) {
            if (isRunning)
                opModeConsumer.accept(instance)
        }

        @JvmStatic
        @Hook(on = Hook.Target.PRE_INIT, priority = 6)
        private fun configureObjects() {
            val bom = _instance ?: return
            Dbg.logv("BunyipsOpMode: setting up ...")
            bom.let {
                it.timer = MovingAverageTimer()
                it.telemetry = DualTelemetry(it.sdkTelemetry, it.timer)
                it.t = it.telemetry
                it.gamepad1 = Controller(GamepadUser.ONE)
                it.gamepad2 = Controller(GamepadUser.TWO)
                Controller.inject(it.gamepad1, it.gamepad2)
                // Update OpModeInternal fields (partial paranoia, inject handles this)
                it.sdkGamepad1 = it.gamepad1
                it.sdkGamepad2 = it.gamepad2
                it.sdkTelemetry = it.telemetry
            }
        }

        const val ERROR = "<font color='red'><b>error</b></font>"
    }

    init {
        // Early assign an instance of BunyipsOpMode to allow member field access of the derived class
        // to access references to the OpMode. We re-assign this value in runBunyipsOpMode() for paranoia to ensure
        // a fully constructed instance is available. Usually, we don't need to get the instance of the derived class,
        // so leaking the instance here is fine. We need this field to be available to early assign values for preInit.
        @Suppress("LeakingThis")
        _instance = this
    }

    /**
     * Runs upon the pressing of the `INIT` button on the Driver Station.
     *
     * This is where you should initialise your hardware (if applicable) and other components.
     *
     * Override this method to use it.
     */
    protected open fun onInit() {
        // no-op (>= v7.0.0)
    }

    /**
     * Run code in a loop AFTER [onInit] has completed, until start is pressed on the Driver Station
     * and the init-task ([setInitTask]) is done. The boolean returned by this method will indicate the status
     * of the init-loop, if it returns true, the init-loop will finish early.
     *
     * If not implemented and no init-task is defined, the OpMode will continue on as normal and wait for start.
     */
    protected open fun onInitLoop(): Boolean {
        return true
    }

    /**
     * Allow code to execute once after all initialisation has finished.
     *
     * Note: This method is always called even if initialisation is cut short by the Driver Station.
     */
    protected open fun onInitDone() {
        // no-op
    }

    /**
     * Perform one time operations after start is pressed.
     *
     * Unlike [onInitDone], this will only execute once play is hit and not when initialisation is done.
     */
    protected open fun onStart() {
        // no-op
    }

    /**
     * Code to run continuously after the `START` button is pressed on the Driver Station.
     *
     * This method will be called on each hardware cycle, but may not be called if the OpMode is stopped before starting.
     */
    protected abstract fun activeLoop()

    /**
     * Perform one time clean-up operations after the [activeLoop] finishes all intentions gracefully.
     *
     * This method is called after a [finish] or [exit] call, but may not be called if the OpMode is
     * terminated by an *unhandled fatal* exception/early stop.
     *
     * This method is useful for ensuring the robot is in a safe state after the OpMode has finished.
     * In an exception-less OpMode, this method will be called before [onStop].
     *
     * @see onStop
     */
    protected open fun onFinish() {
        // no-op
    }

    /**
     * Perform one time clean-up operations as the OpMode is stopping.
     *
     * This method is called after the OpMode has been requested to stop, and will be the last method
     * called before the OpMode is terminated, and is *guaranteed* to be called.
     *
     * This method is useful for releasing resources to prevent memory leaks, as motor controllers will be powered off
     * as the OpMode is ending.
     *
     * @see onFinish
     */
    protected open fun onStop() {
        // no-op
    }

    /**
     * This method is the entry point for the BunyipsLib framework, and is called by the FTC SDK.
     *
     * @throws InterruptedException If the OpMode is interrupted by the FTC SDK, this exception will be raised.
     */
    @Throws(InterruptedException::class)
    final override fun runBunyipsOpMode() {
        // BunyipsOpMode
        _instance = this
        try {
            throwIfModulesAreOutdated(hardwareMap)
            robotControllers.forEach { module ->
                module.bulkCachingMode = LynxModule.BulkCachingMode.MANUAL
                module.setConstant(Color.CYAN)
            }
            telemetry.init("<small><font color='#e5ffde'>bunyipslib</font> <font color='gray'>v${BuildConfig.SEMVER}-${BuildConfig.GIT_COMMIT}-${BuildConfig.BUILD_TIME}</font></small>")
            telemetry.overheadTag = "<b>${javaClass.simpleName}</b>"
            // An EmergencyStop which is handled by Exceptions may be raised on another thread.
            // However, we need this throw operation to be on this thread and polled often, so we attach it as a telemetry action.
            telemetry.addAction {
                Exceptions.THROWN_EXCEPTIONS.filterIsInstance<Exceptions.EmergencyStop>().forEach { throw it }
            }
            telemetry.update()

            telemetry.opModeStatus = "<b><font color='yellow'>static_init</font></b>"
            telemetry.logBracketColor = "gray"
            Dbg.logv("BunyipsOpMode: firing onInit() ...")
            // Store telemetry objects raised by onInit() by turning off auto-clear
            telemetry.isAutoClear = false
            telemetry.update()
            var ok = true
            if (!gamepad1.atRest() || !gamepad2.atRest()) {
                ok = false
                telemetry.log("<b><font color='yellow'>WARNING!</font></b> a gamepad was not zeroed during init. please ensure controllers zero out correctly by disconnecting and reconnecting them.")
                telemetry.overrideStatus = "<font color='yellow'><b>check gamepads</b></font>"
            }
            // Run user-defined setup
            try {
                onInit()
            } catch (e: Exception) {
                // Catch all exceptions, log them, and continue running the OpMode
                // All InterruptedExceptions are handled by the FTC SDK and are raised in the Exceptions handler
                ok = false
                telemetry.overrideStatus = ERROR
                Exceptions.handle(e, telemetry::log)
            }

            telemetry.opModeStatus = "<font color='aqua'>dynamic_init</font>"
            telemetry.update()
            Dbg.logv("BunyipsOpMode: starting onInitLoop() ...")
            if (initTask != null) {
                Dbg.logd(
                    "BunyipsOpMode: running initTask -> % ...",
                    if (initTask is Task) (initTask as Task).toVerboseString() else initTask
                )
                telemetry.log("<font color='gray'>running init-task:</font> %", initTask)
            }
            robotControllers.forEach { module ->
                module.pattern = listOf(
                    Blinker.Step(Color.CYAN, 400, TimeUnit.MILLISECONDS),
                    Blinker.Step(Color.BLACK, 400, TimeUnit.MILLISECONDS)
                )
            }
            // Run user-defined dynamic initialisation
            var done = false
            while (opModeInInit() && !operationsCompleted) {
                val curr = System.nanoTime()
                Dashboard.usePacket {
                    try {
                        robotControllers.forEach { m -> m.clearBulkCache() }
                        // Run until onInitLoop returns true, and the initTask is done, or the OpMode is continued
                        initTask?.preview(it.fieldOverlay())
                        done = onInitLoop() && (initTask == null || !initTask!!.run(it))
                    } catch (e: Exception) {
                        ok = false
                        telemetry.overrideStatus = ERROR
                        Exceptions.handle(e, telemetry::log)
                    }
                }
                timer.update()
                telemetry.update()
                if (loopSpeed.magnitude() > 0)
                    sleep(abs(((loopSpeed to Nanoseconds).toLong() - (System.nanoTime() - curr))) / 1_000_000)
                if (done)
                    break
            }

            telemetry.opModeStatus = "<font color='yellow'>finish_init</font>"
            // We can only finish Task objects, as the RobotTask interface does not have a finish method
            // Most tasks will inherit from Task, so this should be safe, but this is to ensure maximum compatibility
            try {
                if (initTask is Task && !(initTask as Task).isFinished) {
                    Dbg.warn("BunyipsOpMode: initTask did not finish in time, early finishing -> % ...", initTask)
                    telemetry.log("<font color='gray'>init-task interrupted by start request.</font>")
                    (initTask as Task).finishNow()
                } else if (initTask != null) {
                    Dbg.logd("BunyipsOpMode: initTask finished -> % ...", initTask)
                    telemetry.log("<font color='gray'>init-task finished.</font>")
                }
            } catch (e: Exception) {
                ok = false
                telemetry.overrideStatus = ERROR
                Exceptions.handle(e, telemetry::log)
            }
            telemetry.update()
            Dbg.logv("BunyipsOpMode: firing onInitDone() ...")
            // Run user-defined final initialisation
            try {
                onInitDone()
            } catch (e: Exception) {
                ok = false
                telemetry.overrideStatus = ERROR
                Exceptions.handle(e, telemetry::log)
            }
            // Other related exceptions may have been thrown nested or on threads
            if (ok && Exceptions.THROWN_EXCEPTIONS.isNotEmpty()) {
                telemetry.overrideStatus = ERROR
                ok = false
            }
            robotControllers.forEach { module ->
                if (ok)
                    module.pattern = listOf(
                        Blinker.Step(Color.GREEN, 400, TimeUnit.MILLISECONDS),
                        Blinker.Step(Color.CYAN, 400, TimeUnit.MILLISECONDS)
                    )
                else
                    module.setConstant(Color.YELLOW)
            }

            // Ready to go.
            telemetry.opModeStatus = "<font color='green'>ready</font>"
            timer.update()
            Dbg.logd("BunyipsOpMode: init cycle completed in ${timer.elapsedTime() to Seconds} secs.")
            telemetry.addDS("<b>Init <font color='green'>complete</font>. Press play to start.</b>")
            Dbg.logd("BunyipsOpMode: ready.")

            // Wait for start
            while (!isStarted && !isStopRequested) {
                telemetry.update()
                // Save some CPU cycles
                sleep(200)
                idle()
            }

            if (isStopRequested)
                return

            // Play button has been pressed
            telemetry.opModeStatus = "<font color='yellow'>starting</font>"
            telemetry.isAutoClear = true
            telemetry.clear()
            Dbg.logv("BunyipsOpMode: firing onStart() ...")
            try {
                // Run user-defined start operations
                onStart()
                telemetry.overrideStatus = if (telemetry.overrideStatus == ERROR) null else telemetry.overrideStatus
            } catch (e: Exception) {
                telemetry.overrideStatus = ERROR
                Exceptions.handle(e, telemetry::log)
            }
            telemetry.update()
            timer.reset()
            telemetry.logBracketColor = "green"
            robotControllers.forEach { module ->
                // Note any changes we make to lights will be undone later by an automatic hook
                module.pattern = listOf(
                    Blinker.Step(Color.GREEN, 200, TimeUnit.MILLISECONDS),
                    Blinker.Step(Color.BLACK, 200, TimeUnit.MILLISECONDS)
                )
            }

            telemetry.opModeStatus = "<font color='green'><b>running</b></font>"
            Dbg.logv("BunyipsOpMode: starting activeLoop() ...")
            while (!isStopRequested && isStarted && !operationsCompleted) {
                if (operationsPaused) {
                    // If the OpMode is paused, skip the loop and wait for the next hardware cycle
                    timer.update()
                    telemetry.update()
                    // Slow down the OpMode as this is all we're doing
                    sleep(100)
                    idle()
                    continue
                }
                val curr = System.nanoTime()
                robotControllers.forEach { m -> m.clearBulkCache() }
                try {
                    // Run user-defined active loop
                    runnables.forEach { Exceptions.runUserMethod(it) }
                    activeLoop()
                } catch (e: Exception) {
                    telemetry.overrideStatus = ERROR
                    Exceptions.handle(e, telemetry::log)
                }
                // Update telemetry and timers
                timer.update()
                telemetry.update()
                if (loopSpeed.magnitude() > 0)
                    sleep(abs(((loopSpeed to Nanoseconds).toLong() - (System.nanoTime() - curr))) / 1_000_000)
            }

            telemetry.opModeStatus = "<font color='gray'>finished</font>"
            try {
                onFinish()
            } catch (e: Exception) {
                telemetry.overrideStatus = ERROR
                Exceptions.handle(e, telemetry::log)
            }
            // Timer will be frozen from hereon
            timer.update()
            telemetry.update()
            Dbg.logd("BunyipsOpMode: all tasks finished.")
            robotControllers.forEach { module ->
                if (safeHaltHardwareOnStop)
                    module.setConstant(Color.LTGRAY)
                else
                    module.pattern = listOf(
                        Blinker.Step(Color.LTGRAY, 600, TimeUnit.MILLISECONDS),
                        Blinker.Step(Color.BLACK, 600, TimeUnit.MILLISECONDS)
                    )
            }
            // Wait for user to hit stop or for the OpMode to be terminated
            // We will continue running the OpMode as there might be some other user threads
            // under Threads that can still run, so we will wait indefinitely and simulate
            // what the FTC SDK does when the OpMode is stopped if they wish.
            // This has been made optional as users may want to hold a position or keep a motor
            // running after the OpMode has finished
            while (opModeIsActive()) {
                if (safeHaltHardwareOnStop)
                    safeHaltHardware()
                // Save some CPU cycles
                telemetry.update()
                sleep(500)
            }
        } catch (t: Throwable) {
            Dbg.error("BunyipsOpMode: unhandled throwable! Stacktrace:")
            Dbg.sendStacktrace(t)
            OpModes.inhibitNextLightsReset()
            robotControllers.forEach { module ->
                module.setConstant(Color.RED)
            }
            // As this error occurred somewhere not inside user code, we should not swallow it.
            // There is also a chance this error is an instance of Error, in which case we should
            // exit immediately as something has gone very wrong
            throw t
        } finally {
            Dbg.logd("BunyipsOpMode: opmode stop requested. cleaning up ...")
            // Ensure all threads have been told to stop, even if there is a post-stop hook
            Threads.stopAll()
            SoundPlayer.getInstance().stopPlayingAll()
            try {
                onStop()
            } catch (e: Exception) {
                telemetry.overrideStatus = ERROR
                Exceptions.handle(e, telemetry::log)
            }
            _instance = null
            safeHaltHardware()
            // Telemetry may be not in a nice state, so we will call our stateful functions
            // such as thread stops and cleanup in onStop() first before updating the status
            telemetry.opModeStatus = "<font color='red'>terminating</font>"
            Dbg.logd("BunyipsOpMode: active cycle completed in ${timer.elapsedTime() to Seconds} secs.")
            telemetry.update()
            Dbg.logv("BunyipsOpMode: exiting ...")
        }
    }

    /**
     * Get the currently respected init-task that will run during `dynamic_init`.
     */
    fun getInitTask(): Optional<Action> {
        return Optional.ofNullable(initTask)
    }

    /**
     * Set a task (exposed as minimum type of [Action]) that will run as an init-task. This will run
     * after your [onInit] has completed, allowing you to initialise hardware first.
     * This is an optional method, and runs alongside [onInitLoop].
     *
     * You should store any running variables inside the task itself, and keep the instance of the task
     * defined as a field in your OpMode. You can then use this value in your [onInitDone] to do
     * what you need to after the init-task has finished, or use appropriate [Task] callbacks.
     * This method should be paired with [onInitDone] to do anything after the initTask has finished, if desired.
     *
     * If you do not define an initTask, then running it during the `dynamic_init` phase will be skipped.
     * Note that there can only be one init-task set. Consider a task/action group for multiple operations.
     *
     * @see onInitDone
     */
    fun setInitTask(task: Action) {
        if (initTask != null) {
            Dbg.warn("BunyipsOpMode: init-task has already been set to %, overriding it with %...", initTask, task)
        }
        initTask = task
    }

    /**
     * Add a [Runnable] to the list of runnables to be executed just before the [activeLoop].
     * This is useful for running code that needs to be executed on the main thread, but is not
     * a subsystem or task.
     *
     * This method is public to allow you to add looping code from [RobotConfig], [Task], and other contexts.
     * The runnables will run in the order they were added, and duplicate Runnable instances will be ignored.
     */
    fun onActiveLoop(vararg runnables: Runnable) {
        // Using a set to not have multiple instances of the same Runnable
        this.runnables.addAll(runnables)
        Dbg.logv("BunyipsOpMode: added % activeLoop task(s), % task(s) set.", runnables.size, this.runnables.size)
    }

    /**
     * Add a [Runnable] to the list of runnables to be executed just before the [activeLoop].
     * This is useful for running code that needs to be executed on the main thread, but is not
     * a subsystem or task.
     *
     * This method is public to allow you to add looping code from [RobotConfig], [Task], and other contexts.
     * This runnable will run in the order they were added, and duplicate Runnable instances will be ignored.
     */
    fun onActiveLoop(runnable: Runnable) {
        this.runnables.add(runnable)
        Dbg.logv("BunyipsOpMode: added an activeLoop task, % task(s) set.", this.runnables.size)
    }

    /**
     * Removes a [Runnable] that was added to the active loop via [onActiveLoop].
     * Calling this method will no-op on runnables that can't be found on the currently attached runnables,
     * and will stop executing runnables on the active loop that could be found.
     */
    fun detachActiveLoopRunnables(vararg attachedRunnables: Runnable) {
        val oldSize = runnables.size
        runnables.removeAll(attachedRunnables.toSet())
        if (runnables.size != oldSize) {
            Dbg.logv("BunyipsOpMode: removed % activeLoop task(s), % task(s) remain.", oldSize, runnables.size)
        }
    }

    /**
     * Call to manually finish the OpMode.
     *
     * This is a dangerous method, as the OpMode will end main thread code and optionally disable hardware while still
     * remaining active on the Driver Station. This allows post-review of telemetry as it is cleared when the OpMode ends.
     *
     * This method should be called when the OpMode is finished and no longer needs to run, and will
     * put the OpMode in a state where it will not run any more code (including timers & telemetry).
     *
     * @param safeHaltHardwareOnStop If true (default), all motors/devices will be actively told to stop for the remainder of the OpMode.
     */
    @JvmOverloads
    fun finish(safeHaltHardwareOnStop: Boolean = true) {
        if (operationsCompleted) {
            return
        }
        this.safeHaltHardwareOnStop = safeHaltHardwareOnStop
        operationsCompleted = true
        Dbg.logd("BunyipsOpMode: activeLoop() terminated by finish().")
        telemetry.addRetained(
            "<b>Robot ${if (safeHaltHardwareOnStop) "<font color='green'>is stopped</font>" else "tasks finished"}. All operations completed.</b>",
            true
        )
        telemetry.update()
    }

    /**
     * Dangerous method: Call to command all motors and servos on the robot to shut down. This is a method
     * called by the SDK continuously when no OpMode is running. It is also automatically called via [finish].
     *
     * Do note [safeHaltHardware] may be an operation for some devices that can only be restored on an OpMode
     * restart or by re-enabling the devices manually. See [stopMotors] to simply set motor powers to zero.
     *
     * This method internally powers down motors, LynxModules, servos, and light sensors.
     */
    fun safeHaltHardware() {
        // Set all motor powers to zero. The implementation here will also stop any CRServos.
        for (motor in hardwareMap.getAll(DcMotorSimple::class.java)) {
            // Avoid enabling servos if they are already zero power
            if (motor.power != 0.0) motor.power = 0.0
        }
        // Shutdown all Lynx devices, that's all we need to do for these devices
        for (device in hardwareMap.getAll(RobotCoreLynxUsbDevice::class.java)) {
            device.failSafe()
        }
        // Power down the servos
        for (servoController in hardwareMap.getAll(ServoController::class.java)) {
            if (servoController.manufacturer != HardwareDevice.Manufacturer.Lynx) {
                servoController.pwmDisable()
            }
        }
        // Set motors to safe state
        for (dcMotor in hardwareMap.getAll(DcMotor::class.java)) {
            if (dcMotor.manufacturer != HardwareDevice.Manufacturer.Lynx) {
                dcMotor.power = 0.0
                dcMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            }
        }
        // Turn off light sensors
        for (light in hardwareMap.getAll(LightSensor::class.java)) {
            light.enableLed(false)
        }
    }

    /**
     * Call to command all moving motors/CRServos to stop by method of setting their powers to zero.
     * This does not impact normal servos.
     *
     * A more aggressive/full-safe approach to stopping can be done via [safeHaltHardware], which is an
     * internal SDK method that runs when no OpMode is running and also shuts down servos.
     */
    fun stopMotors() {
        for (motor in hardwareMap.getAll(DcMotorSimple::class.java)) {
            // Avoid enabling servos if they are already zero power
            if (motor.power != 0.0) motor.power = 0.0
            if (motor is DcMotor)
                motor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        }
    }

    /**
     * Call to temporarily halt all [activeLoop]-related updates from running.
     * Note this will pause the entire [activeLoop], but continue to update timers and telemetry. These events
     * must be handled manually if needed, which include any conditional calls to [resume].
     *
     * Ensure hardware state is properly set before halting, as they will no longer receive updates via the main thread.
     */
    fun halt() {
        if (operationsPaused) {
            return
        }
        operationsPaused = true
        telemetry.overrideStatus = "<font color='yellow'>halted</font>"
        Dbg.logd("BunyipsOpMode: activeLoop() halted.")
    }

    /**
     * Call to resume the [activeLoop] after a [halt] call.
     */
    fun resume() {
        if (!operationsPaused) {
            return
        }
        operationsPaused = false
        telemetry.overrideStatus = null
        Dbg.logd("BunyipsOpMode: activeLoop() resumed.")
    }

    /**
     * Dangerous method: call to shut down the OpMode as soon as possible.
     * This will run any [BunyipsOpMode] cleanup code, much as if the user pressed the `STOP` button.
     */
    fun exit() {
        Dbg.logd("BunyipsOpMode: exiting opmode...")
        finish()
        requestOpModeStop()
    }

    /**
     * Dangerous method: call to IMMEDIATELY terminate the OpMode.
     * **No further code will run**, and this should only be used in emergencies.
     *
     * See the [Exceptions.EmergencyStop] for terminating the OpMode through an exception
     * that does not silently terminate the OpMode.
     */
    fun emergencyStop() {
        Dbg.logd("BunyipsOpMode: emergency stop requested.")
        terminateOpModeNow()
    }
}
// he will never know