package au.edu.sa.mbhs.studentrobotics.virtual.apartmentbot;

import static au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Units.InchesPerSecond;
import static au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Units.Seconds;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.BunyipsOpMode;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.RobotConfig;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.control.pid.PIDFController;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Measure;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Time;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.hardware.IMUEx;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.hardware.Motor;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.hardware.ServoEx;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.localization.OTOSLocalizer;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.roadrunner.parameters.DriveModel;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.roadrunner.parameters.MecanumGains;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.roadrunner.parameters.MotionProfile;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.subsystems.Actuator;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.subsystems.Switch;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.subsystems.drive.MecanumDrive;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.groups.ParallelTaskGroup;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.groups.SequentialTaskGroup;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.hardware.sparkfun.SparkFunOTOS;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.IMU;

public class ApartmentBot extends RobotConfig {
    public static class Hardware {
        /**
         * Control 0: frontLeft
         */
        public DcMotor fl;

        /**
         * Control 3: frontRight
         */
        public DcMotor fr;

        /**
         * Control 2: backLeft
         */
        public DcMotor bl;

        /**
         * Control 1: backRight
         */
        public DcMotor br;


        /**
         * Expansion 0: intake
         */
        public DcMotor intake;

        /**
         * Expansion 3: output
         */
        public Motor output;


        /**
         * Control 5: preventer
         */
        public ServoEx preventer;


        /**
         * Internally connected
         */
        public IMUEx imu;

        public SparkFunOTOS otos;
    }

    /**
     * 4-Wheels MecanumDrive
     */
    public MecanumDrive drive;

    /**
     * Intake Actuator
     */
    public Actuator intake;

    /**
     * Output Actuator
     */
    public Actuator output;

    /**
     * Preventer Switch
     */
    public Switch preventer;

    public double kP = 1;
    public double kI = 0;
    public double kD = 0;
    public double kF = 0.98;

    public double outputPower = 0.7;
    public Measure<Time> outputChargeTime = Seconds.of(3);

    public final Hardware hw = new Hardware();

    /**
     * SequentialTaskGroup that launches the collected pollen.
     */
    public SequentialTaskGroup launch;

    /**
     * Variation on the launch SequentialTaskGroup that skips closing the preventer at the beginning,
     * charging the flywheel,
     * and closing the preventer at the end.
     * All these actions must be done outside of this SequentialTaskGroup for it to be used.
     */
    public SequentialTaskGroup quickLaunch;

    /**
     * ParallelTaskGroup that ejects all collected field elements out the intake.
     */
    public ParallelTaskGroup eject;

    //TODO: measure these in inches
    public double width = 18;
    public double length = 18;
    /**
     * Distance between the centre of the robot and its corners.
     */
    public double diagonal = Math.sqrt(Math.pow(width, 2) + Math.pow(length, 2));

    @Override
    protected void onRuntime() {
        hw.fl = getHardware("front_left_motor", DcMotorEx.class, (d) -> {
            d.setDirection(DcMotor.Direction.REVERSE);
            d.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        });
        hw.fr = getHardware("front_right_motor", DcMotorEx.class, (d) -> {
            d.setDirection(DcMotor.Direction.FORWARD);
            d.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        });
        hw.bl = getHardware("back_left_motor", DcMotorEx.class, (d) -> {
            d.setDirection(DcMotor.Direction.REVERSE);
            d.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        });
        hw.br = getHardware("back_right_motor", DcMotorEx.class, (d) -> {
            d.setDirection(DcMotor.Direction.FORWARD);
            d.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        });

        hw.intake = getHardware("intake_motor", DcMotorEx.class, (d) -> {
            d.setDirection(DcMotor.Direction.REVERSE);
            d.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        });
        hw.output = getHardware("shooter_motor", Motor.class, (d) -> {
            d.setDirection(DcMotor.Direction.FORWARD);
            d.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            d.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            PIDFController pidf = new PIDFController(kP, kI, kD, kF);
            d.setRunUsingEncoderController(1, 2100, pidf); // theoretically 2707 tps w/ no load

            BunyipsOpMode.ifRunning(o -> o.onActiveLoop(() -> {
                pidf.setCoefficients(kP, kI, kD, kF);

                o.telemetry.addData("currentVelocity", d.getVelocity());
                o.telemetry.addData("targetVelocity", pidf.getSetpoint());
            }));
        });

        hw.otos = getHardware("sensor_otos", SparkFunOTOS.class);

        hw.preventer = getHardware("kicker_servo", ServoEx.class, (d) -> {
            d.scaleRange(0, 1);
            d.setEndToEndTime(Seconds.of(0.8));
        });

        hw.imu = getHardware("imu", IMUEx.class, (d) ->
                d.lazyInitialize(new IMU.Parameters(new RevHubOrientationOnRobot(
                        RevHubOrientationOnRobot.LogoFacingDirection.LEFT,
                        RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
                ))));

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

        drive = new MecanumDrive(driveModel, motionProfile, mecanumGains, hw.fl, hw.bl, hw.br, hw.fr, hw.imu, hardwareMap.voltageSensor)
                .withLocalizer(new OTOSLocalizer(otosParams, hw.otos))
                .withName("drive");

        intake = new Actuator(hw.intake)
                .withName("intakeSub");

        output = new Actuator(hw.output)
                .withName("outputSub");

        preventer = new Switch(hw.preventer)
                .withOpenBound(0)
                .withClosedBound(1)
                .withName("preventerSub");

        launch = new SequentialTaskGroup(
                preventer.tasks.close(),
                output.tasks.runFor(outputChargeTime, outputPower),
                output.tasks.control(() -> outputPower).until(preventer.tasks.open()),
                output.tasks.runFor(Seconds.of(1.5), outputPower).with(intake.tasks.runFor(Seconds.of(1.5), 1)),
                preventer.tasks.close()
        );

        quickLaunch = new SequentialTaskGroup(
                output.tasks.control(() -> outputPower).until(preventer.tasks.open()),
                output.tasks.runFor(Seconds.of(1.5), outputPower).with(intake.tasks.runFor(Seconds.of(1.5), 1))
        );

        eject = new ParallelTaskGroup(
                intake.tasks.runFor(Seconds.of(2), -1)
        );
    }
}
