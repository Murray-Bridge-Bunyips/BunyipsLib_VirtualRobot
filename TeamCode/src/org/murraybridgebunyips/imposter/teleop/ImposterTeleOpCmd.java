package org.murraybridgebunyips.imposter.teleop;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.DoubleSupplier;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.murraybridgebunyips.bunyipslib.*;
import org.murraybridgebunyips.bunyipslib.drive.MecanumDrive;
import org.murraybridgebunyips.bunyipslib.drive.TriDeadwheelMecanumDrive;
import org.murraybridgebunyips.bunyipslib.tasks.*;
import org.murraybridgebunyips.imposter.components.ImposterConfig;

import static org.murraybridgebunyips.bunyipslib.external.units.Units.*;

/** bunyipslib virtual testing ground */
@TeleOp

public class ImposterTeleOpCmd extends CommandBasedBunyipsOpMode {
    private final ImposterConfig config = new ImposterConfig();
    private MecanumDrive drive;
    private DoubleSupplier model = () -> config.back_left_motor.getVelocity();
    private DoubleSupplier sensor = () -> config.back_left_motor.getVelocity() + ((Math.random() - 0.5) * 40) + 10;
    private final Filter.Kalman filter = new Filter.Kalman(model, sensor, 8, 0.00001);
    private PrintWriter logWriter;

    @Override
    protected void onInitialise() {
        config.init();
        drive = new TriDeadwheelMecanumDrive(config.driveConstants, config.mecanumCoefficients, config.imu, config.front_left_motor, config.front_right_motor, config.back_left_motor, config.back_right_motor, config.localizerCoefficients, config.enc_left, config.enc_right, config.enc_x);

        try {
            logWriter = new PrintWriter(new FileWriter("telemetry_log.csv", false));
            logWriter.println("timestamp,ang_velo,lowpass_angvelo,ang_velo_false");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void assignCommands() {
        HolonomicVectorDriveTask t = new HolonomicVectorDriveTask(gamepad1, drive, () -> false);
        drive.setDefaultTask(t);
        driver().whenPressed(Controls.A).run(() -> t.setHeadingTarget(Degrees.zero()));
    }

    protected void periodic() {
        double angVelo = model.getAsDouble();
        double angVelo2 = sensor.getAsDouble();
        double lowPassAngVelo = filter.getAsDouble();
        long timestamp = System.currentTimeMillis();

        logWriter.printf("%d,%.6f,%.6f,%.6f%n", timestamp, angVelo, lowPassAngVelo, angVelo2);
        logWriter.flush();
    }

    @Override
    public void onStop() {
        if (logWriter != null) {
            logWriter.close();
        }
    }
}