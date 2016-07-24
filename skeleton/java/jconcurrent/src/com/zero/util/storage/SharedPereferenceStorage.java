package com.zero.util.storage;

/**
 * for android
 *
 * Created by zhaoyi on 7/23/16.
 */
public class SharedPereferenceStorage implements Storage {
    @Override
    public boolean put(String key, boolean value) {
        return false;
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return false;
    }

    @Override
    public boolean put(String key, int value) {
        return false;
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return 0;
    }

    @Override
    public boolean put(String key, long value) {
        return false;
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return 0;
    }

    @Override
    public boolean put(String key, float value) {
        return false;
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return 0;
    }

    @Override
    public boolean put(String key, double value) {
        return false;
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        return 0;
    }

    @Override
    public boolean put(String key, String value) {
        return false;
    }

    @Override
    public String getString(String key, String defaultValue) {
        return null;
    }

    @Override
    public boolean put(String key, Storable value) {
        return false;
    }

    @Override
    public Object getObject(String key, Storable defaultValue) {
        return null;
    }
}
