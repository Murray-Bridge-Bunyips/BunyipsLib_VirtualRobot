package au.edu.sa.mbhs.studentrobotics.virtual.imposter.teleop;

import au.edu.sa.mbhs.studentrobotics.virtual.imposter.components.ImposterConfig;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.jetbrains.annotations.NotNull;
import org.murraybridgebunyips.bunyipslib.CommandBasedBunyipsOpMode;
import org.murraybridgebunyips.bunyipslib.Controls;
import org.murraybridgebunyips.bunyipslib.RoadRunner;
import org.murraybridgebunyips.bunyipslib.drive.MecanumDrive;
import org.murraybridgebunyips.bunyipslib.roadrunner.drive.RoadRunnerDrive;
import org.murraybridgebunyips.bunyipslib.tasks.DynamicTask;
import org.murraybridgebunyips.bunyipslib.tasks.HolonomicDriveTask;

import static org.murraybridgebunyips.bunyipslib.external.units.Units.Centimeters;
import static org.murraybridgebunyips.bunyipslib.external.units.Units.Inches;

@TeleOp
public class ImposterAugmented extends CommandBasedBunyipsOpMode implements RoadRunner {
    private final ImposterConfig config = new ImposterConfig();
    private MecanumDrive drive;

    @Override
    protected void onInitialise() {
        config.init();
//        drive = new TriDeadwheelMecanumDrive(config.driveConstants, config.mecanumCoefficients, hardwareMap.voltageSensor, config.imu, config.front_left_motor, config.front_right_motor, config.back_left_motor, config.back_right_motor, config.localizerCoefficients, config.enc_left, config.enc_right, config.enc_x);
        drive = new MecanumDrive(config.driveConstants, config.mecanumCoefficients, config.imu, config.front_left_motor, config.front_right_motor, config.back_left_motor, config.back_right_motor);
    }

    @Override
    protected void assignCommands() {
        drive.setDefaultTask(new HolonomicDriveTask(gamepad1, drive, () -> false));
        driver()
                .whenPressed(Controls.A)
                .run(new DynamicTask(() ->
                        makeTrajectory(drive.getPoseEstimate())
                                .lineToLinearHeading(new Pose2d(48 - Inches.convertFrom(5, Centimeters), -35.41, 0))
                                .buildTask(false))
                )
                .finishingIf(() -> !gamepad1.atRest());
    }

    @NotNull
    @Override
    public RoadRunnerDrive getDrive() {
        return drive;
    }
}
