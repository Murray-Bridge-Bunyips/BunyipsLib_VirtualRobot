package org.murraybridgebunyips.imposter.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import org.jetbrains.annotations.Nullable;
import org.murraybridgebunyips.bunyipslib.AutonomousBunyipsOpMode;
import org.murraybridgebunyips.bunyipslib.Controls;
import org.murraybridgebunyips.bunyipslib.Reference;
import org.murraybridgebunyips.bunyipslib.StartingConfiguration;

import static org.murraybridgebunyips.bunyipslib.StartingConfiguration.*;
import static org.murraybridgebunyips.bunyipslib.external.units.Units.*;

@Autonomous
public class ImposterStartingConfTest extends AutonomousBunyipsOpMode {
    @Override
    protected void onInitialise() {
        setOpModes(
                blueLeft().tile(4).rotate(Degrees.of(45)),
                blueRight().translate(Centimeters.of(15)).backward(Inches.of(5)),
                redLeft().tile(5.5).rotate(Degrees.of(90)),
                redRight().tile(2).rotate(Degrees.of(90)).backward(Centimeters.of(3))
        );
    }

    @Override
    protected void onReady(@Nullable Reference<?> selectedOpMode, Controls selectedButton) {
        telemetry.log(((StartingConfiguration.Position) selectedOpMode.require()).toFieldPose());
    }
}
