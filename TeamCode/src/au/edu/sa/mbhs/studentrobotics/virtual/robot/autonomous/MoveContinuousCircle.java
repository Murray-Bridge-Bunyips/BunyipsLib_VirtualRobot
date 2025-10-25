package au.edu.sa.mbhs.studentrobotics.virtual.robot.autonomous;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.BunyipsSubsystem;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.Scheduler;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.Mathf;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.roadrunner.TaskBuilder;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.bases.Task;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Geometry;
import au.edu.sa.mbhs.studentrobotics.virtual.robot.Robot;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import static au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Units.Degrees;
import static au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Units.Inches;

@Autonomous(name = "Move Continuous Circle")
public class MoveContinuousCircle extends OpMode {
    @Override
    public void init() {
        Task t = Robot.instance.drive.makeTrajectory()
                .splineTo(new Vector2d(30, 30), Inches, 90, Degrees)
                .splineTo(new Vector2d(0, 60), Inches, 180, Degrees)
                .splineTo(new Vector2d(-30, 30), Inches, -90, Degrees)
                .splineTo(new Vector2d(0, 0), Inches, 0, Degrees)
                .build()
                .mutate()
                .addPeriodic(() -> telemetry.addData("Circle progress", "%.1f%%", Mathf.wrapRadians(Robot.instance.drive.getPose().heading.log()) / Mathf.TWO_PI * 100));
        Scheduler.schedule(t);    
    }
    
    @Override
    public void loop() {
        Scheduler.update();
    }
}
