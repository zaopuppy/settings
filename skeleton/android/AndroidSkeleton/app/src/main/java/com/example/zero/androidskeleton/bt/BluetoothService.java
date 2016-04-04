package com.example.zero.androidskeleton.bt;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

/**
 * Created by zero on 4/4/16.
 */
public class BluetoothService {

    private final Context mContext;

    public enum Code {
        BT_NOT_ENABLED
    }

    public BluetoothService(Context context) {
        mContext = context.getApplicationContext();
    }

    public Code start() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        assert adapter != null;

        if (!adapter.isEnabled()) {
            return Code.BT_NOT_ENABLED;
        }

    }
}
