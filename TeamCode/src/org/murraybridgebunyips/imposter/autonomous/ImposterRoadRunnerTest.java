package org.murraybridgebunyips.imposter.autonomous;

import androidx.annotation.NonNull;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import org.jetbrains.annotations.Nullable;
import org.murraybridgebunyips.bunyipslib.*;
import org.murraybridgebunyips.bunyipslib.drive.MecanumDrive;
import org.murraybridgebunyips.bunyipslib.roadrunner.trajectorysequence.TrajectorySequence;
import org.murraybridgebunyips.bunyipslib.tasks.RunTask;
import org.murraybridgebunyips.bunyipslib.tasks.WaitTask;
import org.murraybridgebunyips.bunyipslib.tasks.groups.ParallelTaskGroup;
import org.murraybridgebunyips.imposter.components.ImposterConfig;

import java.util.Arrays;

import static org.murraybridgebunyips.bunyipslib.external.units.Units.*;

@Autonomous
public class ImposterRoadRunnerTest extends AutonomousBunyipsOpMode implements RoadRunner {
    private final ImposterConfig config = new ImposterConfig();
    private MecanumDrive drive;

    @Override
    protected void onInitialise() {
        config.init();
        drive = new MecanumDrive(config.driveConstants, config.mecanumCoefficients, config.imu, config.front_left_motor, config.front_right_motor, config.back_left_motor, config.back_right_motor);
//        drive.setLocalizer(new IntrinsicMecanumLocalizer(new IntrinsicMecanumLocalizer.Coefficients.Builder().setMultiplier(365.76 / 11.0).build(), drive));
//        drive = new TriDeadwheelMecanumDrive(config.driveConstants, config.mecanumCoefficients, config.imu, config.front_left_motor, config.front_right_motor, config.back_left_motor, config.back_right_motor, config.localizerCoefficients, config.enc_left, config.enc_right, config.enc_x);
//        setOpModes(StartingPositions.use());
        setOpModes(Arrays.stream(StartingPositions.values()).map(StartingPositions::toStartingConfiguration).toArray());
        setInitTask(new WaitTask(Seconds.of(1), false));
    }

    @Override
    protected void onReady(@Nullable Reference<?> selectedOpMode, Controls selectedButton) {
        if (selectedOpMode == null) {
//            makeTrajectory().forward(23.6*2).turn(-Math.PI / 2.0).forward(23.6*1.5).addTask();
            return;
        }
        Dbg.log(selectedButton);
        StartingPositions startingPosition = ((StartingConfiguration.Position) selectedOpMode.require()).toStartingPosition();
        setPose(startingPosition.getPose());
        Reference<TrajectorySequence> blueLeft = Reference.empty();
        Reference<TrajectorySequence> blueRight = Reference.empty();

        float FIELD_TILE_SCALE = 1.1f;
        TrajectorySequence redLeft = makeTrajectory()
                .forward(2 * FIELD_TILE_SCALE, FieldTiles)
                .strafeRight(2.8 * FIELD_TILE_SCALE, FieldTiles)
                .turn(-Math.PI / 2)
                .strafeRight(1 * FIELD_TILE_SCALE, FieldTile)
                .mirrorToRef(blueRight)
                .build();

        TrajectorySequence redRight = makeTrajectory()
                .lineToLinearHeading(new Pose2d(36, -36))
                .mirrorToRef(blueLeft)
                .build();

        TrajectorySequence targetTrajectory = null;
        switch (startingPosition) {
            case STARTING_RED_LEFT:
                targetTrajectory = redLeft;
                break;
            case STARTING_RED_RIGHT:
                targetTrajectory = redRight;
                break;
            case STARTING_BLUE_LEFT:
                targetTrajectory = blueLeft.require();
                break;
            case STARTING_BLUE_RIGHT:
                targetTrajectory = blueRight.require();
                break;
        }
        assert targetTrajectory != null;
        makeTrajectory()
                .runSequence(targetTrajectory)
                .withName("Navigate to Backboard")
                .addTask();

        addTask(new WaitTask(Seconds.of(2)).withName("Deploy Arm"));
        addTask(new RunTask().withName("Drop Pixels"));
        addTask(new WaitTask(Seconds.of(1)).withName("Wait for Pixels"));
        addTask(new ParallelTaskGroup(
                makeTrajectory()
                        .strafeLeft(0.95 * FIELD_TILE_SCALE, FieldTile)
                        .buildTask(),
                new WaitTask(Seconds.of(2))
        ).withName("Stow and Move to Park"));

        makeTrajectory()
                .forward(1.1 * FIELD_TILE_SCALE, FieldTiles)
                .withName("Finish Park")
                .addTask();
    }

    @Override
    protected void periodic() {
//        telemetry.add(config.back_left_motor.getPower());
//        telemetry.add(config.back_right_motor.getPower());
//        telemetry.add(config.front_left_motor.getPower());
//        telemetry.add(config.front_right_motor.getPower());
    }

    @NonNull
    @Override
    public MecanumDrive getDrive() {
        return drive;
    }
}
