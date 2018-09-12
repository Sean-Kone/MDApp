package gsi.com.mdapp;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class IOExecutor {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    private static IOExecutor sInstance = null;

    private BlockingQueue<Runnable> mQueue;
    private ThreadPoolExecutor mExecutor;

    private IOExecutor() {
        mQueue = new LinkedBlockingQueue<Runnable>();
        mExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mQueue);
    }

    public static IOExecutor getInstance() {
        if (sInstance == null) {
            synchronized (IOExecutor.class) {
                if (sInstance == null) {
                    sInstance = new IOExecutor();
                }
            }
        }
        return sInstance;
    }

    public void execute(Runnable runnable) {
        mExecutor.execute(runnable);
    }
}