package org.prebid.mobile.drprebid.managers;

import org.prebid.mobile.drprebid.async.MainThreadExecutor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DemandTestManager {
    private final ThreadPoolExecutor requestThreadPool;
    private final BlockingQueue<Runnable> requestQueue;

    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 5;
    private static final int KEEP_ALIVE_TIME = 50;

    private static DemandTestManager testManager;
    private static MainThreadExecutor handler;

    static {
        testManager = new DemandTestManager();
        handler = new MainThreadExecutor();
    }

    private DemandTestManager() {
        requestQueue = new LinkedBlockingQueue<>();
        requestThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS, requestQueue);
    }

    public static DemandTestManager getInstance() {
        return testManager;
    }

    public MainThreadExecutor getMainThreadExecutor() {
        return handler;
    }

    public void runRequest(Runnable task) {
        requestThreadPool.execute(task);
    }
}
