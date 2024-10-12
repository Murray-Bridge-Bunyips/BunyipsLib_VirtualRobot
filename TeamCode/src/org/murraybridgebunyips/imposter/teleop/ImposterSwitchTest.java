package org.murraybridgebunyips.imposter.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.murraybridgebunyips.bunyipslib.BunyipsOpMode;
import org.murraybridgebunyips.bunyipslib.ProfiledServo;
import org.murraybridgebunyips.bunyipslib.external.TrapezoidProfile;
import org.murraybridgebunyips.imposter.components.ImposterConfig;

import static org.murraybridgebunyips.bunyipslib.external.units.Units.Milliseconds;

@TeleOp
public class ImposterSwitchTest extends BunyipsOpMode {
    private final ImposterConfig config = new ImposterConfig();

    private ProfiledServo servo;

    @Override
    protected void onInit() {
        config.init();
        servo = new ProfiledServo(config.back_servo);
        servo.setConstraints(new TrapezoidProfile.Constraints(2, 0.8));
        servo.setPositionRefreshRate(Milliseconds.of(50));
        servo.setPositionDeltaThreshold(0.005);
    }

    @Override
    protected void activeLoop() {
        servo.setPosition(gamepad1.a ? 1 : 0);
        t.add("servo pos %", servo.getPosition());
    }
}
