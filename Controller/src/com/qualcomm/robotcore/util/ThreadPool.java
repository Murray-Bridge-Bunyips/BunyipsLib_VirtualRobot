package com.qualcomm.robotcore.util;

import java.util.concurrent.*;

public class ThreadPool {
    public static ExecutorService newFixedThreadPool(int i, String name) {
        return Executors.newFixedThreadPool(i);
    }
    public static ScheduledThreadPoolExecutor newScheduledExecutor(int maxWorkerThreadCount, String nameRoot)
    {
        return new ScheduledThreadPoolExecutor(maxWorkerThreadCount);
    }
    private static ExecutorService defaultExecutor = null;
    public static ExecutorService getDefault() {
        if (defaultExecutor == null) {
            defaultExecutor = new ThreadPoolExecutor(8, 16, 6L, TimeUnit.SECONDS, new SynchronousQueue<>());
        }
        return defaultExecutor;
    }
}
