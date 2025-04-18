package au.edu.sa.mbhs.studentrobotics.bunyipslib.subsystems;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.robotcore.hardware.Servo;

import java.util.function.Supplier;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.BunyipsSubsystem;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.DualTelemetry;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.Mathf;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Measure;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Time;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.hardware.ServoEx;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.bases.Lambda;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.bases.Task;

/**
 * Class for a generic servo switch that may switch between two known states.
 * A whole new file is technically unnecessary, but we wanted to make a class named Cannon.
 * <p>
 * "Fire in the hole!"
 * <p>
 * This class is deprecated by {@link Switch}, which offers more functionality relating to single-servo subsystems,
 * allowing movement beyond just two positions.
 *
 * @author Lachlan Paul, 2023
 * @author Lucas Bubner, 2023
 * @since 1.0.0-pre
 */
public class Cannon extends BunyipsSubsystem {
    /**
     * Tasks for Cannon.
     */
    public final Tasks tasks = new Tasks();
    private final double FIRED;
    private final double RESET;
    private Servo prolong;
    private double target;

    /**
     * Constructs a new Cannon.
     *
     * @param prolong       the servo to use
     * @param closePosition the position to set the servo to when not firing
     * @param openPosition  the position to set the servo to when firing
     */
    public Cannon(@Nullable Servo prolong, double openPosition, double closePosition) {
        if (openPosition == closePosition)
            throw new IllegalArgumentException("Open and close positions cannot be the same");

        FIRED = Mathf.clamp(openPosition, 0, 1);
        RESET = Mathf.clamp(closePosition, 0, 1);

        if (!assertParamsNotNull(prolong)) return;
        this.prolong = prolong;

        // We assume there will always be something in the reset position for us to hold
        target = RESET;
    }

    /**
     * Constructs a new Cannon.
     * Implicitly set the close position to 0.0 and the open position to 1.0.
     *
     * @param prolong the servo to use
     */
    public Cannon(@Nullable Servo prolong) {
        this(prolong, 1.0, 0.0);
    }

    /**
     * Fire in the hole!
     *
     * @return this
     */
    @NonNull
    public Cannon fire() {
        target = FIRED;
        return this;
    }

    /**
     * Reset the cannon to its initial position
     *
     * @return this
     */
    @NonNull
    public Cannon reset() {
        target = RESET;
        return this;
    }

    /**
     * Query if the cannon is fired.
     *
     * @return true if the cannon is fired
     */
    public boolean isFired() {
        return target == FIRED;
    }

    /**
     * Query if the cannon is reset.
     *
     * @return true if the cannon is reset
     */
    public boolean isReset() {
        return target == RESET;
    }

    @Override
    protected void periodic() {
        DualTelemetry.smartAdd(toString(), target == FIRED ? "<font color='red'><b>FIRED</b></font>" : "<font color='green'>READY</font>");
        prolong.setPosition(target);
    }

    /**
     * Tasks for Cannon, access through {@link #tasks}.
     */
    public class Tasks {
        /**
         * Fire the cannon.
         *
         * @return Fire cannon task
         */
        @NonNull
        public Task fire() {
            Supplier<Measure<Time>> taskTimeout = () -> ServoEx.tryGetEndToEndTime(prolong, target, FIRED);
            return new Lambda((t) -> {
                t.timeout = taskTimeout.get();
                Cannon.this.fire();
            }).on(Cannon.this, true)
                    .timeout(taskTimeout.get()) // preliminary
                    .named(forThisSubsystem("Fire"));
        }

        /**
         * Reset the cannon.
         *
         * @return Reset cannon task
         */
        @NonNull
        public Task reset() {
            Supplier<Measure<Time>> taskTimeout = () -> ServoEx.tryGetEndToEndTime(prolong, target, RESET);
            return new Lambda((t) -> {
                t.timeout = taskTimeout.get();
                Cannon.this.reset();
            }).on(Cannon.this, true)
                    .timeout(taskTimeout.get()) // preliminary
                    .named(forThisSubsystem("Reset"));
        }
    }
}
