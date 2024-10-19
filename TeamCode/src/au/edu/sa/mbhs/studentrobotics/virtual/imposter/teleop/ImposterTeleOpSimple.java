package au.edu.sa.mbhs.studentrobotics.virtual.imposter.teleop;

import au.edu.sa.mbhs.studentrobotics.virtual.imposter.components.ImposterConfig;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.murraybridgebunyips.bunyipslib.BunyipsOpMode;
import org.murraybridgebunyips.bunyipslib.Direction;
import org.murraybridgebunyips.bunyipslib.IMUEx;
import org.murraybridgebunyips.bunyipslib.Scheduler;
import org.murraybridgebunyips.bunyipslib.drive.CartesianFieldCentricMecanumDrive;
import org.murraybridgebunyips.bunyipslib.drive.CartesianMecanumDrive;
import org.murraybridgebunyips.bunyipslib.tasks.HolonomicDriveTask;

import static org.murraybridgebunyips.bunyipslib.external.units.Units.Degrees;

@TeleOp
public class ImposterTeleOpSimple extends BunyipsOpMode {
    private final ImposterConfig config = new ImposterConfig();
    private CartesianMecanumDrive drive;
    private Scheduler scheduler;
    private IMUEx imu;

    @Override
    protected void onInit() {
        config.init();
        scheduler = new Scheduler();
//        drive = new TriDeadwheelMecanumDrive(this, config.driveConstants, config.mecanumCoefficients, hardwareMap.voltageSensor, config.imu, config.front_left_motor, config.front_right_motor, config.back_left_motor, config.back_right_motor, config.localizerCoefficients, config.enc_left, config.enc_right, config.enc_x);
        drive = new CartesianFieldCentricMecanumDrive(config.front_left_motor, config.front_right_motor, config.back_left_motor, config.back_right_motor, config.imu, false, Direction.FORWARD);
        scheduler.addSubsystems(drive);
        drive.setDefaultTask(new HolonomicDriveTask(gamepad1, drive, () -> false));
        imu = new IMUEx(config.imu);
        imu.setYawOffset(Degrees.of(90));
        imu.setYawDomain(IMUEx.YawDomain.UNSIGNED);
//        scheduler.when(() -> drive.speedX != 0.0 || drive.speedY != 0.0 || drive.speedR != 0.0)
//                .runDebounced(() -> log("Drive has started moving at % seconds.", getRuntime()));
    }

    @Override
    protected void activeLoop() {
        imu.run();
        telemetry.add(imu.yaw.in(Degrees));
        scheduler.run();
    }
}
