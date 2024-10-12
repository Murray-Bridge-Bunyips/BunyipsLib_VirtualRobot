package org.murraybridgebunyips.imposter.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.murraybridgebunyips.bunyipslib.BunyipsOpMode;
import org.murraybridgebunyips.bunyipslib.Controls;
import org.murraybridgebunyips.bunyipslib.ProfiledServo;
import org.murraybridgebunyips.bunyipslib.external.TrapezoidProfile;
import org.murraybridgebunyips.bunyipslib.subsystems.Switch;
import org.murraybridgebunyips.imposter.components.ImposterConfig;

@TeleOp
public class ImposterSwitchTest extends BunyipsOpMode {
    private final ImposterConfig config = new ImposterConfig();

    private ProfiledServo servo;

    @Override
    protected void onInit() {
        config.init();
        servo = new ProfiledServo(config.back_servo);
        servo.setConstraints(new TrapezoidProfile.Constraints(1, 0.8));
    }

    @Override
    protected void activeLoop() {
        servo.setPosition(gamepad1.a ? 0.5 : 0.3);
        t.add("servo pos %", servo.getPosition());
    }
}
