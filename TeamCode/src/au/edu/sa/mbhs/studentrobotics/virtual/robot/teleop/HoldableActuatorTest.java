package au.edu.sa.mbhs.studentrobotics.virtual.robot.teleop;

import androidx.annotation.Nullable;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.AutonomousBunyipsOpMode;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.subsystems.HoldableActuator;
import au.edu.sa.mbhs.studentrobotics.virtual.robot.Robot;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import dev.frozenmilk.util.cell.RefCell;

import static au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Units.Seconds;

@TeleOp
public class HoldableActuatorTest extends AutonomousBunyipsOpMode {
    private HoldableActuator holdableActuator;
    
    @Override
    protected void onInitialise() {
        holdableActuator = new HoldableActuator(Robot.instance.hw.arm)
                .withName("ha");
    }

    @Override
    protected void onReady(@Nullable RefCell<?> selectedOpMode) {
        add(holdableActuator.tasks.goTo(1000));
        wait(2, Seconds);
//        add(holdableActuator.tasks.home());
    }
}
