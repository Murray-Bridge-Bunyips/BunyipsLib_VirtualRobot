package au.edu.sa.mbhs.studentrobotics.virtual.robot.autonomous;

import androidx.annotation.Nullable;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.AutonomousBunyipsOpMode;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.Dbg;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Ref;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import dev.frozenmilk.util.cell.RefCell;

import java.util.Arrays;

@Autonomous
public class TestUserSelection extends AutonomousBunyipsOpMode {
    @Override
    protected void onInitialise() {
        setOpModes(Arrays.asList("uno", new Object()), Arrays.asList("c", "a"));
    }
    
    @Override
    protected void onReady(@Nullable RefCell<?> selectedOpMode) {
        Dbg.log(Ref.stringify(selectedOpMode));
    }
}
