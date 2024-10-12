package org.murraybridgebunyips.imposter.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.jetbrains.annotations.Nullable;
import org.murraybridgebunyips.bunyipslib.AutonomousBunyipsOpMode;
import org.murraybridgebunyips.bunyipslib.Controls;
import org.murraybridgebunyips.bunyipslib.PurePursuit;
import org.murraybridgebunyips.bunyipslib.Reference;
import org.murraybridgebunyips.bunyipslib.drive.MecanumDrive;
import org.murraybridgebunyips.bunyipslib.tasks.TurnTask;
import org.murraybridgebunyips.imposter.components.ImposterConfig;

import static org.murraybridgebunyips.bunyipslib.external.units.Units.Degrees;
import static org.murraybridgebunyips.bunyipslib.external.units.Units.Inches;

@TeleOp
public class ImposterPPTest extends AutonomousBunyipsOpMode {
    private final ImposterConfig config = new ImposterConfig();
    private PurePursuit pp;
    private MecanumDrive drive;

    @Override
    protected void onInitialise() {
        config.init();
        drive = new MecanumDrive(config.driveConstants, config.mecanumCoefficients, config.imu, config.front_left_motor, config.front_right_motor, config.back_left_motor, config.back_right_motor);
        pp = new PurePursuit(drive).withLookaheadRadius(Inches.of(8));
//        pp.followPath(pp.makePath().splineTo(new Vector2d(30, 30), Inches, 90, Degrees).buildPath());
//        pp.followPath(pp.makePath().strafeTo(new Vector2d(30, 30), Inches).buildPath());
    }

    @Override
    protected void onReady(@Nullable Reference<?> selectedOpMode, Controls selectedButton) {
//        pp.makePath()
//                .strafeTo(new Vector2d(30, 30), Inches)
//                .addTask()
//                .onSubsystem(drive);
//
//        pp.makePath()
//                .reversed()
//                .splineTo(new Vector2d(), Inches, 180, Degrees)
//                .addTask()
//                .onSubsystem(drive);
//
        addTask(new TurnTask(drive, Degrees.of(45)));
//        addTask(new TurnTask(drive, Degrees.of(180)));
//        addTask(new TurnTask(drive, Degrees.of(45)));
//        addTask(new TurnTask(drive, Degrees.of(20)));
//        addTask(new TurnTask(drive, Degrees.of(20).negate()));
    }
}
