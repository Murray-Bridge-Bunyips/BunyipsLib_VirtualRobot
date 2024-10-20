package au.edu.sa.mbhs.studentrobotics.virtual.imposter.teleop;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.CommandBasedBunyipsOpMode;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.localization.MecanumLocalizer;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.subsystems.drive.MecanumDrive;
import au.edu.sa.mbhs.studentrobotics.virtual.imposter.components.ImposterConfig;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp
public class ImposterDrive extends CommandBasedBunyipsOpMode {
    private final ImposterConfig config = new ImposterConfig();
    private MecanumDrive drive;

    @Override
    protected void onInitialise() {
        config.init();
        drive = new MecanumDrive(config.driveModel, config.motionProfile, config.mecanumGains, config.front_left_motor, config.back_left_motor, config.back_right_motor, config.front_right_motor, config.lazyImu, hardwareMap.voltageSensor);
        MecanumLocalizer l = (MecanumLocalizer) drive.getLocalizer();
        l.leftFront.setDirection(DcMotorSimple.Direction.REVERSE);
        l.leftBack.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    @Override
    protected void assignCommands() {
        
    }
}
