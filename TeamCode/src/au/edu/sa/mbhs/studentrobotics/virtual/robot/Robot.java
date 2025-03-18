package au.edu.sa.mbhs.studentrobotics.virtual.robot;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.RobotConfig;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.control.pid.PController;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.control.pid.PIDController;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.hardware.IMUEx;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.hardware.Motor;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.localization.MecanumLocalizer;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.localization.accumulators.BoundedAccumulator;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.roadrunner.parameters.DriveModel;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.roadrunner.parameters.MecanumGains;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.roadrunner.parameters.MotionProfile;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.subsystems.drive.MecanumDrive;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Field;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.Servo;

import static au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Units.Inches;

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

        hw.arm = getHardware("arm_motor", DcMotorEx.class);

        hw.back_left_motor.setDirection(DcMotorEx.Direction.REVERSE);
        hw.front_left_motor.setDirection(DcMotorEx.Direction.REVERSE);

        // https://github.com/Beta8397/virtual_robot_RR1/blob/03e5d30b30558c1e67ee2478d45de3d136798074/TeamCode/src/org/firstinspires/ftc/teamcode/MecanumDrive.java
        double inPerTick = 1.0 / 89.1;
        DriveModel driveModel = new DriveModel.Builder()
                .setInPerTick(inPerTick)
                .setTrackWidthTicks(17.9 / inPerTick)
                .build();
        MotionProfile motionProfile = new MotionProfile.Builder()
                .setKv(0.0056)
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

        hw.imu = getHardware("imu", IMUEx.class, d -> {
            d.lazyInitialize(new IMU.Parameters(new RevHubOrientationOnRobot(
                    RevHubOrientationOnRobot.LogoFacingDirection.UP, 
                    RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
            )));
        });

        drive = new MecanumDrive(driveModel, motionProfile, mecanumGains, hw.front_left_motor, hw.back_left_motor, hw.back_right_motor, hw.front_right_motor, hw.imu, hardwareMap.voltageSensor)
                .withAccumulator(new BoundedAccumulator(Inches.of(9)).withRestrictedAreas(Field.Season.INTO_THE_DEEP));
        MecanumLocalizer l = (MecanumLocalizer) drive.getLocalizer();
        l.leftFront.setDirection(DcMotorSimple.Direction.REVERSE);
        l.leftBack.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    public static class Hardware {
        public Motor back_right_motor;
        public Motor back_left_motor;
        public Motor front_right_motor;
        public Motor front_left_motor;
        public IMUEx imu;
        public DcMotorEx arm;
    }
}
