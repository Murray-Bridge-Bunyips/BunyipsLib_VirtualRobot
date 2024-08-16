package com.qualcomm.robotcore.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ThreadPool {
    public static ExecutorService newFixedThreadPool(int i, String name) {
        return Executors.newFixedThreadPool(i);
    }
    public static ScheduledThreadPoolExecutor newScheduledExecutor(int maxWorkerThreadCount, String nameRoot)
    {
        return new ScheduledThreadPoolExecutor(maxWorkerThreadCount);
    }
}
