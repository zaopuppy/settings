package com.zero.util.concurrent;

/**
 * Created by zhaoyi on 7/21/16.
 */
public interface GenericFutureListener<T> {
    void onResult(int code, T result);
}
