package com.example.zero.androidskeleton.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;

/**
 * Created by zero on 2016/4/7.
 */
public class BtLeService {
    public static final BtLeService INSTANCE = new BtLeService();

    private final BluetoothAdapter mBtAdapter;
    private final BluetoothLeScanner mBtLeScanner;

    private BtLeService() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        mBtLeScanner = mBtAdapter.getBluetoothLeScanner();
    }

    public void startScan(ScanCallback callback) {
        mBtLeScanner.startScan(callback);
    }

    public void stopScan(ScanCallback callback) {
        mBtLeScanner.stopScan(callback);
    }
}
