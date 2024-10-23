package au.edu.sa.mbhs.studentrobotics.virtual.robot.teleop;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.CommandBasedBunyipsOpMode;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.HolonomicDriveTask;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Geometry;
import au.edu.sa.mbhs.studentrobotics.virtual.robot.Robot;
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
        robot.drive.setDefaultTask(new HolonomicDriveTask(gamepad1, robot.drive));
        always().run(() -> t.add(Geometry.toString(robot.drive.getPose())));
    }
}
