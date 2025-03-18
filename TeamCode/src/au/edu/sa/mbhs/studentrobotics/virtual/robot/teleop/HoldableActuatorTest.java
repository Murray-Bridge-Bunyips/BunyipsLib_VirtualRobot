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
        holdableActuator.tasks.control(() -> gamepad1.lsy * -1).setAsDefaultTask();
        add(holdableActuator.tasks.goTo(1000));
        add(holdableActuator.tasks.goTo(0));
        add(holdableActuator.tasks.goTo(1200));
        add(holdableActuator.tasks.home());
//        add(holdableActuator.tasks.home());
    }
}
