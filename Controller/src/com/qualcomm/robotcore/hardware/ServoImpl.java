package com.qualcomm.robotcore.hardware;

public class ServoImpl extends ServoImplEx {
    private final ServoController controller;
    
    public ServoImpl(ServoController controller, int port, Servo.Direction direction) {
        this.controller = controller;
    }

    @Override
    public void setDirection(Direction direction) {
        controller.getUnderlyingServo().setDirection(direction);
    }

    @Override
    public Direction getDirection() {
        return controller.getUnderlyingServo().getDirection();
    }

    @Override
    public void setPosition(double position) {
        controller.getUnderlyingServo().setPosition(position);
    }

    @Override
    public double getPosition() {
        return controller.getUnderlyingServo().getPosition();
    }

    @Override
    public void scaleRange(double min, double max) {
        controller.getUnderlyingServo().scaleRange(min, max);
    }

    @Override
    public ServoController getController() {
        return controller;
    }
}
