package au.edu.sa.mbhs.studentrobotics.bunyipslib.subsystems;

import androidx.annotation.NonNull;

import com.qualcomm.robotcore.hardware.Servo;

import java.util.function.DoubleSupplier;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.BunyipsSubsystem;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.Mathf;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.ContinuousTask;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.RunTask;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.bases.Task;

/**
 * A generic servo controller subsystem that may be used to hold two positions and to control movements in between
 * these setpoints. This is similar to a {@link DualServos} subsystem but only controls a singular servo.
 *
 * @author Lucas Bubner, 2024
 * @since 5.1.0
 */
public class Switch extends BunyipsSubsystem {
    /**
     * Tasks for Switch.
     */
    public final Tasks tasks = new Tasks();

    private final Servo servo;
    private double target;
    private double openPosition = 1;
    private double closePosition = 0;

    /**
     * Constructs a new Switch.
     *
     * @param servo         the servo to use
     * @param openPosition  the position to set the servo to when open
     * @param closePosition the position to set the servo to when closed
     */
    public Switch(@NonNull Servo servo, double openPosition, double closePosition) {
        this.servo = servo;
        // Auto-close servo
        // Note: Updating must be done manually
        target = closePosition;
        withName("Switch");
        setBounds(openPosition, closePosition);
    }

    /**
     * Constructs a new Switch with default open and close positions.
     *
     * @param servo the servo to use
     */
    public Switch(@NonNull Servo servo) {
        this(servo, 1, 0);
    }

    /**
     * Set the new bounds this servo should respect for opened and closed positions.
     *
     * @param openPosition  the open position to respect
     * @param closePosition the closed position to respect
     * @return this
     */
    @NonNull
    public Switch setBounds(double openPosition, double closePosition) {
        this.openPosition = Mathf.clamp(openPosition, 0, 1);
        this.closePosition = Mathf.clamp(closePosition, 0, 1);
        return this;
    }

    /**
     * Set the new open position.
     *
     * @param openPosition the open position to respect
     * @return this
     */
    @NonNull
    public Switch setOpenBound(double openPosition) {
        this.openPosition = Mathf.clamp(openPosition, 0, 1);
        return this;
    }

    /**
     * Set the new closed position.
     *
     * @param closePosition the closed position to respect
     * @return this
     */
    @NonNull
    public Switch setClosedBound(double closePosition) {
        this.closePosition = Mathf.clamp(closePosition, 0, 1);
        return this;
    }

    /**
     * Open the switch.
     *
     * @return this
     */
    @NonNull
    public Switch open() {
        target = openPosition;
        return this;
    }

    /**
     * Close the switch.
     *
     * @return this
     */
    @NonNull
    public Switch close() {
        target = closePosition;
        return this;
    }

    /**
     * Toggles the switch between the open and closed positions, moving to the closest position if the servo is
     * not in one of the two open or closed positions.
     *
     * @return this
     */
    @NonNull
    public Switch toggle() {
        double pos = servo.getPosition();
        if (pos == openPosition) {
            close();
            return this;
        }
        if (pos == closePosition) {
            open();
            return this;
        }

        // Snap to the closest open or closed position
        double distanceToOpen = Math.abs(pos - openPosition);
        double distanceToClose = Math.abs(pos - closePosition);
        if (distanceToOpen < distanceToClose) {
            open();
        } else {
            close();
        }
        return this;
    }

    /**
     * Set a custom position that is clipped between the closed and open bounds.
     *
     * @param position the position to set to, clipped between open and close
     */
    @NonNull
    public Switch setPositionClipped(double position) {
        target = Mathf.clamp(position, Math.min(openPosition, closePosition), Math.max(openPosition, closePosition));
        return this;
    }

    /**
     * @return the current servo target
     */
    public double getTarget() {
        return target;
    }

    /**
     * @return the current servo commanded position
     */
    public double getPosition() {
        return servo.getPosition();
    }

    /**
     * Set a custom position that is not affected by the closed and open bounds.
     *
     * @param position the raw position to send to the servo
     */
    @NonNull
    public Switch setPosition(double position) {
        target = Mathf.clamp(position, 0, 1);
        return this;
    }

    /**
     * Query whether the switch is open.
     *
     * @return whether the switch is open
     */
    public boolean isOpen() {
        return servo.getPosition() == openPosition;
    }

    /**
     * Query whether the switch is closed.
     *
     * @return whether the switch is closed
     */
    public boolean isClosed() {
        return servo.getPosition() == closePosition;
    }

    @Override
    protected void periodic() {
        opMode(o -> o.telemetry.add("%: %", this, target == openPosition
                ? "<font color='orange'><b>OPEN</b></font> (" + Mathf.round(openPosition, 1) + ")"
                : target == closePosition ? "<font color='green'>CLOSE</font> (" + Mathf.round(closePosition, 1) + ")"
                : "<font color='white'>" + Mathf.round(target, 2) + "/1.00</font>"));
        servo.setPosition(target);
    }

    /**
     * Tasks for Switch, access with {@link #tasks}.
     */
    public class Tasks {
        /**
         * Move the servo with a supplier of position. Should be a default task.
         *
         * @param positionSupplier the position value supplier
         * @return a task to continually move the switch to the position supply
         */
        @NonNull
        public Task controlPosition(@NonNull DoubleSupplier positionSupplier) {
            return new ContinuousTask(() -> setPosition(positionSupplier.getAsDouble()))
                    .onSubsystem(Switch.this, false)
                    .withName("Supplier Position Control");
        }

        /**
         * Move the servo with a supplier of power/delta. Should be a default task.
         *
         * @param powerSupplier the power/delta supplier
         * @return a task to continually move the switch in accordance with the delta step
         */
        @NonNull
        public Task controlDelta(@NonNull DoubleSupplier powerSupplier) {
            return new ContinuousTask(() -> setPosition(servo.getPosition() + powerSupplier.getAsDouble()))
                    .onSubsystem(Switch.this, false)
                    .withName("Supplier Delta Control");
        }

        /**
         * Open the switch.
         *
         * @return Open switch task.
         */
        @NonNull
        public Task open() {
            return new RunTask(Switch.this::open)
                    .onSubsystem(Switch.this, true)
                    .withName("Open");
        }

        /**
         * Close the switch.
         *
         * @return Close switch task.
         */
        @NonNull
        public Task close() {
            return new RunTask(Switch.this::close)
                    .onSubsystem(Switch.this, true)
                    .withName("Close");
        }

        /**
         * Toggle the switch
         *
         * @return Toggle switch task
         */
        @NonNull
        public Task toggle() {
            return new RunTask(Switch.this::toggle)
                    .onSubsystem(Switch.this, true)
                    .withName("Toggle");
        }

        /**
         * Set a custom position that is clipped between the closed and open bounds.
         *
         * @param position the position to set to, clipped between open and close
         * @return a task to perform this action
         */
        @NonNull
        public Task setClipped(double position) {
            return new RunTask(() -> setPositionClipped(position))
                    .onSubsystem(Switch.this, true)
                    .withName("Go To " + Mathf.clamp(position, Math.min(closePosition, openPosition), Math.max(closePosition, openPosition)) + "/1.0");
        }

        /**
         * Set a custom position that is not affected by the closed and open bounds.
         *
         * @param position the raw position to send to the servo
         * @return a task to perform this action
         */
        @NonNull
        public Task setTo(double position) {
            return new RunTask(() -> setPosition(position))
                    .onSubsystem(Switch.this, true)
                    .withName("Go To " + Mathf.clamp(position, Math.min(closePosition, openPosition), Math.max(closePosition, openPosition)) + "/1.0");
        }

        /**
         * Delta the current servo position. This task is clamped between the closed and open positions.
         *
         * @param delta the amount to delta the servo by
         * @return a task to perform this action
         */
        @NonNull
        public Task deltaClipped(double delta) {
            return new RunTask(() -> setPositionClipped(servo.getPosition() + delta))
                    .onSubsystem(Switch.this, true)
                    .withName("Delta By " + delta);
        }

        /**
         * Delta the current servo position. This task is not affected by the closed and open bounds.
         *
         * @param delta the amount to delta the servo by, unclamped between the closed and open bounds
         * @return a task to perform this action
         */
        @NonNull
        public Task delta(double delta) {
            return new RunTask(() -> setPosition(servo.getPosition() + delta))
                    .onSubsystem(Switch.this, true)
                    .withName("Delta By " + delta);
        }
    }
}
