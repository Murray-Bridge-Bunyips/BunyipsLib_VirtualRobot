package au.edu.sa.mbhs.studentrobotics.virtual.imposter.teleop;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.BunyipsOpMode;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.subsystems.drive.MecanumDrive;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.transforms.Controls;
import au.edu.sa.mbhs.studentrobotics.virtual.imposter.components.ImposterConfig;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp
public class ImposterDrive extends BunyipsOpMode {
    private final ImposterConfig config = new ImposterConfig();
    private MecanumDrive drive;

    @Override
    protected void onInit() {
        config.init();
        drive = new MecanumDrive(config.driveModel, config.motionProfile, config.mecanumGains, config.front_left_motor, config.back_left_motor, config.back_right_motor, config.front_right_motor, config.lazyImu, hardwareMap.voltageSensor);
    }

    @Override
    protected void activeLoop() {
        drive.setPower(Controls.makeRobotVel(gamepad1.lsx, gamepad1.lsy, gamepad1.rsx));
        drive.update();
    }
}
