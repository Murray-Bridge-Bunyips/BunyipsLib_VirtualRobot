package au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.groups;

import static au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Units.Seconds;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.AutonomousBunyipsOpMode;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.BunyipsSubsystem;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.Dbg;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.EmergencyStop;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.Exceptions;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.Scheduler;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.Mathf;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Measure;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Time;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.bases.Task;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Text;

/**
 * A group of tasks.
 * Users must be careful to ensure they do not allocate tasks that use the same subsystems when
 * running in parallel (all task groups except SequentialTaskGroup), otherwise hardware will
 * try to take commands from multiple tasks at once, overriding each other and causing unpredictable behaviour.
 *
 * @author Lucas Bubner, 2024
 * @since 1.0.0-pre
 */
public abstract class TaskGroup extends Task {
    protected final ArrayList<Task> tasks = new ArrayList<>();
    private final HashSet<Task> attachedTasks = new HashSet<>();

    protected TaskGroup(@NonNull Measure<Time> maxTimeout, @NonNull Task... tasks) {
        super(maxTimeout);
        this.tasks.addAll(Arrays.asList(tasks));
        if (tasks.length == 0) {
            throw new EmergencyStop(getClass().getSimpleName() + " created with no tasks.");
        }
        StringBuilder taskNames = new StringBuilder();
        taskNames.append(getClass().getSimpleName().replace("TaskGroup", ""));
        taskNames.append(": ");
        for (int i = 0; i < tasks.length - 1; i++) {
            taskNames.append(tasks[i]).append(", ");
        }
        taskNames.append(tasks[tasks.length - 1]);
        withName(taskNames.toString());
    }

    /**
     * @return all tasks in this group
     */
    @NonNull
    public List<Task> getGroupedTasks() {
        return new ArrayList<>(tasks);
    }

    /**
     * Log the creation of this task group in the OpMode telemetry.
     * Called internally by {@link AutonomousBunyipsOpMode}.
     */
    public void logCreation() {
        String groupName = toString();
        String taskGroup = getClass().getSimpleName();
        // Avoid printing the group name if it is the same as the task group name
        if (!groupName.equals(taskGroup)) {
            opMode(o -> o.telemetry.log("<font color='gray'>%:</font> % created with % tasks.", groupName, taskGroup, tasks.size()));
        } else {
            opMode(o -> o.telemetry.log("<font color='gray'>%:</font> Created with % tasks.", taskGroup, tasks.size()));
        }
        // List subtasks
        for (Task task : tasks) {
            opMode(o -> o.telemetry.log("&nbsp;&nbsp;-> <font color='gray'>%<i>(t=%)</i></font>", task.toString(), task.getTimeout().magnitude() != 0.0 ? Mathf.round(task.getTimeout().in(Seconds), 1) + "s" : "∞"));
        }
    }

    protected final void executeTask(@NonNull Task task) {
        if (task.isFinished()) return;
        // Do not manage a task if it is already attached to a subsystem being managed there
        if (attachedTasks.contains(task)) return;
        task.getDependency().ifPresent(dependency -> {
            dependency.setCurrentTask(task);
            attachedTasks.add(task);
        });
        // Otherwise we can just run the task outright
        if (!task.hasDependency()) {
            Scheduler.addTaskReport(toString(), false, task.toString(), Mathf.round(task.getDeltaTime().in(Seconds), 1), task.getTimeout().in(Seconds));
            task.run();
        }
    }

    protected final void finishAllTasks() {
        for (Task task : tasks) {
            if (!task.isFinished())
                task.getDependency().ifPresent(BunyipsSubsystem::cancelCurrentTask);
            task.finishNow();
        }
    }

    @Override
    protected final void init() {
        // no-op
    }

    @Override
    protected final void onFinish() {
        finishAllTasks();
    }

    @NonNull
    @Override
    public final Task onSubsystem(@NonNull BunyipsSubsystem subsystem, boolean override) {
        StackTraceElement f = Exceptions.getCallingUserCodeFunction();
        Dbg.error(f, "Task groups are not designed to be attached to a subsystem, as the internal tasks will be scheduled to subsystems instead.");
        opMode(o -> o.telemetry.log(f, Text.html().color("red", "error: ").text("task groups should not be attached to subsystems!")));
        return this;
    }

    @Override
    protected void onReset() {
        for (Task task : tasks) {
            task.reset();
        }
        attachedTasks.clear();
    }
}
