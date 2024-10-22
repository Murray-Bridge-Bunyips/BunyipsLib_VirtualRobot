package au.edu.sa.mbhs.studentrobotics.virtual.robot.teleop;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.CommandBasedBunyipsOpMode;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.subsystems.drive.SimpleMecanumDrive;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.HolonomicDriveTask;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.transforms.Controls;
import au.edu.sa.mbhs.studentrobotics.virtual.robot.Robot;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import virtual_robot.config.Config;

@TeleOp
public class Drive extends CommandBasedBunyipsOpMode {
    private final Robot robot = new Robot();
    private SimpleMecanumDrive drive;

    @Override
    protected void onInitialise() {
        robot.init();
        drive = new SimpleMecanumDrive(robot.hw.front_left_motor, robot.hw.back_left_motor, robot.hw.back_right_motor, robot.hw.front_right_motor);
    }

    @Override
    protected void assignCommands() {
    }

    protected void periodic() {
        drive.setPower(Controls.vel(gamepad1.left_stick_x, gamepad1.left_stick_y, gamepad1.right_stick_x));
        drive.update();
    }
}
