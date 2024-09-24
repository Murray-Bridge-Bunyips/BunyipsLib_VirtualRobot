package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

// portination for bunyips library
public class DcMotorImplEx implements DcMotorEx {
    public final MotorConfigurationType motorType;
    private final DcMotorControllerImpl con;
    private final int port;

    public DcMotorImplEx(DcMotorController controller, int port, Direction direction, MotorConfigurationType type){
        this.port = port;
        con = (DcMotorControllerImpl) controller;
        motorType = type;
        setDirection(direction);
    }

    @Override
    public void setMotorEnable() {
        con.getMotor(port).setMotorEnable();
    }

    @Override
    public void setMotorDisable() {
        con.getMotor(port).setMotorDisable();
    }

    @Override
    public boolean isMotorEnabled() {
        return con.getMotor(port).isMotorEnabled();
    }

    @Override
    public void setVelocity(double angularRate) {
        con.getMotor(port).setVelocity(angularRate);
    }

    @Override
    public void setVelocity(double angularRate, AngleUnit unit) {
        con.getMotor(port).setVelocity(angularRate, unit);
    }

    @Override
    public double getVelocity() {
        return con.getMotor(port).getVelocity();
    }

    @Override
    public double getVelocity(AngleUnit unit) {
        return con.getMotor(port).getVelocity(unit);
    }

    @Override
    public void setPIDCoefficients(RunMode mode, PIDCoefficients pidCoefficients) {
        con.getMotor(port).setPIDCoefficients(mode, pidCoefficients);
    }

    @Override
    public void setPIDFCoefficients(RunMode mode, PIDFCoefficients pidfCoefficients) throws UnsupportedOperationException {
        con.getMotor(port).setPIDFCoefficients(mode, pidfCoefficients);
    }

    @Override
    public void setVelocityPIDFCoefficients(double p, double i, double d, double f) {
        con.getMotor(port).setVelocityPIDFCoefficients(p, i, d, f);
    }

    @Override
    public void setPositionPIDFCoefficients(double p) {
        con.getMotor(port).setPositionPIDFCoefficients(p);
    }

    @Override
    public PIDCoefficients getPIDCoefficients(RunMode mode) {
        return con.getMotor(port).getPIDCoefficients(mode);
    }

    @Override
    public PIDFCoefficients getPIDFCoefficients(RunMode mode) {
        return con.getMotor(port).getPIDFCoefficients(mode);
    }

    @Override
    public void setTargetPositionTolerance(int tolerance) {
        con.getMotor(port).setTargetPositionTolerance(tolerance);
    }

    @Override
    public int getTargetPositionTolerance() {
        return con.getMotor(port).getTargetPositionTolerance();
    }

    @Override
    public double getCurrent(CurrentUnit unit) {
        return con.getMotor(port).getCurrent(unit);
    }

    @Override
    public double getCurrentAlert(CurrentUnit unit) {
        return con.getMotor(port).getCurrentAlert(unit);
    }

    @Override
    public void setCurrentAlert(double current, CurrentUnit unit) {
        con.getMotor(port).setCurrentAlert(current, unit);
    }

    @Override
    public boolean isOverCurrent() {
        return con.getMotor(port).isOverCurrent();
    }

    @Override
    public void setMode(RunMode mode) {
        con.getMotor(port).setMode(mode);
    }

    @Override
    public RunMode getMode() {
        return con.getMotor(port).getMode();
    }

    @Override
    public int getCurrentPosition() {
        return con.getMotor(port).getCurrentPosition();
    }

    @Override
    public void setTargetPosition(int pos) {
        con.getMotor(port).setTargetPosition(pos);
    }

    @Override
    public int getTargetPosition() {
        return con.getMotor(port).getTargetPosition();
    }

    @Override
    public boolean isBusy() {
        return con.getMotor(port).isBusy();
    }

    @Override
    public void setZeroPowerBehavior(ZeroPowerBehavior zeroPowerBehavior) {
        con.getMotor(port).setZeroPowerBehavior(zeroPowerBehavior);
    }

    @Override
    public ZeroPowerBehavior getZeroPowerBehavior() {
        return con.getMotor(port).getZeroPowerBehavior();
    }

    @Override
    public MotorConfigurationType getMotorType() {
        return motorType;
    }

    @Override
    public DcMotorController getController() {
        return con;
    }

    @Override
    public int getPortNumber() {
        return port;
    }

    @Override
    public void setDirection(Direction direction) {
        con.getMotor(port).setDirection(direction);
    }

    @Override
    public Direction getDirection() {
        return con.getMotor(port).getDirection();
    }

    @Override
    public void setPower(double power) {
        con.getMotor(port).setPower(power);
    }

    @Override
    public double getPower() {
        return con.getMotor(port).getPower();
    }
}
