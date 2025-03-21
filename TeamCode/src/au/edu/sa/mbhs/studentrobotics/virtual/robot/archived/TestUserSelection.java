package au.edu.sa.mbhs.studentrobotics.virtual.robot.archived;

import androidx.annotation.Nullable;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.AutonomousBunyipsOpMode;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Dbg;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Ref;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import dev.frozenmilk.util.cell.RefCell;

import java.util.Arrays;

@Autonomous
@Disabled
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
