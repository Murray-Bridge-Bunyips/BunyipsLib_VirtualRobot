package au.edu.sa.mbhs.studentrobotics.virtual.robot.autonomous;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.BunyipsSubsystem;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.Mathf;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.bases.Task;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Geometry;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Tasks;
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
//        Robot.instance.drive.setPose(new Vector2d(32.67, -19.95), Inches, 204.44, Degrees);
        Task task = Robot.instance.drive.makeTrajectory()
                .splineTo(new Vector2d(-34.41, -13.88), Inches, 79.10, Degrees)
                .splineTo(new Vector2d(-9.40, 33.69), Inches, 5.02, Degrees)
                .splineTo(new Vector2d(27.47, 26.75), Inches, 260.39, Degrees)
                .splineTo(new Vector2d(9.25, -22.99), Inches, 177.27, Degrees)
                .splineTo(new Vector2d(41.35, 64.63), Inches, 270.00, Degrees)
                .splineTo(new Vector2d(35.71, -46.55), Inches, 242.15, Degrees)
                .splineTo(new Vector2d(-40.05, -15.61), Inches, 46.53, Degrees)
                .splineTo(new Vector2d(-39.33, 51.76), Inches, 89.39, Degrees)
                .build();

        Task t = Robot.instance.drive.makeTrajectory(new Pose2d(4, 5, 0))
                .splineTo(new Vector2d(30, 30), Inches, 90, Degrees)
                .splineTo(new Vector2d(0, 60), Inches, 180, Degrees)
                .splineTo(new Vector2d(-30, 30), Inches, -90, Degrees)
                .splineTo(new Vector2d(0, 0), Inches, 0, Degrees)
                .build();
//                .mutate()
//                .addPeriodic(() -> telemetry.addData("Circle progress", "%.1f%%", Mathf.wrapRadians(Robot.instance.drive.getPose().heading.log()) / Mathf.TWO_PI * 100));
        Tasks.register(task);
    }

    @Override
    public void loop() {
        Tasks.runRepeatedly(0);
        BunyipsSubsystem.updateAll();
    }
}
