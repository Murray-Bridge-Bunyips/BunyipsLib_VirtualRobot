package au.edu.sa.mbhs.studentrobotics.virtual.robot.autonomous;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.BunyipsOpMode;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.executables.UserSelection;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Dbg;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Threads;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import java.util.Arrays;

@TeleOp
public class TestManualUserSelection extends BunyipsOpMode {
    @Override
    protected void onInit() {
        var a = new UserSelection<>(Dbg::log, Arrays.asList("uno", new Object()), Arrays.asList("c", "a")).captionLayer(0, "CHOOSE SOMETHING");
//        a.disableChaining();
        Threads.start("a", a);
    }
    
    @Override
    protected void activeLoop() {
        
    }
}
