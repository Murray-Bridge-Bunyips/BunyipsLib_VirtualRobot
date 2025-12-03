package au.edu.sa.mbhs.studentrobotics.virtual.robot.autonomous;

import androidx.annotation.Nullable;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.AutonomousBunyipsOpMode;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.annotations.PreselectBehaviour;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.Mathf;
import au.edu.sa.mbhs.studentrobotics.virtual.robot.Robot;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import dev.frozenmilk.util.cell.RefCell;

import static au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Units.Degrees;
import static au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Units.Inches;

@Autonomous(name = "Move Continuous Circle", preselectTeleOp = "TeleOperation")
@PreselectBehaviour(action = PreselectBehaviour.Action.START)
public class MoveContinuousCircle extends AutonomousBunyipsOpMode {
    private final Robot robot = Robot.instance;
    
    @Override
    protected void onInitialise() {
    }

    @Override
    protected void onReady(@Nullable RefCell<?> selectedOpMode) {
        add(robot.drive.makeTrajectory()
                .splineTo(new Vector2d(30, 30), Inches, 90, Degrees)
                .splineTo(new Vector2d(0, 60), Inches, 180, Degrees)
                .splineTo(new Vector2d(-30, 30), Inches, -90, Degrees)
                .splineTo(new Vector2d(0, 0), Inches, 0, Degrees)
                .build()
                .mutate()
                .addPeriodic(() -> telemetry.addData("Circle progress", "%.1f%%", Mathf.wrapRadians(Robot.instance.drive.getPose().heading.log()) / Mathf.TWO_PI * 100))
                .repeatedly());
    }
}
