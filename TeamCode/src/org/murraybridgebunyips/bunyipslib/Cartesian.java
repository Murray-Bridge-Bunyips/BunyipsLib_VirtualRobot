package org.murraybridgebunyips.bunyipslib;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import org.murraybridgebunyips.bunyipslib.external.units.Angle;
import org.murraybridgebunyips.bunyipslib.external.units.Measure;

import static org.murraybridgebunyips.bunyipslib.external.units.Units.Radians;

/**
 * Rotational utilities for Pose2d and Vector2d.
 *
 * @author Lucas Bubner, 2023
 */
public final class Cartesian {
    private Cartesian() {
    }

    /**
     * Rotate a Cartesian vector.
     *
     * @param cartesianVec the vector to rotate, must be in a Cartesian vector
     * @param rot the angle to rotate these points around by
     * @return the rotated Cartesian pose
     */
    public static Vector2d rotate(Vector2d cartesianVec, Measure<Angle> rot) {
        double t = rot.in(Radians);
        return new Vector2d(
                cartesianVec.getX() * Math.cos(t) - cartesianVec.getY() * Math.sin(t),
                cartesianVec.getX() * Math.sin(t) + cartesianVec.getY() * Math.cos(t)
        );
    }

    /**
     * @param pose the pose to convert
     * @return the pose with the x and y coordinates swapped and the heading negated
     */
    public static Pose2d toPose(Pose2d pose) {
        return new Pose2d(pose.getY(), -pose.getX(), -pose.getHeading());
    }

    /**
     * @param pose the pose to convert
     * @return the pose with the x and y coordinates swapped and the heading negated
     */
    public static Pose2d fromPose(Pose2d pose) {
        return new Pose2d(-pose.getY(), pose.getX(), -pose.getHeading());
    }

    /**
     * @param x       the x coordinate
     * @param y       the y coordinate
     * @param heading the heading
     * @return the pose with the x and y coordinates swapped and the heading negated
     */
    public static Pose2d toPose(double x, double y, double heading) {
        // noinspection SuspiciousNameCombination
        return new Pose2d(y, -x, -heading);
    }

    /**
     * @param x       the x coordinate
     * @param y       the y coordinate
     * @param heading the heading
     * @return the pose with the x and y coordinates swapped and the heading negated
     */
    public static Pose2d fromPose(double x, double y, double heading) {
        // noinspection SuspiciousNameCombination
        return new Pose2d(-y, x, -heading);
    }

    /**
     * @param vector the vector to convert
     * @return the vector with the x and y coordinates swapped
     */
    public static Vector2d toVector(Vector2d vector) {
        return new Vector2d(vector.getY(), -vector.getX());
    }

    /**
     * @param vector the vector to convert
     * @return the vector with the x and y coordinates swapped
     */
    public static Vector2d fromVector(Vector2d vector) {
        return new Vector2d(-vector.getY(), vector.getX());
    }

    /**
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the vector with the x and y coordinates swapped
     */
    public static Vector2d toVector(double x, double y) {
        // noinspection SuspiciousNameCombination
        return new Vector2d(y, -x);
    }

    /**
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the vector with the x and y coordinates swapped
     */
    public static Vector2d fromVector(double x, double y) {
        // noinspection SuspiciousNameCombination
        return new Vector2d(-y, x);
    }
}
