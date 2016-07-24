package com.zero.util.storage;

/**
 * Created by zhaoyi on 7/23/16.
 */
public interface Storage {

    interface Storable<T> {
        String encode();
        T decode(String data);
    }

    boolean put(String key, boolean value);

    boolean getBoolean(String key, boolean defaultValue);

    boolean put(String key, int value);

    int getInt(String key, int defaultValue);

    boolean put(String key, long value);

    long getLong(String key, long defaultValue);

    boolean put(String key, float value);

    float getFloat(String key, float defaultValue);

    boolean put(String key, double value);

    double getDouble(String key, double defaultValue);

    boolean put(String key, String value);

    String getString(String key, String defaultValue);

    boolean put(String key, Storable value);

    Object getObject(String key, Storable defaultValue);
}

