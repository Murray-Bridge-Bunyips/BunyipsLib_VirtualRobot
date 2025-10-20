package au.edu.sa.mbhs.studentrobotics.virtual.robot.teleop;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.BunyipsOpMode;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.Scheduler;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.HolonomicDriveTask;
import au.edu.sa.mbhs.studentrobotics.virtual.robot.Robot;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp
public class Drive extends BunyipsOpMode {
    @Override
    protected void onInit() {
        new HolonomicDriveTask(gamepad1, Robot.instance.drive).setAsDefaultTask();
    }

    @Override
    protected void activeLoop() {
        Scheduler.update();
    }
}
