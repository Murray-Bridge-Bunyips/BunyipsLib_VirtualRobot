package au.edu.sa.mbhs.studentrobotics.virtual.robot.autonomous;

import androidx.annotation.Nullable;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.AutonomousBunyipsOpMode;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.roadrunner.TaskBuilder;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Geometry;
import au.edu.sa.mbhs.studentrobotics.virtual.robot.Robot;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import dev.frozenmilk.util.cell.RefCell;

@Autonomous(name = "Move One Tile")
public class MoveOneTile extends AutonomousBunyipsOpMode {
    private final Robot robot = Robot.instance;
    
    @Override
    protected void onInitialise() {
        setOpModes("forward", "backward", "left", "right");
        robot.drive.setPose(Geometry.zeroPose());
    }

    @Override
    protected void onReady(@Nullable RefCell<?> selectedOpMode) {
        if (selectedOpMode == null) return;
        TaskBuilder tb = robot.drive.makeTrajectory();
        switch ((String) selectedOpMode.get()) {
            case "forward":
                tb.lineToX(24);
                break;
            case "backward":
                tb.lineToX(-24);
                break;
            case "left":
                tb.setTangent(Math.PI / 2);
                tb.lineToY(24);
                break;
            case "right":
                tb.setTangent(-Math.PI / 2);
                tb.lineToY(-24);
                break;
        }
        tb.addTask();
    }
}
