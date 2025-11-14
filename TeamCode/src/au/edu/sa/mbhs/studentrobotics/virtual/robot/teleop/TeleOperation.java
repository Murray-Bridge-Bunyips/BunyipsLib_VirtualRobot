package au.edu.sa.mbhs.studentrobotics.virtual.robot.teleop;

import static au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Units.Second;
import static au.edu.sa.mbhs.studentrobotics.bunyipslib.transforms.Controls.*;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.BunyipsOpMode;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.Scheduler;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.HolonomicDriveTask;
import au.edu.sa.mbhs.studentrobotics.virtual.robot.Robot;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp
public class TeleOperation extends BunyipsOpMode {
    private final Robot robot = Robot.instance;
    
    @Override
    protected void onInit() {
        new HolonomicDriveTask(gamepad1, robot.drive).setAsDefaultTask();
        gamepad1.button(A)
                .toggleOnTrue(robot.shooter.tasks.run(1).named("Run Shooter"));
        gamepad1.button(X)
                .toggleOnTrue(robot.intake.tasks.run(1).named("Run Intake"));
        gamepad1.button(Y)
                .whileTrue(robot.scoop.tasks.run(1).named("Run Scoop Forward"));
        gamepad1.button(B)
                .whileTrue(robot.scoop.tasks.run(-1).named("Run Scoop Backward"));
        gamepad1.button(RIGHT_BUMPER)
                .onTrue(robot.kicker.tasks.close().named("Kick").then(robot.kicker.tasks.open().named("Arm").after(1, Second)));
    }

    @Override
    protected void activeLoop() {
        Scheduler.update();
    }
}
