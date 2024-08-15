package org.murraybridgebunyips.bunyipslib;

import com.qualcomm.robotcore.hardware.*;

import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.murraybridgebunyips.bunyipslib.external.units.Measure;
import org.murraybridgebunyips.bunyipslib.external.units.Time;

/**
 * Wrapper class for a DcMotorEx where all motor speed is passed through a configurable SmoothDamp function.
 *
 * @author Lucas Bubner, 2024
 */
public class DcMotorRamping /* extends DcMotorImplEx */ implements RampingFunction, DcMotorEx {
    private final DcMotorEx __VIRTUAL_MOTOR;
    private final RampingSupplier v;

    /**
     * Create a new DcMotorRamping object, wrapping a DcMotor object and
     * returning a new object that can be used in place of the original.
     * By default, the ramping function time is set to 0.1 seconds, with a maximum delta of 1.0 power units per second.
     *
     * @param motor the DcMotor object to wrap. By default, the ramping function is enabled.
     */
    public DcMotorRamping(DcMotorEx motor) {
//        super(motor.getController(), motor.getPortNumber(), motor.getDirection(), motor.getMotorType());
        this.__VIRTUAL_MOTOR = motor;
        v = new RampingSupplier(__VIRTUAL_MOTOR::getPower);
    }

    /**
     * Create a new DcMotorRamping object, wrapping a DcMotor object and
     * returning a new object that can be used in place of the original.
     *
     * @param motor      the DcMotor object to wrap. By default, the ramping function is enabled.
     * @param smoothTime the time it takes for the motor to reach the target power level
     * @param maxDelta   the maximum change in power level per second
     */
    public DcMotorRamping(DcMotorEx motor, Measure<Time> smoothTime, double maxDelta) {
//        super(motor.getController(), motor.getPortNumber());
        this.__VIRTUAL_MOTOR = motor;
        v = new RampingSupplier(__VIRTUAL_MOTOR::getPower);
        v.setRampingParameters(smoothTime, maxDelta);
    }

    /**
     * Set whether the ramping function is enabled.
     * If disabled, the motor will instantly set its power level to the target power level.
     *
     * @param enabled whether the ramping function is enabled, default is on
     */
    @Override
    public DcMotorRamping setRampingEnabled(boolean enabled) {
        v.setRampingEnabled(enabled);
        return this;
    }

    /**
     * Set the ramping parameters of the motor.
     *
     * @param smoothTime the time it takes for the motor to reach the target power level
     * @param maxDelta   the maximum change in power level per second
     */
    @Override
    public DcMotorRamping setRampingParameters(Measure<Time> smoothTime, double maxDelta) {
        v.setRampingParameters(smoothTime, maxDelta);
        return this;
    }

    /**
     * Set the time it takes for the motor to reach the target power level.
     *
     * @param smoothTime the time it takes for the motor to reach the target power level
     */
    @Override
    public DcMotorRamping setRampingTime(Measure<Time> smoothTime) {
        v.setRampingTime(smoothTime);
        return this;
    }

    /**
     * Set the maximum change in power level per second.
     *
     * @param maxDelta the maximum change in power level per second
     */
    @Override
    public DcMotorRamping setMaxDelta(double maxDelta) {
        v.setMaxDelta(maxDelta);
        return this;
    }

    @Override
    public void setDirection(Direction direction) {
        __VIRTUAL_MOTOR.setDirection(direction);
    }

    @Override
    public Direction getDirection() {
        return __VIRTUAL_MOTOR.getDirection();
    }

    /**
     * Set the power level of the motor, which will be passed through a SmoothDamp function defined by the motor's ramping parameters.
     * <b>This function must be called periodically</b> (e.g. constantly during an activeLoop) to update the motor's power level,
     * as it relies on the time since the last call to calculate the new power level.
     *
     * @param power the new power level of the motor, a value in the interval [-1.0, 1.0]
     */
//    @Override
    public void setPower(double power) {
        __VIRTUAL_MOTOR.setPower(v.get(power));
    }

    @Override
    public double getPower() {
        return 0;
    }

    /**
     * Instantly set the power level of the motor, bypassing the SmoothDamp function.
     */
    public void setPowerInstant(double power) {
        __VIRTUAL_MOTOR.setPower(power);
    }

    /**
     * Instantly stop the motor, bypassing the SmoothDamp function.
     */
    public void stop() {
        setPowerInstant(0);
    }

    @Override
    public void setMode(RunMode mode) {
        __VIRTUAL_MOTOR.setMode(mode);
    }

    @Override
    public RunMode getMode() {
        return __VIRTUAL_MOTOR.getMode();
    }

    @Override
    public DcMotorController getController() {
        return __VIRTUAL_MOTOR.getController();
    }

    @Override
    public int getCurrentPosition() {
        return __VIRTUAL_MOTOR.getCurrentPosition();
    }

    @Override
    public void setTargetPosition(int pos) {
        __VIRTUAL_MOTOR.setTargetPosition(pos);
    }

    @Override
    public int getTargetPosition() {
        return __VIRTUAL_MOTOR.getTargetPosition();
    }

    @Override
    public boolean isBusy() {
        return __VIRTUAL_MOTOR.isBusy();
    }

    @Override
    public void setZeroPowerBehavior(ZeroPowerBehavior zeroPowerBehavior) {
        __VIRTUAL_MOTOR.setZeroPowerBehavior(zeroPowerBehavior);
    }

    @Override
    public ZeroPowerBehavior getZeroPowerBehavior() {
        return __VIRTUAL_MOTOR.getZeroPowerBehavior();
    }

    @Override
    public MotorConfigurationType getMotorType() {
        return __VIRTUAL_MOTOR.getMotorType();
    }

    @Override
    public void setMotorEnable() {

    }

    @Override
    public void setMotorDisable() {

    }

    @Override
    public boolean isMotorEnabled() {
        return false;
    }

    @Override
    public void setVelocity(double angularRate) {

    }

    @Override
    public void setVelocity(double angularRate, AngleUnit unit) {

    }

    @Override
    public double getVelocity() {
        return 0;
    }

    @Override
    public double getVelocity(AngleUnit unit) {
        return 0;
    }

    @Override
    public void setPIDCoefficients(RunMode mode, PIDCoefficients pidCoefficients) {

    }

    @Override
    public void setPIDFCoefficients(RunMode mode, PIDFCoefficients pidfCoefficients) throws UnsupportedOperationException {

    }

    @Override
    public void setVelocityPIDFCoefficients(double p, double i, double d, double f) {

    }

    @Override
    public void setPositionPIDFCoefficients(double p) {

    }

    @Override
    public PIDCoefficients getPIDCoefficients(RunMode mode) {
        return null;
    }

    @Override
    public PIDFCoefficients getPIDFCoefficients(RunMode mode) {
        return null;
    }

    @Override
    public void setTargetPositionTolerance(int tolerance) {

    }

    @Override
    public int getTargetPositionTolerance() {
        return 0;
    }

    @Override
    public double getCurrent(CurrentUnit unit) {
        return 0;
    }

    @Override
    public double getCurrentAlert(CurrentUnit unit) {
        return 0;
    }

    @Override
    public void setCurrentAlert(double current, CurrentUnit unit) {

    }

    @Override
    public boolean isOverCurrent() {
        return false;
    }
}