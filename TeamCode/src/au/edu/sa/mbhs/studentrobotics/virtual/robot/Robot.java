package au.edu.sa.mbhs.studentrobotics.virtual.robot;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.RobotConfig;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.localization.OTOSLocalizer;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.roadrunner.parameters.DriveModel;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.roadrunner.parameters.MecanumGains;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.roadrunner.parameters.MotionProfile;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.subsystems.Actuator;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.subsystems.Switch;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.subsystems.drive.MecanumDrive;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.hardware.sparkfun.SparkFunOTOS;
import com.qualcomm.robotcore.hardware.*;

/**
 * Running under "Ulti Bot" config
 */
@RobotConfig.AutoInit
public class Robot extends RobotConfig {
    public static final Robot instance = new Robot();
    public final Hardware hw = new Hardware();
    
    public MecanumDrive drive;
    public Actuator intake;
    public Actuator shooter;
    public Actuator scoop;
    public Switch kicker;

    @Override
    protected void onRuntime() {
        hw.back_right_motor = getHardware("back_right_motor", DcMotorEx.class);
        hw.back_left_motor = getHardware("back_left_motor", DcMotorEx.class);
        hw.front_right_motor = getHardware("front_right_motor", DcMotorEx.class);
        hw.front_left_motor = getHardware("front_left_motor", DcMotorEx.class);
        
        hw.otos = getHardware("sensor_otos", SparkFunOTOS.class);
        
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
        OTOSLocalizer.Params otosParams = new OTOSLocalizer.Params.Builder()
                .build();

        hw.imu = getHardware("imu", IMU.class, d ->
                d.initialize(new IMU.Parameters(new RevHubOrientationOnRobot(RevHubOrientationOnRobot.LogoFacingDirection.UP, RevHubOrientationOnRobot.UsbFacingDirection.FORWARD))));
        hw.color_sensor = getHardware("color_sensor", ColorSensor.class);
        hw.left_distance = getHardware("left_distance", DistanceSensor.class);
        hw.right_distance = getHardware("right_distance", DistanceSensor.class);
        hw.front_distance = getHardware("front_distance", DistanceSensor.class);
        hw.back_distance = getHardware("back_distance", DistanceSensor.class);

        hw.intake_motor = getHardware("intake_motor", DcMotorEx.class);
        hw.shooter_motor = getHardware("shooter_motor", DcMotorEx.class);
        hw.scoop_motor = getHardware("scoop_motor", DcMotorEx.class);
        hw.kicker_servo = getHardware("kicker_servo", Servo.class);

        drive = new MecanumDrive(driveModel, motionProfile, mecanumGains, hw.front_left_motor, hw.back_left_motor, hw.back_right_motor, hw.front_right_motor, hw.imu)
                .withLocalizer(new OTOSLocalizer(otosParams, hw.otos))
                .withName("Drive");
        intake = new Actuator(hw.intake_motor)
                .withName("Intake");
        shooter = new Actuator(hw.shooter_motor)
                .withName("Shooter");
        scoop = new Actuator(hw.scoop_motor)
                .withName("Scoop");
        kicker = new Switch(hw.kicker_servo)
                .withName("Kicker");
    }

    public static class Hardware {
        public DcMotorEx back_right_motor;
        public DcMotorEx back_left_motor;
        public DcMotorEx front_right_motor;
        public DcMotorEx front_left_motor;
        public SparkFunOTOS otos;
        public IMU imu;
        public ColorSensor color_sensor;
        public DistanceSensor left_distance;
        public DistanceSensor right_distance;
        public DistanceSensor front_distance;
        public DistanceSensor back_distance;
        public DcMotorEx intake_motor;
        public DcMotorEx shooter_motor;
        public DcMotorEx scoop_motor;
        public Servo kicker_servo;
    }
}
