package au.edu.sa.mbhs.studentrobotics.virtual.robot.teleop;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.CommandBasedBunyipsOpMode;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.Dbg;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.control.TrapezoidProfile;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.hardware.ProfiledServo;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.subsystems.HoldableActuator;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.*;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.bases.Task;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.groups.SequentialTaskGroup;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.transforms.Controls;
import au.edu.sa.mbhs.studentrobotics.virtual.robot.Robot;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import static au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Units.Seconds;

@TeleOp
public class GrandTests extends CommandBasedBunyipsOpMode {
    private final Robot robot = new Robot();
//    private ProfiledServo servo;
//    private HoldableActuator ha;

    @Override
    protected void onInitialise() {
        robot.init();
//        ha = new HoldableActuator(robot.hw.back_left_motor)
//                .withTolerance(1, true)
//                .withMaxSteadyStateTime(Seconds.of(1));
//        servo = new ProfiledServo(robot.hw.back_servo);
//        servo.setConstraints(new TrapezoidProfile.Constraints(1, 0.5));
    }

    @Override
    protected void assignCommands() {
        driver().whenPressed(Controls.A).run(Task.task().init(Dbg::stamp).onFinish(() -> Dbg.log("STOP"))).finishIf(() -> gamepad1.getDebounced(Controls.A));
//        ha.setDefaultTask(ha.tasks.control(() -> -gamepad1.lsy));
//        driver().whenPressed(Controls.A).run(ha.tasks.goTo(1000));
//        driver().whenPressed(Controls.B).run(ha.tasks.goTo(0));
//        robot.drive.setDefaultTask(new HolonomicDriveTask(gamepad1, robot.drive));
//        always().run(new SequentialTaskGroup(
//                new ContinuousTask(() -> servo.setPosition(1)).until(() -> servo.getPosition() == 1),
//                new ContinuousTask(() -> servo.setPosition(0)).until(() -> servo.getPosition() == 0)
//        ).repeatedly().withName("Servo Oscillation"));
    }
}
