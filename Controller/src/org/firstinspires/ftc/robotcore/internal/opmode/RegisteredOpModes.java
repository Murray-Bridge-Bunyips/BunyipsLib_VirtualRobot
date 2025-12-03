package org.firstinspires.ftc.robotcore.internal.opmode;

public class RegisteredOpModes {
    private static RegisteredOpModes instance = new RegisteredOpModes();
    public static RegisteredOpModes getInstance() {
        return instance;
    }
    
    public OpModeMeta getOpModeMetadata(String name) {
        // since the only use of this is for BunyipsLib checking in preload after the teleop/auto scanner, we just
        // return an empty meta since we need to do some classpath modification to get real meta. this is fine
        // since we just check if its not null (this will also assume all opmodes are "real" but that's fine)
        return new OpModeMeta.Builder().setName(name).build();
    }
}
