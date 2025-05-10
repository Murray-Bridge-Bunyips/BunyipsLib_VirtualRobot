package com.qualcomm.robotcore.eventloop;


import com.qualcomm.robotcore.hardware.Gamepad;

public class EventLoopManager {
    private static EventLoopManager instance;
    public static EventLoopManager getInstance() {
        if (instance == null) instance = new EventLoopManager();
        return instance;
    }

    public final Gamepad[] opModeGamepads = { new Gamepad(), new Gamepad() };
}
