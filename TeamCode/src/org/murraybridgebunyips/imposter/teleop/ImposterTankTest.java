package org.murraybridgebunyips.imposter.teleop;

import com.acmerobotics.roadrunner.control.PIDCoefficients;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.murraybridgebunyips.bunyipslib.BunyipsOpMode;
import org.murraybridgebunyips.bunyipslib.drive.TankDrive;
import org.murraybridgebunyips.bunyipslib.roadrunner.drive.RoadRunnerDrive;
import org.murraybridgebunyips.bunyipslib.roadrunner.drive.TankCoefficients;
import org.murraybridgebunyips.imposter.components.ImposterConfig;

import java.util.Arrays;

@TeleOp
public class ImposterTankTest extends BunyipsOpMode {
    private final ImposterConfig config = new ImposterConfig();

    private RoadRunnerDrive drive;

    @Override
    protected void onInit() {
        config.init();
        TankCoefficients t = new TankCoefficients.Builder()
                .setAxialPID(new PIDCoefficients(2, 0, 0))
                .setCrossTrackPID(new PIDCoefficients(2, 0, 0))
                .setHeadingPID(new PIDCoefficients(2, 0, 0))
                .build();
//        drive = new MecanumDrive(config.driveConstants, config.mecanumCoefficients, config.imu, config.front_left_motor, config.front_right_motor, config.back_left_motor, config.back_right_motor);
        drive = new TankDrive(config.driveConstants, t, config.imu, Arrays.asList(config.front_left_motor, config.back_left_motor), Arrays.asList(config.back_right_motor, config.front_right_motor));
        drive.followTrajectorySequenceAsync(drive.trajectorySequenceBuilder(new Pose2d()).forward(32).turn(Math.PI / 2).build());
    }

    @Override
    protected void activeLoop() {
        drive.update();
//        drive.setSpeedUsingController(gamepad1.lsx, gamepad1.lsy, gamepad1.rsx);
//        drive.update();
    }
}
