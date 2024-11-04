package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import virtual_robot.controller.VirtualBot;

import java.util.Arrays;

public class DcMotorControllerImpl implements DcMotorControllerEx{

    DcMotorEx[] motors = new DcMotorEx[8];

    public void setMotor(int n, DcMotor motor){
        motors[n] = (DcMotorEx) motor;
    }

    public DcMotorEx getMotor(int n){
        return motors[n];
    }

    public DcMotorControllerImpl(){
        for (int n = 0; n<motors.length; n++){
            motors[n] = null;
        }
    }

    @Override
    public void setMotorType(int motor, MotorConfigurationType motorType) {
        motors[motor].setMotorType(motorType);
    }

    @Override
    public MotorConfigurationType getMotorType(int motor) {
        return motors[motor].getMotorType();
    }

    @Override
    public void setMotorMode(int motor, DcMotor.RunMode mode) {
        motors[motor].setMode(mode);
    }

    @Override
    public DcMotor.RunMode getMotorMode(int motor) {
        return motors[motor].getMode();
    }

    @Override
    public void setMotorPower(int motor, double power) {
        motors[motor].setPower(power);
    }

    @Override
    public double getMotorPower(int motor) {
        return motors[motor].getPower();
    }

    @Override
    public boolean isBusy(int motor) {
        return motors[motor].isBusy();
    }

    @Override
    public void setMotorZeroPowerBehavior(int motor, DcMotor.ZeroPowerBehavior zeroPowerBehavior) {
        motors[motor].setZeroPowerBehavior(zeroPowerBehavior);
    }

    @Override
    public DcMotor.ZeroPowerBehavior getMotorZeroPowerBehavior(int motor) {
        return motors[motor].getZeroPowerBehavior();
    }

    @Override
    public boolean getMotorPowerFloat(int motor) {
        return motors[motor].getZeroPowerBehavior() == DcMotor.ZeroPowerBehavior.FLOAT;
    }

    @Override
    public void setMotorTargetPosition(int motor, int position) {
        motors[motor].setTargetPosition(position);
    }

    @Override
    public int getMotorTargetPosition(int motor) {
        return motors[motor].getTargetPosition();
    }

    @Override
    public int getMotorCurrentPosition(int motor) {
        // bunyipslib change: motor controllers dont reverse outputs at the controller level. we invert it to avoid this problem.
        //    this also makes sure the motors reflect what actually happens on a robot
        return motors[motor].getCurrentPosition() * (motors[motor].getDirection() == DcMotorSimple.Direction.FORWARD ? 1 : -1);
    }

    @Override
    public void resetDeviceConfigurationForOpMode(int motor) {
        motors[motor].resetDeviceConfigurationForOpMode();
    }

    @Override
    public void setMotorEnable(int motor) {
        motors[motor].setMotorEnable();
    }

    @Override
    public void setMotorDisable(int motor) {
        motors[motor].setMotorDisable();
    }

    @Override
    public boolean isMotorEnabled(int motor) {
        return motors[motor].isMotorEnabled();
    }

    @Override
    public void setMotorVelocity(int motor, double ticksPerSecond) {
        motors[motor].setVelocity(ticksPerSecond);
    }

    @Override
    public void setMotorVelocity(int motor, double angularRate, AngleUnit unit) {
        motors[motor].setVelocity(angularRate, unit);
    }

    @Override
    public double getMotorVelocity(int motor) {
        return motors[motor].getVelocity() * (motors[motor].getDirection() == DcMotorSimple.Direction.FORWARD ? 1 : -1);
    }

    @Override
    public double getMotorVelocity(int motor, AngleUnit unit) {
        return motors[motor].getVelocity(unit);
    }

    @Override
    public void setPIDCoefficients(int motor, DcMotor.RunMode mode, PIDCoefficients pidCoefficients) {
        motors[motor].setPIDCoefficients(mode, pidCoefficients);
    }

    @Override
    public void setPIDFCoefficients(int motor, DcMotor.RunMode mode, PIDFCoefficients pidfCoefficients) throws UnsupportedOperationException {
        motors[motor].setPIDFCoefficients(mode, pidfCoefficients);
    }

    @Override
    public PIDCoefficients getPIDCoefficients(int motor, DcMotor.RunMode mode) {
        return motors[motor].getPIDCoefficients(mode);
    }

    @Override
    public PIDFCoefficients getPIDFCoefficients(int motor, DcMotor.RunMode mode) {
        return motors[motor].getPIDFCoefficients(mode);
    }

    @Override
    public void setMotorTargetPosition(int motor, int position, int tolerance) {
        motors[motor].setTargetPositionTolerance(tolerance);
        motors[motor].setTargetPosition(position);
    }

    @Override
    public double getMotorCurrent(int motor, CurrentUnit unit) {
        return motors[motor].getCurrent(unit);
    }

    @Override
    public double getMotorCurrentAlert(int motor, CurrentUnit unit) {
        return motors[motor].getCurrentAlert(unit);
    }

    @Override
    public void setMotorCurrentAlert(int motor, double current, CurrentUnit unit) {
        motors[motor].setCurrentAlert(current, unit);
    }

    @Override
    public boolean isMotorOverCurrent(int motor) {
        return motors[motor].isOverCurrent();
    }
}
