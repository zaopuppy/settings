package com.zero.util.concurrent;

/**
 * Created by zhaoyi on 7/21/16.
 */
public interface Future<T> extends java.util.concurrent.Future<T> {
    void addListener(GenericFutureListener<T> listener);
}
