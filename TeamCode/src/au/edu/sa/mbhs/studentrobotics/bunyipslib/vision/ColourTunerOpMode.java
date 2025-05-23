package au.edu.sa.mbhs.studentrobotics.bunyipslib.vision;


import static au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Units.Milliseconds;

import androidx.annotation.NonNull;

import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraName;
import org.opencv.core.Scalar;

import java.util.function.Supplier;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.BunyipsOpMode;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.Mathf;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.UnaryFunction;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.transforms.Controls;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Dashboard;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Dbg;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Exceptions;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Text;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.vision.processors.ColourThreshold;

/**
 * A tuning OpMode for calibrating the vision system's colour thresholding using controller input.
 * <p>
 * Note that if you want to tune a processor using FtcDashboard, you have to use the static fields
 * offered by ColourTuner, not the processor (as it will be overridden and controlled by the controllers)
 *
 * @author Lucas Bubner, 2024
 * @since 3.4.0
 */
public abstract class ColourTunerOpMode extends BunyipsOpMode {
    /**
     * The scalars used for the current processor.
     */
    @NonNull
    public static double[] scalars = new double[6];

    private final String[] channelNames = new String[3];
    private ColourThreshold[] processors;
    private int processorIdx;
    private int scalarIdx;
    private Vision vision;

    /**
     * The camera from HardwareMap to use for vision processing.
     *
     * @return the camera to use
     */
    @NonNull
    protected abstract CameraName setCamera();

    /**
     * The processors to tune in this OpMode.
     *
     * @return the processors to tune, will be able to switch between them during runtime
     */
    @NonNull
    protected abstract ColourThreshold[] setThresholdsToTune();

    @Override
    protected final void onInit() {
        telemetry.setAutoClear(true);
        // Using quadratic scaling for the scalar adjustment for more precision
        gamepad1.set(Controls.Analog.LEFT_STICK_Y, UnaryFunction.SQUARE_KEEP_SIGN);
        // Restrict speed so that adjustment is consistent
        setLoopSpeed(Milliseconds.of(10));
        telemetry.setMsTransmissionInterval(25);
        vision = new Vision(setCamera());
        processors = setThresholdsToTune();
        if (processors.length == 0)
            throw new Exceptions.EmergencyStop("No processors to tune");
        // Initialise all processors but start only one
        vision.init(processors);
        vision.startPreview();
        changeToProcessor(0);
        Dashboard.enableConfig(getClass());
    }

    @Override
    protected final boolean onInitLoop() {
        activeLoop();
        return false;
    }

    private void changeToProcessor(int index) {
        // Stop old processor
        vision.stop(processors[processorIdx]);
        // Repopulate scalars with new processor's scalars
        for (int i = 0; i < 3; i++) {
            Supplier<Scalar> lowerThreshold = processors[index].lowerThreshold;
            Supplier<Scalar> upperThreshold = processors[index].upperThreshold;
            assert lowerThreshold != null;
            assert upperThreshold != null;
            scalars[i] = lowerThreshold.get().val[i];
            scalars[i + 3] = upperThreshold.get().val[i];
        }
        // Start new processor
        vision.start(processors[index]);
        vision.setPreview(processors[index]);
        // Cache the namespaces of the channel (e.g. "Red", "Green", "Blue")
        for (int i = 0; i < 3; i++) {
            Supplier<ColourThreshold.ColourSpace> colourSpace = processors[index].colourSpace;
            assert colourSpace != null;
            channelNames[i] = colourSpace.get().getChannelName(i);
        }
    }

    @Override
    protected final void activeLoop() {
        // Scalar selection
        if (gamepad1.getDebounced(Controls.DPAD_DOWN)) {
            scalarIdx++;
            if (scalarIdx > 5)
                scalarIdx = 0;
        } else if (gamepad1.getDebounced(Controls.DPAD_UP)) {
            scalarIdx--;
            if (scalarIdx < 0)
                scalarIdx = 5;
        }

        // Processor selection
        if (gamepad1.getDebounced(Controls.RIGHT_BUMPER)) {
            processorIdx++;
            if (processorIdx >= processors.length)
                processorIdx = 0;
            changeToProcessor(processorIdx);
        } else if (gamepad1.getDebounced(Controls.LEFT_BUMPER)) {
            processorIdx--;
            if (processorIdx < 0)
                processorIdx = processors.length - 1;
            changeToProcessor(processorIdx);
        }

        // Scalar adjustment
        scalars[scalarIdx] -= gamepad1.lsy / 6.0;
        scalars[scalarIdx] = Mathf.clamp(Mathf.round(scalars[scalarIdx], 2), 0, 255);

        telemetry.add("LB/RB: Select processor to tune").small();
        telemetry.add("Dpad Up: Move scalar index up").small();
        telemetry.add("Dpad Down: Move scalar index down").small();
        telemetry.add("LStick Y: Adjust selected scalar").small();
        telemetry.add("A: Print current values to Logcat").small();
        telemetry.addNewLine();

        telemetry.add("Tuning '%'", Text.html().color("cyan", processors[processorIdx].toString())).big().bold();

        // Display scalars and highlight the selected scalar
        for (int i = 0; i < 6; i++) {
            int finalI = i;
            if (i == 3)
                telemetry.addNewLine();
            telemetry.add("%[%]: %", i < 3 ? "Lower" : "Upper", channelNames[i % 3], scalars[i], 2)
                    .color("green").bold().applyStylesIf(() -> scalarIdx == finalI);
        }

        // Update the processor's scalars
        processors[processorIdx].setLowerThreshold(new Scalar(scalars[0], scalars[1], scalars[2]));
        processors[processorIdx].setUpperThreshold(new Scalar(scalars[3], scalars[4], scalars[5]));

        // Save functionality
        if (gamepad1.getDebounced(Controls.A)) {
            String lower = Text.format("['%' lower] Scalar(%, %, %)", processors[processorIdx], scalars[0], scalars[1], scalars[2]);
            String upper = Text.format("['%' upper] Scalar(%, %, %)", processors[processorIdx], scalars[3], scalars[4], scalars[5]);
            Dbg.log(lower + "\n" + upper);
            telemetry.log(lower + "\n" + upper);
        }
    }
}
