package com.zero.tools.javaclassviewer;

import org.apache.commons.javaflow.Continuation;

/**
 * Created by zhaoyi on 7/10/16.
 */
public class TestContinuation implements Runnable {
    @Override
    public void run() {
        for (int i = 0; i < 10; ++i) {
            System.out.println(i);
            Continuation.suspend();
        }
    }
}
