package au.edu.sa.mbhs.studentrobotics.virtual.robot.teleop;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.BunyipsOpMode;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.Encoder;
import au.edu.sa.mbhs.studentrobotics.virtual.robot.Robot;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp
@Disabled
public class EncoderTest extends BunyipsOpMode {
    private final Robot robot = new Robot();
    private Encoder encoder;

    @Override
    protected void onInit() {
        robot.init();
        encoder = robot.hw.back_left_motor.getEncoder();
    }

    @Override
    protected void activeLoop() {
        t.add(encoder.getPosition());
        robot.hw.back_left_motor.setPower(-gamepad1.lsy);
        if (gamepad1.a)
            encoder.setKnownPosition(3000);
        if (gamepad1.b)
            encoder.reset();
    }
}
// giulio was here