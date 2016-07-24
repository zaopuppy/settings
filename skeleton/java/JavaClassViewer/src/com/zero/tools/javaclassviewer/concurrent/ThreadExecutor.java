package com.zero.tools.javaclassviewer.concurrent;

import com.zero.tools.javaclassviewer.common.GenericResultListener;

import java.util.concurrent.*;

/**
 * Created by zhaoyi on 7/9/16.
 */
public class ThreadExecutor {

    public static final int CODE_SUCCESS = 0;
    public static final int CODE_EXCEPTION = -1;
    public static final int CODE_TIMEOUT = -2;

    private final ThreadPoolExecutor mExecutor;

    public ThreadExecutor(String name) {
        BlockingQueue<Runnable> mTaskQueue = new LinkedBlockingQueue<>(50);
        mExecutor = new ThreadPoolExecutor(4, 4, 5*60, TimeUnit.SECONDS, mTaskQueue);
    }

    public <T> Future<T> submit(Callable<T> task, GenericResultListener<T> listener) {
        return mExecutor.submit(() -> {
            int code = CODE_EXCEPTION;
            T result = null;
            try {
                result = task.call();
                code = CODE_SUCCESS;
            } finally {
                if (listener != null) {
                    listener.onResult(code, result);
                }
            }
            return result;
        });
    }
}
