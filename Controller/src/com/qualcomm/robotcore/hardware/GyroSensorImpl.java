package com.qualcomm.robotcore.hardware;

import virtual_robot.controller.VirtualBot;

/**
 * Implementation of the GyroSensor interface
 */
public class GyroSensorImpl implements GyroSensor {
    private VirtualBot bot;
    private boolean initialized = false;
    private double initialHeading = 0.0;
    private double heading = 0.0;

    private long latencyNanos = 0;
    private long prevNanos = System.nanoTime();

    /**
     * For internal use only.
     * @param bot
     */
    public GyroSensorImpl(VirtualBot bot){
        this.bot = bot;
    }

    public GyroSensorImpl(VirtualBot bot, int latencyMillis){
        this.bot = bot;
        latencyNanos = latencyMillis * 1000000;
    }

    /**
     * Initialize the gyro sensor
     */
    public synchronized void init(){
        initialized = true;
        double tempHeading = bot.getHeadingRadians() * 180.0 / Math.PI;
        heading = tempHeading;
        initialHeading = tempHeading;
    }

    /**
     * For internal use only.
     */
    public synchronized void deinit(){
        initialized = false;
        initialHeading = 0.0;
        heading = 0.0;
    }

    @Override
    public void calibrate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCalibrating() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get heading in degrees (-180 to 180)
     * @return heading in degrees (-180 to 180)
     */
    public synchronized int getHeading(){
        if (initialized){
            double result = heading - initialHeading;
            if (result < -180.0) result += 360.0;
            else if (result > 180.0) result -= 360.0;
            return (int) result;
        }
        else return 0;
    }

    @Override
    public double getRotationFraction() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int rawX() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int rawY() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int rawZ() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetZAxisIntegrator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String status() {
        return "VIRTUAL ROBOT GYRO SENSOR ON";
    }

    /**
     * For internal use only.
     * @param heading
     */
    public synchronized void updateHeading(double heading){
        long nanos = System.nanoTime();
        if (nanos < (prevNanos + latencyNanos)) return;
        this.heading = heading;
        prevNanos = nanos;
    }
}
