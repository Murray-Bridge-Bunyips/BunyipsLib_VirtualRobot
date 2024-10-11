package org.murraybridgebunyips.imposter.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.murraybridgebunyips.bunyipslib.BunyipsOpMode;
import org.murraybridgebunyips.bunyipslib.Controls;
import org.murraybridgebunyips.bunyipslib.subsystems.Switch;
import org.murraybridgebunyips.imposter.components.ImposterConfig;

@TeleOp
public class ImposterSwitchTest extends BunyipsOpMode {
    private final ImposterConfig config = new ImposterConfig();
    private Switch servo;

    @Override
    protected void onInit() {
        config.init();
        servo = new Switch(config.back_servo);
    }

    @Override
    protected void activeLoop() {
        if (gamepad1.getDebounced(Controls.A)) servo.toggle();

        servo.update();
    }
}
