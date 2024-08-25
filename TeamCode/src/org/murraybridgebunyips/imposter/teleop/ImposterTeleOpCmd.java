package org.murraybridgebunyips.imposter.teleop;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import org.murraybridgebunyips.bunyipslib.Cartesian;
import org.murraybridgebunyips.bunyipslib.CommandBasedBunyipsOpMode;
import org.murraybridgebunyips.bunyipslib.Controls;
import org.murraybridgebunyips.bunyipslib.Motor;
import org.murraybridgebunyips.bunyipslib.drive.MecanumDrive;
import org.murraybridgebunyips.bunyipslib.drive.TriDeadwheelMecanumDrive;
import org.murraybridgebunyips.bunyipslib.external.pid.PIDController;
import org.murraybridgebunyips.bunyipslib.roadrunner.drive.localizers.IntrinsicMecanumLocalizer;
import org.murraybridgebunyips.bunyipslib.subsystems.Switch;
import org.murraybridgebunyips.bunyipslib.tasks.*;
import org.murraybridgebunyips.imposter.components.ImposterConfig;

import static org.murraybridgebunyips.bunyipslib.external.units.Units.*;

/** bunyipslib virtual testing ground */
@TeleOp
public class ImposterTeleOpCmd extends CommandBasedBunyipsOpMode {
    private final ImposterConfig config = new ImposterConfig();
    private MecanumDrive drive;

    @Override
    protected void onInitialise() {
        config.init();
//        setLoopSpeed(Seconds.of(0.199));
        drive = new TriDeadwheelMecanumDrive(config.driveConstants, config.mecanumCoefficients, config.imu, config.front_left_motor, config.front_right_motor, config.back_left_motor, config.back_right_motor, config.localizerCoefficients, config.enc_left, config.enc_right, config.enc_x);
        drive.setLocalizer(new IntrinsicMecanumLocalizer(new IntrinsicMecanumLocalizer.Coefficients.Builder().setMultiplier(365.76 / 11.0).build(), drive));
//        setInitTask(drive.useFallbackLocalizer().tasks.manualTestMainLocalizer());
//        drive.update();
    }

//    @Override
//    protected boolean onInitLoop() {
//        drive.setSpeedUsingController(gamepad1.lsx, gamepad1.lsy, gamepad1.rsx);
//        return false;
//    }

    //Motor motor;
    @Override
    protected void assignCommands() {
//        motor = new Motor(config.front_right_motor);
//        PIDController pid = new PIDController(0, 1, 2);
//        motor.setRunToPositionController(pid);
//        motor.scheduleRunToPositionGains()
//                .atPosition(0, 10, 0, 0)
//                .atPosition(100, 10, 2, 3)
//                .atPosition(400, 14, 1.0, 1.5)
//                .build();
//
//        motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
//        motor.setPower(1);
//        always().run(() -> telemetry.add("Motor Position: %", motor.getCurrentPosition()));
//        always().run(() -> telemetry.add("%, %, %", pid.getCoefficients()[0], pid.getCoefficients()[1], pid.getCoefficients()[2]));
        drive.setDefaultTask(new HolonomicDriveTask(gamepad1, drive, () -> false));

    }

    protected void periodic() {
//        drive.setSpeedUsingController(gamepad1.lsx, gamepad1.lsy, gamepad1.rsx);
//        motor.setTargetPosition((int) (motor.getTargetPosition() - (gamepad1.lsy)));
//        telemetry.add(motor.getTargetPosition());
//        motor.setPower(1);
//        telemetry.add(config.back_left_motor.getPower());
//        telemetry.add(config.back_right_motor.getPower());
//        telemetry.add(config.front_left_motor.getPower());
//        telemetry.add(config.front_right_motor.getPower());
    }
}