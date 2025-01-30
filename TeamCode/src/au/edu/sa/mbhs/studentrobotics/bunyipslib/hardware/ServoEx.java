package au.edu.sa.mbhs.studentrobotics.bunyipslib.hardware;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.control.TrapezoidProfile;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Measure;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Time;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.subsystems.DualServos;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.subsystems.Switch;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.bases.Lambda;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoController;

import static au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Units.Milliseconds;
import static au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Units.Nanoseconds;
import static au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Units.Seconds;

/**
 * Extension of the extended {@link Servo} interface that allows for motion profiling via a {@link TrapezoidProfile}.
 * This extension also offers refresh rate and position cache tolerance handling for loop time
 * optimisation similar to an {@link SimpleRotator}.
 * <p>
 * This class serves as a drop-in replacement for the {@link Servo}, similar to {@link Motor} with the {@link DcMotor}.
 * The virtual configuration of this class extends the Servo interface as advanced PWM controls are not possible.
 *
 * @author Lucas Bubner, 2024
 * @since 5.1.0
 */
public class ServoEx implements Servo {
    private final Servo __VIRTUAL_SERVO;

    @Nullable
    private TrapezoidProfile.Constraints constraints;
    private TrapezoidProfile.State setpoint = new TrapezoidProfile.State();
    private double lastDtSec = -1;

    private double positionDeltaTolerance;
    private double lastPosition = -1;
    private long refreshRateNanos;
    private long lastUpdate;

    /**
     * Attempts to get the end-to-end time for this servo (only available on ServoEx).
     *
     * @param servo the servo to get the time for (rescaling parameters are ignored if this is not a ServoEx)
     * @param from  the rescaling lower limit where the servo is currently
     * @param to    the rescaling upper limit where the servo should go to
     * @return the rescaled servo end-to-end time if available, else {@link Lambda#EPSILON_MS}.
     */
    public static Measure<Time> tryGetEndToEndTime(Servo servo, double from, double to) {
        return servo instanceof ServoEx sex ? sex.endToEndTime.times(to - from) : Milliseconds.of(Lambda.EPSILON_MS);
    }

    /**
     * Set the delta in servo position required to propagate a hardware write.
     *
     * @param magnitude absolute magnitude of delta in servo position, 0/default will disable
     */
    public void setPositionDeltaThreshold(double magnitude) {
        positionDeltaTolerance = Math.abs(magnitude);
    }

    /**
     * Set the refresh rate of the servo that will be a minimum time between hardware writes.
     * Consistent calls to {@link #setPosition(double)} is required for this refresh rate to be effective.
     *
     * @param refreshRate the refresh rate interval, <=0/default will disable
     */
    public void setPositionRefreshRate(Measure<Time> refreshRate) {
        refreshRateNanos = (long) refreshRate.in(Nanoseconds);
    }

    /**
     * The user-defined time it takes for this servo to travel from position 0 to 1 or 1 to 0.
     * Default of 1 second.
     *
     * @return end to end time
     */
    @NonNull
    public Measure<Time> getEndToEndTime() {
        return endToEndTime;
    }

    /**
     * Set the time at which it takes the servo to complete a full rotation from the programmed 0 to 1 position (and
     * vice versa). This value is used to define the task timing for several subsystems, including the {@link Switch}
     * and {@link DualServos}.
     * <p>
     * This value can also be used for your own purposes accessed through {@link #getEndToEndTime()}.
     *
     * @param servoFullRotationTime the time it takes for the servo to travel from programmed position 0 to 1 or 1 to 0.
     */
    public void setEndToEndTime(@NonNull Measure<Time> servoFullRotationTime) {
        endToEndTime = servoFullRotationTime;
    }

    /**
     * Sets the trapezoidal constraints to apply to this servo's positions. These constraints are in units
     * of delta step of position (for example, 1 corresponds with the full servo travel distance per second).
     *
     * @param positionConstraints the position velocity and acceleration constraints
     */
    public void setConstraints(@Nullable TrapezoidProfile.Constraints positionConstraints) {
        constraints = positionConstraints;
    }

    /**
     * Return to standard servo controls without a motion profile. This is shorthand for {@code setConstraints(null)}.
     */
    public void disableConstraints() {
        constraints = null;
    }

    private Measure<Time> endToEndTime = Seconds.one();

    /**
     * Wrap a Servo to use with the ServoEx class.
     *
     * @param servo the Servo from hardwareMap to use.
     */
    public ServoEx(Servo servo) {
        __VIRTUAL_SERVO = servo;
    }

    @Override
    public void setDirection(Direction direction) {
        __VIRTUAL_SERVO.setDirection(direction);
    }

    @Override
    public Direction getDirection() {
        return __VIRTUAL_SERVO.getDirection();
    }

    /**
     * Sets the current position of the servo, expressed as a fraction of its available
     * range. If PWM power is enabled for the servo, the servo will attempt to move to
     * the indicated position.
     * <p>
     * <b>Important ServoEx Note:</b> Since this class requires continuous update with a motion profile,
     * it is important that this method is being called periodically if one is being used; this is similar
     * to how {@code setPower} has to be called periodically in {@link Motor} to update system controllers.
     *
     * @param targetPosition the position to which the servo should move, a value in the range [0.0, 1.0]
     * @see ServoController#pwmEnable()
     * @see #getPosition()
     */
    @Override
    public synchronized void setPosition(double targetPosition) {
        if (constraints != null) {
            // Apply motion profiling for current target in seconds
            TrapezoidProfile.State goal = new TrapezoidProfile.State(targetPosition, 0);
            TrapezoidProfile profile = new TrapezoidProfile(constraints, goal, setpoint);
            double t = System.nanoTime() / 1.0E9;
            if (lastDtSec == -1) lastDtSec = t;
            setpoint = profile.calculate(t - lastDtSec);
            lastDtSec = t;
            targetPosition = setpoint.position;
        }

        // Apply refresh rate and cache restrictions
        long now = System.nanoTime();
        if (refreshRateNanos > 0 && Math.abs(lastUpdate - now) < refreshRateNanos) {
            return;
        }
        if (Math.abs(lastPosition - targetPosition) < positionDeltaTolerance && targetPosition != 1 && targetPosition != 0) {
            return;
        }
        if (targetPosition == lastPosition) {
            // Useless operation
            return;
        }

        lastUpdate = now;
        lastPosition = targetPosition;
        __VIRTUAL_SERVO.setPosition(targetPosition);
    }

    @Override
    public double getPosition() {
        return __VIRTUAL_SERVO.getPosition();
    }

    @Override
    public void scaleRange(double min, double max) {
        __VIRTUAL_SERVO.scaleRange(min, max);
    }
}
