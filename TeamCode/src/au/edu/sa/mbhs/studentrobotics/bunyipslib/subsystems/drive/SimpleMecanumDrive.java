package au.edu.sa.mbhs.studentrobotics.bunyipslib.subsystems.drive;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Time;
import com.acmerobotics.roadrunner.Twist2dDual;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.BunyipsSubsystem;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.DualTelemetry;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.Mathf;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.localization.Localizer;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.localization.accumulators.Accumulator;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Storage;

/**
 * A minimal Mecanum drive implementation that does not include a RoadRunner configuration.
 * This is useful for implementations where localizer information is not required at all times, such as in TeleOp,
 * and RoadRunner features are not required to be active.
 *
 * @author Lucas Bubner, 2024
 * @since 6.0.0
 */
public class SimpleMecanumDrive extends BunyipsSubsystem implements Moveable {
    private final DcMotor leftFront;
    private final DcMotor leftBack;
    private final DcMotor rightBack;
    private final DcMotor rightFront;
    /**
     * Forward speed.
     */
    public double speedX = 0;
    /**
     * Left speed.
     */
    public double speedY = 0;
    /**
     * Counter-clockwise rotational speed.
     */
    public double speedR = 0;
    @Nullable
    private Localizer localizer;
    private Accumulator accumulator = new Accumulator(Storage.memory().lastKnownPosition);
    private Priority priority = Priority.NORMALISED;

    /**
     * Construct a new SimpleMecanumDrive.
     *
     * @param leftFront  the front left motor
     * @param leftBack   the back left motor
     * @param rightBack  the back right motor
     * @param rightFront the front right motor
     */
    public SimpleMecanumDrive(@Nullable DcMotor leftFront, @Nullable DcMotor leftBack, @Nullable DcMotor rightBack, @Nullable DcMotor rightFront) {
        if (assertParamsNotNull(leftFront, leftBack, rightBack, rightFront)) {
            assert leftFront != null && leftBack != null && rightBack != null && rightFront != null;
            leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        }

        this.leftFront = leftFront;
        this.leftBack = leftBack;
        this.rightBack = rightBack;
        this.rightFront = rightFront;
    }

    @Override
    protected void periodic() {
        speedX = Mathf.clamp(speedX, -1, 1);
        speedY = Mathf.clamp(speedY, -1, 1);
        speedR = Mathf.clamp(speedR, -1, 1);

        // Convert from Robot to Cartesian form by rotating 90 degrees counter-clockwise
        double cX = -speedY;
        //noinspection SuspiciousNameCombination
        double cY = speedX;
        double cR = -speedR;

        if (localizer != null) {
            Twist2dDual<Time> twist = localizer.update();

            accumulator.accumulate(twist);

            Pose2d pose = accumulator.getPose();
            PoseVelocity2d poseVelRot = pose.heading.times(accumulator.getVelocity());
            Telemetry.Item i = DualTelemetry.smartAdd("Localizer", "X:%in(%/s) Y:%in(%/s) %°(%/s)",
                    Mathf.round(pose.position.x, 1),
                    Mathf.round(poseVelRot.linearVel.x, 1),
                    Mathf.round(pose.position.y, 1),
                    Mathf.round(poseVelRot.linearVel.y, 1),
                    Mathf.round(Math.toDegrees(pose.heading.toDouble()), 1),
                    Mathf.round(Math.toDegrees(poseVelRot.angVel), 1)
            );
            if (i instanceof DualTelemetry.HtmlItem hi)
                hi.color("gray").small();
        }

        double leftFrontPower, leftBackPower, rightBackPower, rightFrontPower;

        if (priority == Priority.ROTATIONAL) {
            // Raw powers for translation and rotation in configuration (front left, front right, back left, back right)
            double[] translationValues = {
                    cY + cX,
                    cY - cX,
                    cY - cX,
                    cY + cX
            };
            double[] rotationValues = {
                    cR,
                    -cR,
                    cR,
                    -cR
            };

            // Try to find the maximum power possible we can allocate to translation by scaling translation depending
            // on the desired rotation. This always ensures our rotational velocity will be achieved at the cost of
            // reduced translational velocity.
            double scaleFactor = 1.0;

            for (int i = 0; i < 4; i++) {
                double combinedPower = translationValues[i] + rotationValues[i];
                // Rescale translation to give more power to rotation if it exceeds motor limits
                if (Math.abs(combinedPower) > 1) {
                    double availablePower = 1 - Math.min(1, Math.abs(rotationValues[i]));
                    double requiredScale = availablePower / Math.abs(translationValues[i]);

                    // Update scaleFactor to the lowest required value to stay within motor limits
                    if (requiredScale < scaleFactor) {
                        scaleFactor = requiredScale;
                    }
                }
            }

            // Apply scaling
            leftFrontPower = translationValues[0] * scaleFactor + rotationValues[0];
            rightFrontPower = translationValues[1] * scaleFactor + rotationValues[1];
            leftBackPower = translationValues[2] * scaleFactor + rotationValues[2];
            rightBackPower = translationValues[3] * scaleFactor + rotationValues[3];
        } else {
            // Calculate motor powers
            leftFrontPower = cY + cX + cR;
            rightFrontPower = cY - cX - cR;
            leftBackPower = cY - cX + cR;
            rightBackPower = cY + cX - cR;

            double maxPower = Math.max(Math.abs(leftFrontPower), Math.max(Math.abs(rightFrontPower), Math.max(Math.abs(leftBackPower), Math.abs(rightBackPower))));
            // If the maximum number is greater than 1.0, then normalise by that number
            if (maxPower > 1.0) {
                leftFrontPower = leftFrontPower / maxPower;
                rightFrontPower = rightFrontPower / maxPower;
                leftBackPower = leftBackPower / maxPower;
                rightBackPower = rightBackPower / maxPower;
            }
        }

        leftFront.setPower(leftFrontPower);
        leftBack.setPower(leftBackPower);
        rightBack.setPower(rightBackPower);
        rightFront.setPower(rightFrontPower);

        DualTelemetry.smartAdd(toString(), "%\\% %, %\\% %, %\\% %",
                Math.round(Math.abs(speedX * 100)), speedX >= 0 ? "↑" : "↓",
                Math.round(Math.abs(speedY * 100)), speedY >= 0 ? "←" : "→",
                Math.round(Math.abs(speedR * 100)), speedR >= 0 ? "↺" : "↻"
        );
    }

    /**
     * @param priority the drive power priority
     * @return this
     */
    @NonNull
    public SimpleMecanumDrive setPriority(@NonNull Priority priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Swap the priority of the drive system.
     *
     * @return this
     */
    @NonNull
    public SimpleMecanumDrive swapPriority() {
        priority = priority == Priority.NORMALISED ? Priority.ROTATIONAL : Priority.NORMALISED;
        return this;
    }

    /**
     * Set the localizer for this drive.
     *
     * @param localizer the localizer to use
     * @return this
     */
    @NonNull
    public SimpleMecanumDrive withLocalizer(@NonNull Localizer localizer) {
        this.localizer = localizer;
        return this;
    }

    @Nullable
    public Localizer getLocalizer() {
        return localizer;
    }

    /**
     * Set the pose accumulator this drive instance should use.
     * If not defined, a default {@link Accumulator} will be used when a Localizer is attached.
     *
     * @param accumulator the  accumulator to use
     * @return this
     */
    @NonNull
    public SimpleMecanumDrive withAccumulator(@NonNull Accumulator accumulator) {
        this.accumulator.copyTo(accumulator);
        this.accumulator = accumulator;
        return this;
    }

    @Override
    protected void onDisable() {
        speedX = 0;
        speedY = 0;
        speedR = 0;
        leftFront.setPower(0);
        leftBack.setPower(0);
        rightBack.setPower(0);
        rightFront.setPower(0);
    }

    @Override
    public void setPower(@NonNull PoseVelocity2d target) {
        speedX = target.linearVel.x;
        speedY = target.linearVel.y;
        speedR = target.angVel;
    }

    @Nullable
    @Override
    public Pose2d getPose() {
        return localizer != null ? accumulator.getPose() : null;
    }

    @Override
    public void setPose(@NonNull Pose2d newPose) {
        accumulator.setPose(newPose);
    }

    @Nullable
    @Override
    public PoseVelocity2d getVelocity() {
        return localizer != null ? accumulator.getVelocity() : null;
    }

    /**
     * Mecanum control prioritisation.
     */
    public enum Priority {
        /**
         * Calculate translational speeds first.
         */
        NORMALISED,
        /**
         * Calculate rotational speeds first, and use the rest for translation.
         */
        ROTATIONAL
    }
}
