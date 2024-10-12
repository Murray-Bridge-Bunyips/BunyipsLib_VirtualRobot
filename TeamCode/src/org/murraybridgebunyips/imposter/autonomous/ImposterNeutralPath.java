package org.murraybridgebunyips.imposter.autonomous;

import androidx.annotation.NonNull;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import org.jetbrains.annotations.Nullable;
import org.murraybridgebunyips.bunyipslib.*;
import org.murraybridgebunyips.bunyipslib.drive.MecanumDrive;
import org.murraybridgebunyips.bunyipslib.roadrunner.drive.RoadRunnerDrive;
import org.murraybridgebunyips.bunyipslib.roadrunner.trajectorysequence.TrajectorySequence;
import org.murraybridgebunyips.imposter.components.ImposterConfig;

import static org.murraybridgebunyips.bunyipslib.StartingConfiguration.blueLeft;
import static org.murraybridgebunyips.bunyipslib.StartingConfiguration.redLeft;
import static org.murraybridgebunyips.bunyipslib.external.units.Units.*;


@Autonomous
public class ImposterNeutralPath extends AutonomousBunyipsOpMode implements RoadRunner {
    private final ImposterConfig robot = new ImposterConfig();

    private MecanumDrive drive;

    @Override
    protected void onInitialise() {
        robot.init();
        drive = new MecanumDrive(
                robot.driveConstants, robot.mecanumCoefficients, robot.imu, robot.front_left_motor, robot.front_right_motor, robot.back_left_motor,
                robot.back_right_motor
        );

        setOpModes(
                redLeft().tile(2).backward(Centimeters.of(7)).rotate(Degrees.of(90)),
                blueLeft().tile(2).backward(Centimeters.of(7)).rotate(Degrees.of(90))
        );
    }

    @Override
    protected void onReady(@Nullable Reference<?> selectedOpMode, Controls selectedButton) {
        if (selectedOpMode == null) return;
        StartingConfiguration.Position startingPosition = (StartingConfiguration.Position) selectedOpMode.require();

        setPose(startingPosition.toFieldPose());
        Reference<TrajectorySequence> blue = Reference.empty();
        TrajectorySequence red = makeTrajectory()
                .setRefMirroring(MirrorMap.SYMMETRIC_MIRROR)
                .mirrorToRef(blue)
                .lineToSplineHeading(new Pose2d(-31.14, -26.09, 180.00), Inches, Degrees)
                .lineToSplineHeading(new Pose2d(-44.91, -13.08, 250.00), Inches, Degrees)
                .splineToLinearHeading(new Pose2d(-56.24, -51.49, 233.97), Inches, Degrees, 233, Degrees)
                .setReversed(true)
                .splineTo(new Vector2d(-44.91, -13.08), Inches, 250 - 180, Degrees)
                .setReversed(false)
                .lineToLinearHeading(new Pose2d(-56.39, -9.41, 260.22), Inches, Degrees)
                .splineTo(new Vector2d(-59.60, -53.02), Inches, 248.37, Degrees)
                .setReversed(true)
                .splineTo(new Vector2d(-56.39, -9.41), Inches, 260.22 - 180, Degrees)
                .setReversed(false)
                .lineToLinearHeading(new Pose2d(-63.12, -7.57, 270.00), Inches, Degrees)
                .splineTo(new Vector2d(-62.51, -55.47), Inches, 270.00, Degrees)
                .lineTo(new Vector2d(-24.56, -11.25), Inches)
                .build();

        addTask(makeTrajectory().runSequence(startingPosition.isRed() ? red : blue.get()).buildTask());
    }

    @NonNull
    @Override
    public RoadRunnerDrive getDrive() {
        return drive;
    }
}
