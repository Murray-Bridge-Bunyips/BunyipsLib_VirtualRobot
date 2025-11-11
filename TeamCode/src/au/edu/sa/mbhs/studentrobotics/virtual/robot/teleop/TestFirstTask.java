package au.edu.sa.mbhs.studentrobotics.virtual.robot.teleop;

import androidx.annotation.Nullable;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.AutonomousBunyipsOpMode;
import au.edu.sa.mbhs.studentrobotics.virtual.robot.Robot;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import dev.frozenmilk.util.cell.RefCell;

@Autonomous
public class TestFirstTask extends AutonomousBunyipsOpMode {
    @Override
    protected void onInitialise() {
    }

    @Override
    protected void onReady(@Nullable RefCell<?> selectedOpMode) {
        add(Robot.instance.holdableActuator.tasks.goTo(1000));
        add(Robot.instance.holdableActuator.tasks.goTo(1500));
        add(Robot.instance.holdableActuator.tasks.goTo(1500));
        add(Robot.instance.holdableActuator.tasks.goTo(1500));
    }
}
