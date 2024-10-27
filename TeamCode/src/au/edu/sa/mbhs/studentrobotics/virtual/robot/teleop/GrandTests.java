package au.edu.sa.mbhs.studentrobotics.virtual.robot.teleop;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.CommandBasedBunyipsOpMode;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.control.TrapezoidProfile;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.hardware.ProfiledServo;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.*;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.groups.SequentialTaskGroup;
import au.edu.sa.mbhs.studentrobotics.virtual.robot.Robot;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp
public class GrandTests extends CommandBasedBunyipsOpMode {
    private final Robot robot = new Robot();
    private ProfiledServo servo;

    @Override
    protected void onInitialise() {
        robot.init();
        servo = new ProfiledServo(robot.hw.back_servo);
        servo.setConstraints(new TrapezoidProfile.Constraints(1, 0.5));
    }

    @Override
    protected void assignCommands() {
        robot.drive.setDefaultTask(new HolonomicDriveTask(gamepad1, robot.drive));
        always().run(new SequentialTaskGroup(
                new ContinuousTask(() -> servo.setPosition(1)).until(() -> servo.getPosition() == 1),
                new ContinuousTask(() -> servo.setPosition(0)).until(() -> servo.getPosition() == 0)
        ).repeatedly().withName("Servo Oscillation"));
    }
}
