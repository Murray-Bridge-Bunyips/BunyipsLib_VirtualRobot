package au.edu.sa.mbhs.studentrobotics.virtual.robot.teleop;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.BunyipsOpMode;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.RobotConfig;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.Mathf;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.control.pid.PIDFController;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import static au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Units.Seconds;

@TeleOp
@RobotConfig.InhibitAutoInit
public class EncoderTest extends BunyipsOpMode {
    private BufferedWriter file;
    private PIDFController pid;
    
    private double displacement = 0;
    private double velocity = 0;

    @Override
    protected void onInit() {
        try {
            file = new BufferedWriter(new FileWriter("./data.csv"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        pid = new PIDFController(20, 0.0, 20, 0.0);
        pid.setDerivativeSmoothingGain(0.8);
        try {
            file.write("time,s,v,a");
            file.newLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setLoopSpeed(Seconds.of(0.1));
    }

    @Override
    protected void activeLoop() {
        double target = 100;
        double maxVelocity = 10;
        double maxAcceleration = 5;
        
        double targetVelocity = pid.calculate(displacement, target);
        double dV = Mathf.moveTowards(velocity, targetVelocity, maxAcceleration * timer.deltaTime().in(Seconds)) - velocity;
        if (velocity >= maxVelocity && dV > 0)
            dV = 0;
        if (velocity <= -maxVelocity && dV < 0)
            dV = 0;
        double dVdT = dV / timer.deltaTime().in(Seconds);
        velocity += dV;
        velocity = Mathf.clamp(velocity, -maxVelocity, maxVelocity);
        displacement += velocity * timer.deltaTime().in(Seconds);
        telemetry.addData("s", displacement);
        telemetry.addData("ds/dt", velocity);
        telemetry.addData("d^2s/dt^2", dVdT);
        double pidError = Math.abs(pid.getErrorDerivative());
        telemetry.addData("pidError", pidError);
        try {
            file.write(String.format("%f,%f,%f,%f\n", timer.elapsedTime().in(Seconds), displacement, velocity, dVdT));
            file.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    protected void onStop() {
        try {
            file.flush();
            file.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
// giulio was here