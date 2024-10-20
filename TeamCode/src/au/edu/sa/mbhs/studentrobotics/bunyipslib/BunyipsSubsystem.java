package au.edu.sa.mbhs.studentrobotics.bunyipslib;

import static au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Units.Seconds;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.Mathf;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Measure;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Time;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.IdleTask;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.bases.Task;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.NullSafety;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Text;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Threads;

/**
 * Base class for all robot subsystems.
 * Integrates with the Task system to allow for task-based command scheduling, where a subsystem is able to hook
 * a {@link Task} to execute it until completion.
 *
 * @author Lucas Bubner, 2024
 * @see Scheduler
 * @since 1.0.0-pre
 */
public abstract class BunyipsSubsystem extends BunyipsComponent {
    private static final HashSet<BunyipsSubsystem> instances = new HashSet<>();
    private final List<BunyipsSubsystem> children = new ArrayList<>();
    /**
     * Reference to the unmodified name of this subsystem.
     *
     * @see #toString() referencing `this` to also retrieve delegation status affixed to the end of the name
     */
    protected String name = getClass().getSimpleName();
    private volatile Task currentTask;
    private volatile Task defaultTask = new IdleTask();
    private volatile boolean shouldRun = true;
    @Nullable
    private String threadName = null;
    private BunyipsSubsystem parent = null;
    private boolean assertionFailed = false;

    protected BunyipsSubsystem() {
        instances.add(this);
    }

    // Package-private, should only be accessed by a BunyipsLib operation (such as in BunyipsOpMode)
    @SuppressWarnings("unchecked")
    static HashSet<BunyipsSubsystem> getInstances() {
        // Shallow clone as the instances should be the same
        return ((HashSet<BunyipsSubsystem>) instances.clone())
                .stream()
                // Filter by subsystems that have no parent
                .filter(subsystem -> subsystem.parent == null)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }

    /**
     * Reset stored static instances of BunyipsSubsystem.
     */
    public static void resetForOpMode() {
        instances.clear();
    }

    /**
     * Update all instances of BunyipsSubsystem that has been constructed since the last clearing.
     * This is useful to call if you simply wish to update every single subsystem at the dispatch phase of your
     * hardware loop.
     */
    public static void updateAll() {
        for (BunyipsSubsystem subsystem : instances) {
            // Only update subsystems that aren't being delegated (no parent)
            if (subsystem.parent == null)
                subsystem.update();
        }
    }

    /**
     * @return a status string of this subsystem
     */
    @NonNull
    public final String toVerboseString() {
        return Text.format("%%%% (%) <=> %", assertionFailed ? "[error]" : "", threadName != null ? " [async]" : "", parent != null ? " [delegated to " + parent + "]" : "", " " + name, shouldRun ? "enabled" : "disabled", getCurrentTask());
    }

    /**
     * @return the name of the subsystem with delegation information appended if available
     * @see #withName(String)
     */
    @Override
    @NonNull
    public final String toString() {
        return name + (parent != null ? " (D. " + parent.name + ")" : "");
    }

    /**
     * Set the name of this subsystem that will be used in telemetry and references.
     *
     * @param subsystemName the name to set
     * @param <T>           subsystem type
     * @return this
     */
    @SuppressWarnings("unchecked")
    public final synchronized <T extends BunyipsSubsystem> T withName(@NonNull String subsystemName) {
        name = subsystemName;
        return (T) this;
    }

    /**
     * @return whether the name of the subsystem has not been modified (customisable name is the same as the class name)
     */
    public final boolean isDefaultName() {
        return name.equals(getClass().getSimpleName());
    }

    /**
     * @return whether this subsystem has been commanded to disable and should not execute any stateful updates
     */
    public final boolean isDisabled() {
        return !shouldRun;
    }

    /**
     * Utility function to run {@code NullSafety.assertComponentArgs()} on the given parameters, usually on
     * the motors/hardware/critical objects passed into the constructor. If this check fails, your subsystem
     * will automatically disable the {@code update()} method from calling to prevent exceptions, no-oping
     * the subsystem. A 'SUBSYSTEM FAULT' will be added to telemetry, and exceptions from this class will be muted.
     *
     * @param parameters constructor parameters for your subsystem that should be checked for null,
     *                   in which case the subsystem should be disabled
     * @return whether the assertion passed or failed, where you can stop the constructor if this returns false
     */
    protected final boolean assertParamsNotNull(Object... parameters) {
        // If a previous check has already failed, we don't need to check again otherwise we might
        // erase a previous check that failed
        if (!shouldRun) return false;
        // assertComponentArgs will manage telemetry/impl of errors being ignored, all we need to do
        // is check if it failed and if so, disable the subsystem
        shouldRun = NullSafety.assertComponentArgs(toString(), getClass().getSimpleName(), parameters);
        if (!shouldRun) {
            assertionFailed = true;
            Dbg.error(getClass(), "%Subsystem has been disabled as assertParamsNotNull() failed.", isDefaultName() ? "" : "(" + this + ") ");
            onDisable();
        }
        return shouldRun;
    }


    /**
     * Call to delegate the update of this subsystem, usually a component of another subsystem, to this subsystem.
     * This is useful in applications where a subsystem is being used as a component of another subsystem, and the
     * parent subsystem wishes to update the child subsystem.
     * <p>
     * Do note that the child subsystem will be updated after the main subsystem update dispatch, and do be aware
     * that performing any operations such as disabling/enabling will be done on the child subsystem. The only
     * operation that is not delegated is the current task, which is managed by the parent subsystem manually.
     *
     * @param child the subsystem to add to the list of children of this subsystem
     */
    protected final void delegate(BunyipsSubsystem child) {
        child.parent = this;
        children.add(child);
    }

    /**
     * Prevent a subsystem from running.
     */
    public final void disable() {
        if (!shouldRun) return;
        shouldRun = false;
        Dbg.logv(getClass(), "%Subsystem disabled via disable() call.", isDefaultName() ? "" : "(" + this + ") ");
        opMode(o -> o.telemetry.log(getClass(), Text.html().color("yellow", "disabled. ").small("check logcat for more info.")));
        onDisable();
        for (BunyipsSubsystem child : children)
            child.disable();
    }

    /**
     * Re-enable a subsystem if it was previously disabled via a disable() call.
     * This method will no-op if the assertion from {@link #assertParamsNotNull(Object...)} failed.
     */
    public final void enable() {
        if (shouldRun || assertionFailed) return;
        shouldRun = true;
        Dbg.logv(getClass(), "%Subsystem enabled via enable() call.", isDefaultName() ? "" : "(" + this + ") ");
        opMode(o -> o.telemetry.log(getClass(), Text.html().color("green", "enabled. ").small("check logcat for more info.")));
        onEnable();
        for (BunyipsSubsystem child : children)
            child.enable();
    }

    /**
     * Cancel the current task immediately and return to the default task, if available.
     */
    public final void cancelCurrentTask() {
        if (shouldRun && currentTask != defaultTask) {
            Dbg.logv(getClass(), "%Task changed: %<-%(INT)", isDefaultName() ? "" : "(" + this + ") ", defaultTask, currentTask);
            currentTask.finishNow();
            currentTask = defaultTask;
        }
    }

    /**
     * Get the current task for this subsystem.
     * If the current task is null or finished, the default task will be returned.
     *
     * @return The current task, null if the subsystem is disabled
     */
    @Nullable
    public final Task getCurrentTask() {
        if (!shouldRun) return null;
        if (currentTask == null || currentTask.isFinished()) {
            if (currentTask == null) {
                Dbg.logv(getClass(), "%Subsystem awake.", isDefaultName() ? "" : "(" + this + ") ");
                onEnable();
            } else {
                // Task changes are repetitive to telemetry log, will just leave the important messages to there
                Dbg.logv(getClass(), "%Task changed: %<-%", isDefaultName() ? "" : "(" + this + ") ", defaultTask, currentTask);
            }
            currentTask = defaultTask;
        }
        return currentTask;
    }

    /**
     * Set the default task for this subsystem, which will be run when no other task is running.
     *
     * @param defaultTask The task to set as the default task
     */
    public final void setDefaultTask(Task defaultTask) {
        if (defaultTask == null) return;
        defaultTask.onSubsystem(this, false);
        this.defaultTask = defaultTask;
    }

    /**
     * Determine if the subsystem is idle, meaning an IdleTask is running.
     */
    public final boolean isIdle() {
        Task current = getCurrentTask();
        return current == null || current.toString().equals("IdleTask");
    }

    /**
     * Set the current task to the given task.
     *
     * @param newTask The task to set as the current task
     * @return whether the task was successfully set or ignored
     */
    public final boolean setCurrentTask(Task newTask) {
        if (!shouldRun) {
            Dbg.warn(getClass(), "%Subsystem is disabled, ignoring task change.", isDefaultName() ? "" : "(" + this + ") ");
            return false;
        }
        if (newTask == null)
            return false;

        if (currentTask == null) {
            Dbg.warn(getClass(), "%Subsystem has not been updated with update() yet and a task was allocated - please ensure your subsystem is being updated if this behaviour is not intended.", isDefaultName() ? "" : "(" + this + ") ");
            currentTask = defaultTask;
        }

        if (currentTask == newTask)
            return true;

        // Lockout if a task is currently running that is not the default task
        if (currentTask != defaultTask) {
            // Override if the task is designed to override
            if (newTask.isOverriding()) {
                setHighPriorityCurrentTask(newTask);
                return true;
            }
            Dbg.log(getClass(), "%Ignored task change: %->%", isDefaultName() ? "" : "(" + this + ") ", currentTask, newTask);
            return false;
        }

        newTask.reset();
        // Default task technically can't finish, but it can be interrupted, so we will just run the finish callback
        if (currentTask == defaultTask) {
            defaultTask.finishNow();
            defaultTask.reset();
        }
        Dbg.logv(getClass(), "%Task changed: %->%", isDefaultName() ? "" : "(" + this + ") ", currentTask, newTask);
        currentTask = newTask;
        return true;
    }

    /**
     * Set the current task to the given task, overriding any current task.
     *
     * @param currentTask The task to set as the current task
     */
    public final void setHighPriorityCurrentTask(Task currentTask) {
        if (!shouldRun) {
            Dbg.warn(getClass(), "%Subsystem is disabled, ignoring high-priority task change.", isDefaultName() ? "" : "(" + this + ") ");
            return;
        }
        if (currentTask == null)
            return;
        // Task will be cancelled abruptly, run the finish callback now
        if (this.currentTask != defaultTask) {
            Dbg.warn(getClass(), "%Task changed: %(INT)->%", isDefaultName() ? "" : "(" + this + ") ", this.currentTask, currentTask);
            this.currentTask.finishNow();
        }
        currentTask.reset();
        // Default task technically can't finish, but it can be interrupted, so we will just run the finish callback
        if (this.currentTask == defaultTask) {
            defaultTask.finishNow();
            defaultTask.reset();
        }
        this.currentTask = currentTask;
    }

    /**
     * Update the subsystem and run the current task, if tasks are not set up this will just call {@link #periodic()}.
     * <p>
     * The update method is designed to be called to propagate new hardware state, with the design philosophy being that
     * any method or task may be executed on a subsystem, but hardware operations will <b>only</b> occur in the update method.
     * The only exception to this rule may apply to a {@code stop()} or {@code onDisable()} call, which may immediately
     * command the actuators to stop for safety purposes.
     * <p>
     * This method should be called if you are running this subsystem manually, otherwise it will be called by the {@link Scheduler}
     * or by {@link AutonomousBunyipsOpMode}.
     * Alternatively all subsystems that were instantiated can be statically updated via {@link #updateAll()}.
     */
    public final void update() {
        if (!shouldRun || threadName != null) return;
        internalUpdate();
    }

    private void internalUpdate() {
        Task task = getCurrentTask();
        if (task != null) {
            if (task == defaultTask && defaultTask.pollFinished()) {
                throw new EmergencyStop("Default task (of " + getClass().getSimpleName() + ") should never finish!");
            }
            // Run the task on our subsystem
            task.run();
            // Update the state of isFinished() after running the task as it may have changed
            task.pollFinished();
            if (!task.isMuted()) {
                Scheduler.addTaskReport(
                        toString(),
                        task == defaultTask,
                        task.toString(),
                        Mathf.round(task.getDeltaTime().in(Seconds), 1),
                        task.getTimeout().in(Seconds)
                );
            }
        }
        // This should be the only place where periodic() is called for this subsystem
        Exceptions.runUserMethod(this::periodic, opMode);
        // Update child subsystems if they are delegated, note we don't touch the parent subsystem at all
        for (BunyipsSubsystem child : children) {
            if (child != null && child.shouldRun)
                child.internalUpdate();
        }
    }

    /**
     * Call to delegate all updates of this subsystem to a thread that will begin execution on this method call.
     * <b>WARNING: You must ensure you know what you're doing before you multithread.</b>
     * <p>
     * Improper usage of threading subsystems will result in unexpected and potentially dangerous robot behaviour.
     * Ensure you know the consequences of multithreading, especially over hardware on a Robot Controller.
     * <p>
     * When this subsystem is being multithreaded, manual calls to {@link #update()} will be ignored.
     * Child subsystems cannot be threaded, only their parents can be.
     * <p>
     * The thread will run at full speed as-is.
     */
    public final void startThread() {
        startThread(Seconds.zero());
    }

    /**
     * Call to delegate all updates of this subsystem to a thread that will begin execution on this method call.
     * <b>WARNING: You must ensure you know what you're doing before you multithread.</b>
     * <p>
     * Improper usage of threading subsystems will result in unexpected and potentially dangerous robot behaviour.
     * Ensure you know the consequences of multithreading, especially over hardware on a Robot Controller.
     * <p>
     * When this subsystem is being multithreaded, manual calls to {@link #update()} will be ignored.
     * Child subsystems cannot be threaded, only their parents can be.
     *
     * @param loopSleepDuration the duration to sleep the external thread by after every iteration
     */
    public final void startThread(Measure<Time> loopSleepDuration) {
        if (threadName != null || parent != null) return;
        threadName = Text.format("Async-%-%-%", getClass().getSimpleName(), name, hashCode());
        Threads.startLoop(this::internalUpdate, threadName, loopSleepDuration);
    }

    /**
     * Call to stop delegating updates of this subsystem to a thread. This reverses {@link #startThread} and no-ops
     * if the subsystem update thread is not running.
     */
    public final void stopThread() {
        Threads.stop(threadName);
        threadName = null;
    }

    /**
     * To be updated periodically on every hardware loop.
     * This method should not be called manually, and should only be called from the context
     * of the {@link #update()} method.
     *
     * @see #update()
     */
    protected abstract void periodic();

    /**
     * User callback that runs once when this subsystem is enabled by a call to {@link #enable()}
     * or the first active call to {@link #periodic()}.
     */
    protected void onEnable() {
        // no-op
    }

    /**
     * User callback that runs once when this subsystem is disabled by a call to {@link #disable()}
     * or by an assertion failure.
     * Note that this method may run where asserted parameters are null, so be sure to check for
     * null safety in this method if necessary.
     */
    protected void onDisable() {
        // no-op
    }
}