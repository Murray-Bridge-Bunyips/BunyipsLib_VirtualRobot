package com.qualcomm.robotcore.hardware;

public class ServoControllerImpl implements ServoController{
    private static ServoControllerImpl theInstance = null;
    private ServoControllerImpl() {}
    public static ServoControllerImpl getInstance(){
        if (theInstance == null) {
            theInstance = new ServoControllerImpl();
        }
        return theInstance;
    }
    
    private Servo servo;
    
    public static ServoControllerImpl useOnServo(Servo servo) {
        ServoControllerImpl servoController = new ServoControllerImpl();
        servoController.servo = servo;
        return servoController;
    }

    @Override
    public Servo getUnderlyingServo() {
        return servo;
    }
}
