package au.edu.sa.mbhs.studentrobotics.virtual.robot.autonomous;

import androidx.annotation.Nullable;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.AutonomousBunyipsOpMode;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.Reference;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.bases.DynamicTask;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.transforms.Controls;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Geometry;
import au.edu.sa.mbhs.studentrobotics.virtual.robot.Robot;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import static au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Units.Degrees;
import static au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Units.Inches;


@Autonomous
public class RoadRunnerTest extends AutonomousBunyipsOpMode {
    private final Robot robot = new Robot();

    @Override
    protected void onInitialise() {
        robot.init();
    }

    @Override
    protected void onReady(@Nullable Reference<?> selectedOpMode, Controls selectedButton) {
        add(new DynamicTask(robot.drive.makeTrajectory()
                .splineTo(new Vector2d(30, 30), Inches, 90, Degrees)
                .turn(90, Degrees)
                .lineToX(50)
                .build()).addPeriodic(() -> t.add("no way we are at %", Geometry.toUserString(robot.drive.getPose()))));
    }
}
