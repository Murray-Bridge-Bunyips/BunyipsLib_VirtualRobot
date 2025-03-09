package au.edu.sa.mbhs.studentrobotics.virtual.robot.teleop;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.CommandBasedBunyipsOpMode;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.RobotConfig;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.HolonomicDriveTask;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.bases.Task;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Dbg;
import au.edu.sa.mbhs.studentrobotics.virtual.robot.Robot;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import virtual_robot.controller.VirtualBot;

@TeleOp
@RobotConfig.InhibitAutoInit
public class Drive extends CommandBasedBunyipsOpMode {
    private final Robot robot = new Robot();

    @Override
    protected void onInitialise() {
        robot.init();
//        setInitTask(robot.drive.makeTrajectory().lineToX(30).build().mutate().addPeriodic(() -> robot.drive.update()));
    }

    @Override
    protected void assignCommands() {
        robot.drive.setDefaultTask(new HolonomicDriveTask(gamepad1, robot.drive));
    }
}
