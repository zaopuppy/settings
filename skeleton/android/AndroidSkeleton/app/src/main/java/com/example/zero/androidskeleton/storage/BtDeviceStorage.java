package com.example.zero.androidskeleton.storage;

import android.content.Context;
import com.example.zero.androidskeleton.utils.SpManager;

/**
 * Created by zero on 4/14/16.
 */
public class BtDeviceStorage {

    public static final int FLAG_NOT_SAVED = 0;

    public static final int FLAG_SAVED = 1;

    private final SpManager mSpManager;

    private BtDeviceStorage(Context context) {
        mSpManager = new SpManager(context, "saved_devices");
    }

    public int get(String address) {
        return mSpManager.getInt(address, FLAG_NOT_SAVED);
    }

    public void put(String address) {
        mSpManager.putInt(address, FLAG_SAVED);
    }
}
