package com.qualcomm.ftccommon;

import com.qualcomm.robotcore.eventloop.EventLoopManager;

public class FtcEventLoopHandler {
    private static FtcEventLoopHandler instance;
    public static FtcEventLoopHandler getInstance() {
        if (instance == null) instance = new FtcEventLoopHandler();
        return instance;
    }

    public EventLoopManager eventLoopManager = EventLoopManager.getInstance();
}
