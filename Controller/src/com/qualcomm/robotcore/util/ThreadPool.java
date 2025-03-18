package com.qualcomm.robotcore.util;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Dbg;

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
            defaultExecutor = new ThreadPoolExecutor(8, 16, 6L, TimeUnit.SECONDS, new SynchronousQueue<>()) {
                @Override
                protected void afterExecute(Runnable r, Throwable t) {
                    super.afterExecute(r, t);
                    if (t == null && r instanceof Future<?>)
                    {
                        try
                        {
                            /**
                             * In at least one case we've seen a call to get() deadlock if the runnable
                             * was not done.  Hence the protection here.
                             */
                            if (((Future<?>) r).isDone())
                            {
                                Object result = ((Future<?>) r).get();
                            }
                        }
                        catch (CancellationException ce)
                        {
                            t = null; // not a user error; don't report
                        }
                        catch (ExecutionException ee)
                        {
                            t = ee.getCause();
                        }
                        catch (InterruptedException ie)
                        {
                            Thread.currentThread().interrupt();
                        }
                    }
                    if (t != null)
                    {
                        System.out.println("ignored exception from thread pool: " + t);
                    }
                }
            };
        }
        return defaultExecutor;
    }
}
