package org.murraybridgebunyips.bunyipslib;

import static org.murraybridgebunyips.bunyipslib.Text.formatString;
import static org.murraybridgebunyips.bunyipslib.Text.round;
import static org.murraybridgebunyips.bunyipslib.external.units.Units.Seconds;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.murraybridgebunyips.bunyipslib.tasks.IdleTask;
import org.murraybridgebunyips.bunyipslib.tasks.bases.Task;

/**
 * Base class for all robot subsystems.
 * Integrates with the Task system to allow for task-based command scheduling.
 *
 * @author Lucas Bubner, 2024
 * @see Scheduler
 */
public abstract class BunyipsSubsystem extends BunyipsComponent {
    private Task currentTask;
    private Task defaultTask = new IdleTask();
    private boolean shouldRun = true;
    private boolean assertionFailed = false;

    @Override
    @NonNull
    public String toString() {
        return formatString("%%(%) <=> %", assertionFailed ? "[error] " : "", getClass().getSimpleName(), shouldRun ? "enabled" : "disabled", getCurrentTask());
    }

    /**
     * Utility function to run NullSafety.assertComponentArgs() on the given parameters, usually on
     * the motors/hardware/critical objects passed into the constructor. If this check fails, your subsystem
     * will automatically disable the update() method from calling to prevent exceptions, no-oping
     * the subsystem. A COM_FAULT will be added to telemetry, and exceptions from this class will be muted.
     *
     * @param parameters constructor parameters for your subsystem that should be checked for null,
     *                   in which case the subsystem should be disabled
     * @return whether the assertion passed or failed, where you can stop the constructor if this returns false
     */
    protected boolean assertParamsNotNull(Object... parameters) {
        // If a previous check has already failed, we don't need to check again otherwise we might
        // erase a previous check that failed
        if (!shouldRun) return false;
        // assertComponentArgs will manage telemetry/impl of errors being ignored, all we need to do
        // is check if it failed and if so, disable the subsystem
        shouldRun = NullSafety.assertComponentArgs(getClass(), parameters);
        if (!shouldRun) {
            assertionFailed = true;
            Dbg.error(getClass(), "Subsystem has been disabled as assertParamsNotNull() failed.");
            onDisable();
        }
        return shouldRun;
    }

    /**
     * Prevent a subsystem from running.
     */
    public void disable() {
        if (!shouldRun) return;
        shouldRun = false;
        Dbg.logd(getClass(), "Subsystem disabled via disable() call.");
        onDisable();
    }

    /**
     * Re-enable a subsystem if it was previously disabled via a disable() call.
     * This method will no-op if the assertion from assertParamsNotNull() failed.
     */
    public void enable() {
        if (shouldRun || assertionFailed) return;
        shouldRun = true;
        Dbg.logd(getClass(), "Subsystem enabled via enable() call.");
        onEnable();
    }

    /**
     * Get the current task for this subsystem.
     * If the current task is null or finished, the default task will be returned.
     *
     * @return The current task, null if the subsystem is disabled
     */
    @Nullable
    public Task getCurrentTask() {
        if (!shouldRun) return null;
        if (currentTask == null || currentTask.isFinished()) {
            if (currentTask == null) {
                Dbg.logd(getClass(), "Subsystem awake.");
                onEnable();
            } else {
                Dbg.logd(getClass(), "Task changed: %<-%", defaultTask, currentTask);
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
            Dbg.warn(getClass(), "Subsystem is disabled, ignoring task change.");
            return false;
        }
        if (newTask == null)
            return false;

        if (currentTask == null) {
            Dbg.warn(getClass(), "Subsystem has not been updated with update() yet and a task was allocated - please ensure your subsystem is being updated via update() or by the addSubsystems() calls of the Scheduler or AutonomousBunyipsOpMode.");
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
            Dbg.log(getClass(), "Ignored task change: %->%", currentTask, newTask);
            return false;
        }

        newTask.reset();
        // Default task technically can't finish, but it can be interrupted, so we will just run the finish callback
        if (currentTask == defaultTask) {
            defaultTask.finishNow();
            defaultTask.reset();
        }
        Dbg.logd(getClass(), "Task changed: %->%", currentTask, newTask);
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
            Dbg.warn(getClass(), "Subsystem is disabled, ignoring high-priority task change.");
            return;
        }
        if (currentTask == null)
            return;
        // Task will be cancelled abruptly, run the finish callback now
        if (this.currentTask != defaultTask) {
            Dbg.warn(getClass(), "Task changed: %(INT)->%", this.currentTask, currentTask);
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
     * This method should be called if you are running this subsystem manually, otherwise it will be called by the {@link Scheduler}
     * or by {@link AutonomousBunyipsOpMode}.
     */
    public final void update() {
        if (!shouldRun) return;
        Task task = getCurrentTask();
        if (task != null) {
            if (task == defaultTask && defaultTask.pollFinished()) {
                throw new EmergencyStop("Default task (of " + getClass().getSimpleName() + ") should never finish!");
            }
            task.run();
            // Update the state of isFinished() after running the task as it may have changed
            task.pollFinished();
            if (!task.isMuted()) {
                Scheduler.addTaskReport(
                        getClass().getSimpleName(),
                        task == defaultTask,
                        task.toString(),
                        round(task.getDeltaTime().in(Seconds), 1),
                        task.getTimeout().in(Seconds)
                );
            }
        }
        // This should be the only place where periodic() is called for this subsystem
        periodic();
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
