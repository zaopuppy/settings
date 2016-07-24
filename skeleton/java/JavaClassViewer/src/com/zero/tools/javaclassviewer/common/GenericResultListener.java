package com.zero.tools.javaclassviewer.common;

/**
 * Created by zhaoyi on 7/9/16.
 */
public interface GenericResultListener<T> {
    void onResult(int code, T result);
}
