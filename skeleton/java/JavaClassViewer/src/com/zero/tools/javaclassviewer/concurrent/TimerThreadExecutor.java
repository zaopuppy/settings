package com.zero.tools.javaclassviewer.concurrent;

import com.zero.tools.javaclassviewer.common.GenericResultListener;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zhaoyi on 7/9/16.
 */
public class TimerThreadExecutor extends ThreadExecutor {

    private final Timer mTimer;

    public TimerThreadExecutor(String name) {
        super(name);
        mTimer = new Timer();
    }

    private static class GuardedListener<T> implements GenericResultListener<T> {
        private final GenericResultListener<T> mListener;
        private final AtomicBoolean mCalled = new AtomicBoolean(false);

        private GuardedListener(GenericResultListener<T> l) {
            mListener = l;
        }

        @Override
        public void onResult(int code, T result) {
            if (mCalled.compareAndSet(false, true)) {
                if (mListener != null) {
                    mListener.onResult(code, result);
                }
            }
        }
    }

    public <T> Future<T> submit(Callable<T> task, GenericResultListener<T> listener, long timeout) {
        GuardedListener<T> guardedListener = new GuardedListener<>(listener);
        Future<T> future = super.submit(task, guardedListener);
        if (timeout >= 0) {
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    guardedListener.onResult(CODE_TIMEOUT, null);
                    future.cancel(true);
                }
            }, timeout);
        }

        // FIXME: listener won't be called if invoker call `future.cancel`
        return future;
    }

}
