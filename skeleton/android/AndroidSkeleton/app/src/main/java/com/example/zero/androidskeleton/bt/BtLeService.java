package com.example.zero.androidskeleton.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Created by zero on 2016/4/7.
 */
public class BtLeService {
    private static final String TAG = "BtLeService";

    public interface ScanListener {
        void onDeviceFound(BtLeDevice dev);

        void onScanChange(boolean isScanning);
    }

    public static final BtLeService INSTANCE = new BtLeService();

    private final ConcurrentHashMap<String, BtLeDevice> mDeviceMap = new ConcurrentHashMap<>(4);

    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(TAG, "onScanResult: " + callbackType);
            if (result == null) {
                return;
            }

            BluetoothDevice dev = result.getDevice();
            if (dev == null) {
                Log.e(TAG, "null device received");
                return;
            }

            synchronized (mDeviceMap) {
                String key = dev.getAddress();
                if (!mDeviceMap.containsKey(key)) {
                    BtLeDevice wrappedDev = new BtLeDevice(dev);
                    mDeviceMap.put(key, wrappedDev);
                    notifyDeviceFound(wrappedDev);
                }
            }
        }
    };

    private void notifyDeviceFound(BtLeDevice dev) {
        for (ScanListener l: mScanListenerList) {
            l.onDeviceFound(dev);
        }
    }

    public BluetoothAdapter getAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }

    public BluetoothLeScanner getScanner() {
        BluetoothAdapter adapter = getAdapter();
        if (adapter == null) {
            return null;
        }
        return adapter.getBluetoothLeScanner();
    }

    private boolean mScanning = false;
    public int startScan() {
        Log.e(TAG, "startScan");
        stopScan();
        mScanning = true;
        getScanner().startScan(mScanCallback);
        return BtCode.OK;
    }

    public int stopScan() {
        getScanner().stopScan(mScanCallback);
        mScanning = false;
        synchronized (mDeviceMap) {
            mDeviceMap.clear();
        }
        return BtCode.OK;
    }

    public boolean isScanning() {
        return mScanning;
    }

    private final ConcurrentLinkedQueue<ScanListener> mScanListenerList = new ConcurrentLinkedQueue<>();

    public void addScanListener(ScanListener l) {
        mScanListenerList.add(l);
    }

    public void removeScanListener(ScanListener l) {
        mScanListenerList.remove(l);
    }

    public BtLeDevice getDevice(String addr) {
        if (addr == null || addr.length() <= 0) {
            return null;
        }
        return mDeviceMap.get(addr);
    }
}
