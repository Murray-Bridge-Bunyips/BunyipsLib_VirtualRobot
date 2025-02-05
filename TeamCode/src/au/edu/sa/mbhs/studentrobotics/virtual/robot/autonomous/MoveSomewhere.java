package au.edu.sa.mbhs.studentrobotics.virtual.robot.autonomous;

import androidx.annotation.Nullable;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.AutonomousBunyipsOpMode;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.roadrunner.TaskBuilder;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Geometry;
import au.edu.sa.mbhs.studentrobotics.virtual.robot.Robot;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import dev.frozenmilk.util.cell.RefCell;

@TeleOp
public class MoveSomewhere extends AutonomousBunyipsOpMode {
    @Override
    protected void onInitialise() {
        setOpModes("forward", "backward", "left", "right");
        Robot.instance.drive.setPose(Geometry.zeroPose());
    }
    
    @Override
    protected void onReady(@Nullable RefCell<?> selectedOpMode) {
        if (selectedOpMode == null) return;
        TaskBuilder tb = Robot.instance.drive.makeTrajectory();
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
