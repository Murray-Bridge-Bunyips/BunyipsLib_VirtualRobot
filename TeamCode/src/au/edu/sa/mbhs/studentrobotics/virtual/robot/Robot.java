package au.edu.sa.mbhs.studentrobotics.virtual.robot;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.RobotConfig;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.control.pid.PController;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.control.pid.PIDController;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.hardware.IMUEx;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.hardware.Motor;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.localization.OTOSLocalizer;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.roadrunner.parameters.DriveModel;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.roadrunner.parameters.MecanumGains;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.roadrunner.parameters.MotionProfile;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.subsystems.drive.MecanumDrive;
import com.qualcomm.hardware.sparkfun.SparkFunOTOS;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * Running under "Mecanum Bot" config
 */
@RobotConfig.AutoInit
public class Robot extends RobotConfig {
    public static final Robot instance = new Robot();
    public final Hardware hw = new Hardware();
    public MecanumDrive drive;

    @Override
    protected void onRuntime() {
        hw.back_right_motor = getHardware("back_right_motor", Motor.class);
        hw.back_left_motor = getHardware("back_left_motor", Motor.class);
        hw.front_right_motor = getHardware("front_right_motor", Motor.class);
        hw.front_left_motor = getHardware("front_left_motor", Motor.class);

        hw.back_right_motor.setRunToPositionController(new PIDController(0.01, 0, 0));
        hw.back_left_motor.setRunToPositionController(new PIDController(0, 0, 0));
        hw.front_left_motor.setRunToPositionController(new PIDController(0.01, 0, 0));
        hw.front_right_motor.setRunToPositionController(new PIDController(0.01, 0, 0));

        hw.back_right_motor.setRunUsingEncoderController(new PController(1));
        hw.back_left_motor.setRunUsingEncoderController(new PController(1));
        hw.front_right_motor.setRunUsingEncoderController(new PController(1));
        hw.front_left_motor.setRunUsingEncoderController(new PController(1));

        hw.back_servo = getHardware("back_servo", Servo.class);

        hw.back_left_motor.setDirection(DcMotorEx.Direction.REVERSE);
        hw.front_left_motor.setDirection(DcMotorEx.Direction.REVERSE);
        
        hw.otos = getHardware("sensor_otos", SparkFunOTOS.class);

        // https://github.com/Beta8397/virtual_robot_RR1/blob/03e5d30b30558c1e67ee2478d45de3d136798074/TeamCode/src/org/firstinspires/ftc/teamcode/MecanumDrive.java
        DriveModel driveModel = new DriveModel.Builder()
                .setInPerTick(1) // using otos
                .setTrackWidthTicks(17.9) // is now in inches
                .build();
        MotionProfile motionProfile = new MotionProfile.Builder()
                .setKv(0.8)
                .setMaxWheelVel(25)
                .setMinProfileAccel(-30)
                .setMaxProfileAccel(50)
                .setMaxAngVel(3)
                .setMaxAngAccel(3)
                .build();
        MecanumGains mecanumGains = new MecanumGains.Builder()
                .setAxialGain(20)
                .setLateralGain(20)
                .setHeadingGain(20)
                .build();
        
        OTOSLocalizer.Params otosParams = new OTOSLocalizer.Params.Builder()
                .build();

        drive = new MecanumDrive(driveModel, motionProfile, mecanumGains, hw.front_left_motor, hw.back_left_motor, hw.back_right_motor, hw.front_right_motor, IMUEx.none(), hardwareMap.voltageSensor)
                .withLocalizer(new OTOSLocalizer(otosParams, hw.otos));
    }

    public static class Hardware {
        public SparkFunOTOS otos;
        public Motor back_right_motor;
        public Motor back_left_motor;
        public Motor front_right_motor;
        public Motor front_left_motor;
        public Servo back_servo;
    }
}
