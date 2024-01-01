package org.murraybridgebunyips.bunyipslib.personalitycore.submodules;

import androidx.annotation.NonNull;

import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

import org.murraybridgebunyips.bunyipslib.BunyipsComponent;
import org.murraybridgebunyips.bunyipslib.BunyipsOpMode;

/**
 * Suspension hook for the GLaDOS/Wheatley robot.
 *
 * @author Lucas Bubner, 2023
 */
public class PersonalityCoreHook extends BunyipsComponent {
    private final Servo hook;

    // Assumes a scale range is being used
    private final double EXTENDED = 1.0;
    private final double UPRIGHT = 0.9;
    private final double RETRACTED = 0.0;
    private double target;

    public PersonalityCoreHook(@NonNull BunyipsOpMode opMode, Servo hook) {
        super(opMode);
        this.hook = hook;
        target = RETRACTED;
        update();
    }

    public void actuateUsingController(double y) {
        target -= y / 5;
        target = Range.clip(target, 0.0, 1.0);
    }

    public void setPosition(double target) {
        this.target = Range.clip(target, 0.0, 1.0);
    }

    public void extend() {
        target = EXTENDED;
    }

    public void retract() {
        target = RETRACTED;
    }

    public void upright() {
        target = UPRIGHT;
    }

    public void update() {
        hook.setPosition(target);
        getOpMode().addTelemetry("Hook: %", target == EXTENDED ? "EXTENDED" : target == RETRACTED ? "RETRACTED" : target == UPRIGHT ? "UPRIGHT" : "CUSTOM_POS");
    }
}