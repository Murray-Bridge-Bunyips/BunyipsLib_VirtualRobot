package au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.bases;

/**
 * A task that runs forever to no finish condition, only able to be interrupted by being finished manually.
 * This is the general class to implement default tasks in.
 *
 * @since 1.0.0-pre
 */
public abstract class ForeverTask extends Task {
    @Override
    protected final boolean isTaskFinished() {
        // Will never finish automatically
        return false;
    }
}
