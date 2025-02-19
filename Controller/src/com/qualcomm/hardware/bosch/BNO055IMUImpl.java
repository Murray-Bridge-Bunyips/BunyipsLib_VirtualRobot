package com.qualcomm.hardware.bosch;

import org.firstinspires.ftc.robotcore.external.navigation.*;
import virtual_robot.controller.VirtualBot;

/**
 * Implementation of the BNO055IMU interface
 */
public class BNO055IMUImpl implements BNO055IMU {
    private VirtualBot bot = null;
    private Parameters parameters = null;
    private double initialHeadingRadians = 0;
    private double headingRadians = 0;
    private boolean initialized = false;

    private long latencyNanos = 0;
    private long prevNanos = System.nanoTime();

    public BNO055IMUImpl(VirtualBot bot){
        this.bot = bot;
    }

    public BNO055IMUImpl(VirtualBot bot, int latencyMillis){
        this.bot = bot;
        latencyNanos = latencyMillis * 1000000;
    }

    @Override
    public String getSystemStatus() {
        return "VIRTUAL ENABLE";
    }

    @Override
    public String getCalibrationStatus() {
        return "NOT REQ.";
    }

    @Override
    public String getSystemError() {
        return "NONE";
    }

    @Override
    public String isSystemCalibrated() {
        return "YES";
    }

    @Override
    public String isGyroCalibrated() {
        return "YES";
    }

    @Override
    public String isAccelerometerCalibrated() {
        return "YES";
    }

    @Override
    public String isMagnetometerCalibrated() {
        return "YES";
    }

    @Override
    public Position getPosition() {
        return new Position();
    }

    @Override
    public Velocity getVelocity() {
        return new Velocity();
    }

    @Override
    public String getAcceleration() {
        return "NA";
    }

    @Override
    public String getOverallAcceleration() {
        return "NA";
    }

    @Override
    public String getLinearAcceleration() {
        return "NA";
    }

    @Override
    public String getGravity() {
        return "9.80665";
    }

    @Override
    public String getAngularVelocity() {
        return "NONE";
    }

    @Override
    public Tempa getTemperature() {
        return new Tempa();
    }

    @Override
    public String getMagneticFieldStrength() {
        return "INFINITE";
    }

    @Override
    public void startAccelerationIntegration(Position a, Velocity b, int g) {

    }

    /**
     * Initialize the BNO055IMU
     * @param parameters Parameters object
     * @return true to indicate initialization was successful
     */
    public synchronized boolean initialize(Parameters parameters){
        initialized = true;
        this.parameters = parameters;
        double tempHeadingRadians = bot.getHeadingRadians();
        headingRadians = tempHeadingRadians;
        initialHeadingRadians = tempHeadingRadians;
        return true;
    }

    public synchronized Parameters getParameters() { return parameters; }

    /**
     * Close the BNO055IMU
     */
    public synchronized void close(){
        initialized = false;
        headingRadians = 0;
        initialHeadingRadians = 0;
    }

    /**
     * Get the angular orientation (as an Orientation object), using the AxesReference, AxesOrder, and AngleUnit
     * specified by the imu's Parameters object
     * @return angular orientation
     */
    public synchronized Orientation getAngularOrientation() {
        org.firstinspires.ftc.robotcore.external.navigation.AngleUnit angleUnit = parameters.angleUnit == AngleUnit.DEGREES ?
                org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES :
                org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.RADIANS;
        return getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, angleUnit);
    }

    /**
     * Get the angular orientation (as an Orientation object), using the AxesReference, AxesOrder, and AngleUnit
     * specified by the arguments
     * @param reference axes reference
     * @param order axes order
     * @param angleUnit angle unit
     * @return angular orientation
     */
    public synchronized Orientation getAngularOrientation(AxesReference reference, AxesOrder order, org.firstinspires.ftc.robotcore.external.navigation.AngleUnit angleUnit) {
        if (!initialized) return null;

        double heading = headingRadians - initialHeadingRadians;
        if (heading > Math.PI) heading -= 2.0 * Math.PI;
        else if (heading < -Math.PI) heading += 2.0 * Math.PI;

        double piOver2;
        double firstAngle = 0.0, secondAngle = 0.0, thirdAngle = 0.0;
        if (angleUnit == org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES) {
            heading *= 180.0 / Math.PI;
            piOver2 = Math.PI / 2.0;
        } else {
            piOver2 = 90.0;
        }

        switch (order) {
            case ZXY: case ZXZ: case ZYX: case ZYZ:
                firstAngle = heading;
                break;
            case XZX: case XZY: case YZX: case YZY:
                secondAngle = heading;
                break;
            case XYZ: case YXZ:
                thirdAngle = heading;
                break;
            case YXY:
                secondAngle = heading;
                if (reference == AxesReference.INTRINSIC){
                    firstAngle = -piOver2;
                    thirdAngle = piOver2;
                } else {
                    firstAngle = piOver2;
                    thirdAngle = -piOver2;
                }
                break;
            case XYX:
                secondAngle = heading;
                if (reference == AxesReference.INTRINSIC){
                    firstAngle = piOver2;
                    thirdAngle = -piOver2;
                } else {
                    firstAngle = -piOver2;
                    thirdAngle = piOver2;
                }
        }
        return new Orientation(reference, order, angleUnit, (float)firstAngle, (float)secondAngle, (float)thirdAngle,
                System.nanoTime());

    }

    /**
     * For internal use only
     * @param heading
     */
    public synchronized void updateHeadingRadians( double heading ){
        if (!initialized) return;
        long nanos = System.nanoTime();
        if (nanos < (prevNanos + latencyNanos)) return;
        headingRadians = heading;
        prevNanos = nanos;
    }
}
