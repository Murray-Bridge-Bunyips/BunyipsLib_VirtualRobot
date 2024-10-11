package org.murraybridgebunyips.imposter.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import org.murraybridgebunyips.bunyipslib.BunyipsOpMode;
import org.murraybridgebunyips.bunyipslib.Controls;
import org.murraybridgebunyips.bunyipslib.Motor;
import org.murraybridgebunyips.bunyipslib.external.pid.PController;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

@TeleOp
public class ImposterMotorTest extends BunyipsOpMode {
    private Motor motor;
    private int setpoint;

//    private PrintWriter logWriter;

    @Override
    protected void onInit() {
        DcMotor m = hardwareMap.dcMotor.get("back_left_motor");
        motor = new Motor(m);
        motor.setRunToPositionController(new PController(0.001));
        motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
//        try {
//            logWriter = new PrintWriter(new FileWriter("motor_log.csv", false));
//            logWriter.println("timestamp,process,setpoint,response");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    protected void activeLoop() {
        motor.setTargetPosition(setpoint);

        t.addData("current", motor.getCurrentPosition());
        t.addData("setpoint", setpoint);
        if (gamepad1.getDebounced(Controls.A))
            setpoint += 100;
        if (gamepad1.getDebounced(Controls.B))
            setpoint -= 100;

        t.addData("response_power", motor.getPower());
        motor.setPower(1);

//        logWriter.printf("%d,%d,%d,%f%n", System.currentTimeMillis(), motor.getCurrentPosition(), motor.getTargetPosition(), motor.getPower());
//        logWriter.flush();
    }

    @Override
    public void onStop() {
//        if (logWriter != null) {
//            logWriter.close();
//        }
    }
}
