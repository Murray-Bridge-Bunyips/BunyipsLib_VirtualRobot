package au.edu.sa.mbhs.studentrobotics.bunyipslib.vision.processors;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Size;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.config.Config;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.Dbg;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.vision.Processor;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.vision.data.ContourData;

/**
 * Legacy colour thresholding processor for a colour space, used to find colour contours in an image.
 * This processor was used pre SDK 10.1 to detect contours, and may still be used if desired.
 *
 * @author Lucas Bubner, 2024
 * @see ColourLocator
 * @see ColourSensor
 * @since 1.0.0-pre
 */
@Config
public abstract class ColourThreshold extends Processor<ContourData> {
    /**
     * The thickness of the border to draw around the biggest contour.
     */
    public static int BIGGEST_CONTOUR_BORDER_THICKNESS = 6;
    /**
     * The thickness of the border to draw around all contours.
     */
    public static int CONTOUR_BORDER_THICKNESS = 3;
    // Default fields for convenience when using a mass of ColourThresholds
    // These cannot be tuned dynamically but can be overridden by subclasses
    protected static double DEFAULT_MIN_AREA = 1.2;
    protected static double DEFAULT_MAX_AREA = 10.0;
    /**
     * The colour space to use for thresholding.
     */
    public final ColourSpace colourSpace;
    private final Mat processingMat = new Mat();
    private final Mat binaryMat = new Mat();
    private final Mat maskedInputMat = new Mat();
    private final List<MatOfPoint> contours = new ArrayList<>();
    private final Mat hierarchy = new Mat();
    private Scalar lowerOverride = null;
    private Scalar upperOverride = null;

    /**
     * Defines a new colour thresholding processor for a specific colour space, which your
     * lower and upper scalars will be based on.
     *
     * @param colourSpace the colour space to use
     */
    @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
    public ColourThreshold(@NonNull ColourSpace colourSpace) {
        this.colourSpace = colourSpace;
    }

    public abstract double getContourAreaMinPercent();

    public abstract double getContourAreaMaxPercent();

    /**
     * Gets the upper scalar to use for thresholding.
     *
     * @return the upper scalar
     */
    @NonNull
    public Scalar getUpper() {
        return upperOverride == null ? setUpper() : upperOverride;
    }

    /**
     * Forces this lower scalar to be used instead of the scalar supplied by the subclass.
     *
     * @param upper the upper scalar to use
     */
    public void setUpper(@NonNull Scalar upper) {
        if (!upper.equals(upperOverride)) {
            Dbg.logd(getClass(), "Overriding upper scalar to " + upper + " from " + setUpper());
            upperOverride = upper;
        }
    }

    /**
     * Gets the lower scalar to use for thresholding.
     *
     * @return the lower scalar
     */
    @NonNull
    public Scalar getLower() {
        return lowerOverride == null ? setLower() : lowerOverride;
    }

    /**
     * Forces this upper scalar to be used instead of the scalar supplied by the subclass.
     *
     * @param lower the lower scalar to use
     */
    public void setLower(@NonNull Scalar lower) {
        if (!lower.equals(lowerOverride)) {
            Dbg.logd(getClass(), "Overriding lower scalar to " + lower + " from " + setLower());
            lowerOverride = lower;
        }
    }

    @NonNull
    protected abstract Scalar setLower();

    @NonNull
    protected abstract Scalar setUpper();

    public abstract int getBoxColour();

    /**
     * @return whether to show the masked black and white input on the screen
     */
    public abstract boolean showMaskedInput();

    /**
     * Resets the lower override, so that the lower scalar is used instead of the override.
     */
    public void resetLower() {
        if (lowerOverride != null)
            Dbg.logd(getClass(), "Resetting scalar to " + setLower());
        lowerOverride = null;
    }

    /**
     * Resets the upper override, so that the upper scalar is used instead of the override.
     */
    public void resetUpper() {
        if (upperOverride != null)
            Dbg.logd(getClass(), "Resetting scalar to " + setUpper());
        upperOverride = null;
    }

    @Override
    protected final void update() {
        Size cameraDimensions = getCameraDimensions();
        if (cameraDimensions == null) return;
        for (MatOfPoint contour : contours) {
            ContourData newData = new ContourData(cameraDimensions, Imgproc.boundingRect(contour));
            // Min-max bounding
            if (newData.getAreaPercent() < getContourAreaMinPercent() || newData.getAreaPercent() > getContourAreaMaxPercent())
                continue;
            data.add(newData);
        }
    }

    @Override
    protected final void onProcessFrame(@NonNull Mat frame, long captureTimeNanos) {
        /*
         * Converts our input mat from RGB to
         * specified color space by the enum.
         * EOCV ALWAYS returns RGB mats, so you'd
         * always convert from RGB to the color
         * space you want to use.
         *
         * Takes our "input" mat as an input, and outputs
         * to a separate Mat buffer "processingMat"
         */
        Imgproc.cvtColor(frame, processingMat, colourSpace.cvtCode);

        /*
         * This is where our thresholding actually happens.
         * Takes our "processingMat" as input and outputs a "binary"
         * Mat to "binaryMat" of the same size as our input.
         * "Discards" all the pixels outside the bounds specified
         * by the scalars "lower" and "upper".
         *
         * Binary meaning that we have either a 0 or 255 value
         * for every pixel.
         *
         * 0 represents our pixels that were outside the bounds
         * 255 represents our pixels that are inside the bounds
         */
        Core.inRange(
                processingMat,
                getLower(),
                getUpper(),
                binaryMat
        );

        /*
         * Release the reusable Mat so that old data doesn't
         * affect the next step in the current processing
         */
        maskedInputMat.release();

        /*
         * Now, with our binary Mat, we perform a "bitwise and"
         * to our input image, meaning that we will perform a mask
         * which will include the pixels from our input Mat which
         * are "255" in our binary Mat (meaning that they're inside
         * the range) and will discard any other pixel outside the
         * range (RGB 0, 0, 0. All discarded pixels will be black)
         */
        Core.bitwise_and(frame, frame, maskedInputMat, binaryMat);

        /*
         * Find the contours of the binary Mat. This will
         * populate the "contours" list with the contours
         * found in the binary Mat.
         */
        contours.clear();
        Imgproc.findContours(binaryMat, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        // Only show the detection matrix if we need to
        if (showMaskedInput())
            maskedInputMat.copyTo(frame);

        binaryMat.release();
        processingMat.release();
        hierarchy.release();
    }

    @Override
    protected final void onFrameDraw(@NonNull Canvas canvas) {
        // Draw borders around the contours, with a thicker border for the largest contour
        ContourData biggest = ContourData.getLargest(data);
        for (ContourData contour : data) {
            canvas.drawRect(
                    contour.getBoundingRect().x,
                    contour.getBoundingRect().y,
                    contour.getBoundingRect().x + contour.getBoundingRect().width,
                    contour.getBoundingRect().y + contour.getBoundingRect().height,
                    new Paint() {{
                        setColor(getBoxColour());
                        setStyle(Style.STROKE);
                        setStrokeWidth(contour == biggest ? BIGGEST_CONTOUR_BORDER_THICKNESS : CONTOUR_BORDER_THICKNESS);
                    }}
            );
        }
    }

    /**
     * The colour spaces that can be used for thresholding.
     */
    public enum ColourSpace {
        /*
         * Define our "conversion codes" in the enum
         * so that we don't have to do a switch
         * statement in the processFrame method.
         */

        /**
         * Red, Green, Blue.
         */
        RGB(Imgproc.COLOR_RGBA2RGB),
        /**
         * Hue, Saturation, Value.
         */
        HSV(Imgproc.COLOR_RGB2HSV),
        /**
         * Luminance, Chrominance.
         */
        YCrCb(Imgproc.COLOR_RGB2YCrCb),
        /**
         * Lightness, A, B.
         */
        Lab(Imgproc.COLOR_RGB2Lab);

        /**
         * OpenCV conversion code for the colour space.
         */
        public final int cvtCode;

        ColourSpace(int cvtCode) {
            this.cvtCode = cvtCode;
        }

        /**
         * Get a component (channel) name from the colour space at a given index (0-2).
         *
         * @param idx the index of the channel, 0-2
         * @return the name of the channel, or null if the index is out of bounds
         */
        @NonNull
        public final String getChannelName(int idx) {
            switch (this) {
                case RGB:
                    return new String[]{"Red", "Green", "Blue"}[idx];
                case HSV:
                    return new String[]{"Hue", "Saturation", "Value"}[idx];
                case YCrCb:
                    return new String[]{"Luminance", "Chrominance Red", "Chrominance Blue"}[idx];
                case Lab:
                    return new String[]{"Lightness", "A", "B"}[idx];
                default:
                    return "Unknown";
            }
        }
    }
}
