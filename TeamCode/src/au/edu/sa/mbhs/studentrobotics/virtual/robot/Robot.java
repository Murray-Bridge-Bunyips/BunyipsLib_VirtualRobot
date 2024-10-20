package au.edu.sa.mbhs.studentrobotics.virtual.robot;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.RobotConfig;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.control.PIDFFController;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.control.SystemController;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.control.ff.SimpleMotorFeedforward;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.control.pid.PIDController;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.hardware.Motor;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.localization.MecanumLocalizer;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.roadrunner.parameters.DriveModel;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.roadrunner.parameters.MecanumGains;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.roadrunner.parameters.MotionProfile;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.subsystems.drive.MecanumDrive;
import com.acmerobotics.roadrunner.ftc.LazyImu;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * Running under "Mecanum Bot" config
 */
public class Robot extends RobotConfig {
    public final Hardware hw = new Hardware();
    public MecanumDrive drive;

    private SystemController getController(Motor motor) {
        return new PIDFFController(
                new PIDController(1, 0, 0),
                new SimpleMotorFeedforward(0, 0, 0),
                motor.getEncoder()
        );
    }

    @Override
    protected void onRuntime() {
        hw.back_right_motor = getHardware("back_right_motor", Motor.class);
        hw.back_left_motor = getHardware("back_left_motor", Motor.class);
        hw.front_right_motor = getHardware("front_right_motor", Motor.class);
        hw.front_left_motor = getHardware("front_left_motor", Motor.class);

        hw.back_right_motor.setRunToPositionController(new PIDController(0.01, 0, 0));
        hw.back_left_motor.setRunToPositionController(new PIDController(0.01, 0, 0));
        hw.front_left_motor.setRunToPositionController(new PIDController(0.01, 0, 0));
        hw.front_right_motor.setRunToPositionController(new PIDController(0.01, 0, 0));

        hw.back_right_motor.setRunUsingEncoderController(getController(hw.back_right_motor), 1);
        hw.back_left_motor.setRunUsingEncoderController(getController(hw.back_left_motor), 1);
        hw.front_right_motor.setRunUsingEncoderController(getController(hw.front_right_motor), 1);
        hw.front_left_motor.setRunUsingEncoderController(getController(hw.front_left_motor), 1);

        hw.back_servo = getHardware("back_servo", Servo.class);

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

        hw.lazyImu = new LazyImu(hardwareMap, "imu",
                new RevHubOrientationOnRobot(RevHubOrientationOnRobot.LogoFacingDirection.UP, RevHubOrientationOnRobot.UsbFacingDirection.FORWARD));

        drive = new MecanumDrive(driveModel, motionProfile, mecanumGains, hw.front_left_motor, hw.back_left_motor, hw.back_right_motor, hw.front_right_motor, hw.lazyImu, hardwareMap.voltageSensor);
        MecanumLocalizer l = (MecanumLocalizer) drive.getLocalizer();
        l.leftFront.setDirection(DcMotorSimple.Direction.REVERSE);
        l.leftBack.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    public static class Hardware {
        public Motor back_right_motor;
        public Motor back_left_motor;
        public Motor front_right_motor;
        public Motor front_left_motor;
        public LazyImu lazyImu;
        public Servo back_servo;
    }
}
