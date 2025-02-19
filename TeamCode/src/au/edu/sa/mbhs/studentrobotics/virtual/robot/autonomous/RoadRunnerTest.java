package au.edu.sa.mbhs.studentrobotics.virtual.robot.autonomous;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.BunyipsSubsystem;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.bases.Task;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Geometry;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Tasks;
import au.edu.sa.mbhs.studentrobotics.virtual.robot.Robot;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import javafx.scene.control.skin.TableCellSkin;

import static au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Units.Degrees;
import static au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Units.Inches;

@Autonomous
public class RoadRunnerTest extends OpMode {
    @Override
    public void init() {
        Task t = Robot.instance.drive.makeTrajectory()
                .splineTo(new Vector2d(30, 30), Inches, 90, Degrees)
                .turn(90, Degrees)
                .lineToX(50)
                .build().mutate().addPeriodic(() -> telemetry.addData("", "no way we are at %s", Geometry.toUserString(Robot.instance.drive.getPose())));
        Tasks.register(t);
    }

    @Override
    public void loop() {
        Tasks.run(0);
        BunyipsSubsystem.updateAll();
    }
}
