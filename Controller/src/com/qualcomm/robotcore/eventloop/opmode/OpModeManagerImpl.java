package com.qualcomm.robotcore.eventloop.opmode;

import com.qualcomm.robotcore.hardware.HardwareMap;
import javafx.application.Application;
import javafx.application.Platform;
import virtual_robot.controller.VirtualRobotController;

import java.lang.reflect.Field;
import java.util.Objects;

import static virtual_robot.controller.VirtualRobotController.getNameFromAnnotationOrOpmode;

public class OpModeManagerImpl {
    public static class ForceStopException extends RuntimeException {}

    public void registerListener(Object thisObjectIsDefinitelyUsed) {

    }
    
    public void unregisterListener(Object thisObjectIsDefinitelyUsed) {

    }
    
    // shimmed to pressing the button, may be in a weird state use with caution
    public void startActiveOpMode() {
        VirtualRobotController.pressDriverButton.run();
    }
    
    public void initOpMode(String opModeName) {
        initOpMode(opModeName, false);
    }
    
    public void initOpMode(String opModeName, boolean defaultMode) {
        for (Class<?> o : VirtualRobotController.opModeListCurrent.getItems()) {
            String name = getNameFromAnnotationOrOpmode(o, o.getSimpleName());
            if (Objects.equals(name, opModeName)) {
                System.out.println("initOpMode(" + name + ")");
                Platform.runLater(() -> {
                    VirtualRobotController.opModeListCurrent.setValue(o);
                });
            }
        }
        // ensure we stop
        VirtualRobotController.opModeInitialized = true;
        VirtualRobotController.opModeStarted = true;
        VirtualRobotController.pressDriverButton.run();
        
        // init
        VirtualRobotController.pressDriverButton.run();
    }
    
    public HardwareMap getHardwareMap() {
        return VirtualRobotController.hardwareMap;
    }
    
    public static OpModeManagerImpl getOpModeManagerOfActivity(Object activity) {
        return new OpModeManagerImpl();
    }

    public String getActiveOpModeName() {
        return getNameFromAnnotationOrOpmode(heyThisisTheOpModeThatsRunning.getClass(), heyThisisTheOpModeThatsRunning.getClass().getSimpleName());
    }
    public static OpMode heyThisisTheOpModeThatsRunning;
    
    public OpMode getActiveOpMode() {
        return heyThisisTheOpModeThatsRunning;
    }

    public static class DefaultOpMode extends OpMode {

        @Override
        public void init() {
            // this thing doesnt actually do anything
        }

        @Override
        public void loop() {

        }
    }
}
