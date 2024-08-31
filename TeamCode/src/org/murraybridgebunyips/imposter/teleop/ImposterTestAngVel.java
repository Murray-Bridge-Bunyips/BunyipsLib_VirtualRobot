package org.murraybridgebunyips.imposter.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.murraybridgebunyips.bunyipslib.BunyipsOpMode;
import org.murraybridgebunyips.bunyipslib.Dbg;
import org.murraybridgebunyips.bunyipslib.EncoderTicks;
import org.murraybridgebunyips.bunyipslib.Motor;
import org.murraybridgebunyips.bunyipslib.drive.MecanumDrive;
import org.murraybridgebunyips.imposter.components.ImposterConfig;

import static org.murraybridgebunyips.bunyipslib.external.units.Units.*;

@TeleOp
public class ImposterTestAngVel extends BunyipsOpMode {
    private final ImposterConfig config = new ImposterConfig();
    private MecanumDrive d;

    @Override
    protected void onInit() {
        config.init();
        d = new MecanumDrive(
            config.driveConstants,
            config.mecanumCoefficients,
            config.imu,
            config.back_left_motor,
            config.back_right_motor,
            config.front_left_motor,
            config.front_right_motor
        );
    }

    @Override
    protected void activeLoop() {
        d.setSpeedUsingController(
            gamepad1.left_stick_x,
            gamepad1.left_stick_y,
            gamepad1.right_stick_x
        );
        d.update();
        telemetry.addData("Dist", EncoderTicks.toDistance(config.back_right_motor.getCurrentPosition(), (int) config.back_right_motor.getMotorType().getTicksPerRev(), Inches.of(d.getConstants().WHEEL_RADIUS * 2.0), 1).in(Inches));
        telemetry.addData("Dist real", d.getPoseEstimate().getX());
    }
}
