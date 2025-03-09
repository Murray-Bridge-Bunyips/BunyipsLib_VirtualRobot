package com.qualcomm.robotcore.eventloop.opmode;

public class OpModeManagerImpl {
    public static class ForceStopException extends RuntimeException {}

    public void registerListener(Object thisObjectIsDefinitelyUsed) {

    }
    
    public void unregisterListener(Object thisObjectIsDefinitelyUsed) {

    }
    
    public void initOpMode(String thisIsDefinitelyGoingToInitialiseSomething, boolean boo) {
        
    }
    
    public static OpModeManagerImpl getOpModeManagerOfActivity(Object activity) {
        return new OpModeManagerImpl();
    }

    public String getActiveOpModeName() {
        return "Virtual OpMode";
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
