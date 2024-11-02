package au.edu.sa.mbhs.studentrobotics.bunyipslib.vision.data

import android.util.Size
import org.opencv.core.RotatedRect

/**
 * Data class for storing contour data from a ColourThreshold.
 * @since 1.0.0-pre
 */
data class ContourData(
    /**
     * The rectangle representing this contour.
     */
    val rect: RotatedRect,
    /**
     * The area of the contour.
     */
    val area: Double,
    /**
     * The percentage of the screen the contour takes up.
     */
    val areaPercent: Double,
    /**
     * The aspect ratio of the contour.
     */
    val aspectRatio: Double,
    /**
     * The x coordinate of the center of the contour.
     */
    val centerX: Double,
    /**
     * The y coordinate of the center of the contour.
     */
    val centerY: Double,
    /**
     * The measured yaw of the contour.
     */
    val yaw: Double,
    /**
     * The measured pitch of the contour.
     */
    val pitch: Double,
    /**
     * The measured angle of the contour in degrees.
     */
    val angDeg: Double,
) : VisionData() {
    constructor(cameraResolution: Size, rect: RotatedRect) : this(
        rect,
        rect.boundingRect().area(),
        rect.boundingRect().area() / (cameraResolution.width * cameraResolution.height) * 100.0,
        rect.boundingRect().width.toDouble() / rect.boundingRect().height.toDouble(),
        rect.boundingRect().x + rect.boundingRect().width / 2.0,
        rect.boundingRect().y + rect.boundingRect().height / 2.0,
        (((rect.boundingRect().x + rect.boundingRect().width / 2.0) - cameraResolution.width / 2.0) / (cameraResolution.width / 2.0)),
        -(((rect.boundingRect().y + rect.boundingRect().height / 2.0) - cameraResolution.height / 2.0) / (cameraResolution.height / 2.0)),
        ang(rect.angle, rect.size.width, rect.size.height)
    )

    companion object {
        /**
         * Get the largest contour from a list of contours.
         */
        @JvmStatic
        fun getLargest(contours: List<ContourData>): ContourData? {
            return contours.maxByOrNull { it.area }
        }

        private fun ang(angDeg: Double, width: Double, height: Double): Double {
            var ang = angDeg
            if (width < height)
                ang += 90.0
            return -ang + 180.0
        }
    }
}
