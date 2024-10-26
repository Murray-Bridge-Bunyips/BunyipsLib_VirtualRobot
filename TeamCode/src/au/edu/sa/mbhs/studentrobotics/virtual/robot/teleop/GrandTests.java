package au.edu.sa.mbhs.studentrobotics.virtual.robot.teleop;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.CommandBasedBunyipsOpMode;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.control.pid.PController;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.control.pid.PDController;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.DriveToPoseTask;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.bases.Task;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.groups.SequentialTaskGroup;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.transforms.Controls;
import au.edu.sa.mbhs.studentrobotics.virtual.robot.Robot;
import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp
public class GrandTests extends CommandBasedBunyipsOpMode {
    private final Robot robot = new Robot();

    @Override
    protected void onInitialise() {
        robot.init();
    }

    @Override
    protected void assignCommands() {
//        robot.drive.setDefaultTask(new HolonomicDriveTask(gamepad1, robot.drive));
//        always().run(() -> t.add(Geometry.toUserString(robot.drive.getPose())));
        PDController forwardController = new PDController(1, 0.001);
        PDController strafeController = new PDController(1, 0.001);
        PDController headingController = new PDController(1, 0.001);
        driver().whenPressed(Controls.A).run(
                new SequentialTaskGroup(
                        new DriveToPoseTask(Task.INFINITE_TIMEOUT, robot.drive, new Pose2d(20, 20, Math.PI / 2), forwardController, strafeController, headingController),
                        new DriveToPoseTask(Task.INFINITE_TIMEOUT, robot.drive, new Pose2d(0, 0, 3 * Math.PI / 2), forwardController, strafeController, headingController),
                        new DriveToPoseTask(Task.INFINITE_TIMEOUT, robot.drive, new Pose2d(20, -20, 0), forwardController, strafeController, headingController),
                        new DriveToPoseTask(Task.INFINITE_TIMEOUT, robot.drive, new Pose2d(0, 0, 0), forwardController, strafeController, headingController),
                        new DriveToPoseTask(Task.INFINITE_TIMEOUT, robot.drive, new Pose2d(-10, -10, 0), forwardController, strafeController, headingController),
                        new DriveToPoseTask(Task.INFINITE_TIMEOUT, robot.drive, new Pose2d(0, 0, 0), forwardController, strafeController, headingController),
                        new DriveToPoseTask(Task.INFINITE_TIMEOUT, robot.drive, new Pose2d(-10, 10, 0), forwardController, strafeController, headingController),
                        new DriveToPoseTask(Task.INFINITE_TIMEOUT, robot.drive, new Pose2d(0, 0, 0), forwardController, strafeController, headingController),
                        new DriveToPoseTask(Task.INFINITE_TIMEOUT, robot.drive, new Pose2d(-10, 0, 0), forwardController, strafeController, headingController),
                        new DriveToPoseTask(Task.INFINITE_TIMEOUT, robot.drive, new Pose2d(0, 10, 0), forwardController, strafeController, headingController),
                        new DriveToPoseTask(Task.INFINITE_TIMEOUT, robot.drive, new Pose2d(0, 0, 0), forwardController, strafeController, headingController),
                        new DriveToPoseTask(Task.INFINITE_TIMEOUT, robot.drive, new Pose2d(0, 0, 3 * Math.PI / 2), forwardController, strafeController, headingController),
                        new DriveToPoseTask(Task.INFINITE_TIMEOUT, robot.drive, new Pose2d(20, 20, Math.PI / 2), forwardController, strafeController, headingController),
                        new DriveToPoseTask(Task.INFINITE_TIMEOUT, robot.drive, new Pose2d(0, 0, 0), forwardController, strafeController, headingController)
                ).repeatedly());
    }
}
