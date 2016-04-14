package com.example.zero.androidskeleton.bt;

import android.util.Log;

/**
 * Created by zero on 4/11/16.
 */
public class DoorProtocol {

    private static final String TAG = "DoorProtocol";

    public byte[] openDoor(int password) {
        if (password < 0 || password >= 1000000) {
            return null;
        }

        return new byte[0];
    }
}
