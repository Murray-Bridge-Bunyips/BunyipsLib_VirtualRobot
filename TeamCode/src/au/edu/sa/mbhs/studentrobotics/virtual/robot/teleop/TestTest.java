package au.edu.sa.mbhs.studentrobotics.virtual.robot.teleop;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.BunyipsOpMode;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.hardware.Motor;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.hardware.SimpleRotator;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp
public class TestTest extends BunyipsOpMode {
    private Motor motor;

    @Override
    protected void onInit() {
        motor = new Motor(hardwareMap.dcMotor.get("back_left_motor"));
    }

    @Override
    protected void activeLoop() {
        motor.setPower(-gamepad1.left_stick_y);
        telemetry.addData("power", motor.getPower());
        telemetry.addData("direction", motor.getDirection());
        telemetry.addData("real direction", motor.encoder.getDirection());
        if (gamepad1.a)
            motor.setDirection(DcMotorSimple.Direction.REVERSE);
        else if (gamepad1.b)
            motor.setDirection(DcMotorSimple.Direction.FORWARD);
        telemetry.addData("pos", motor.getCurrentPosition());
        telemetry.addData("vel", motor.getVelocity());
    }
}
