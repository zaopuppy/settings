package com.example.zero.androidskeleton.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;
import com.example.zero.androidskeleton.concurrent.TimerThreadExecutor;
import com.example.zero.androidskeleton.utils.SequenceGenerator;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * Created by zero on 2016/4/7.
 */
public class BtLeService {

    private static final String TAG = "BtLeService";

    private static final long MAX_SCAN_TIME = 5*1000;

    public interface ScanListener {
        void onDeviceFound(BtLeDevice dev);

        void onScanChange(boolean isScanning);
    }

    public static final BtLeService INSTANCE = new BtLeService();

    private final Timer mTimer = new Timer("btle-service-timer");

    private final TimerThreadExecutor mExecutor = new TimerThreadExecutor(2, 10*1000, mTimer);

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

            final BtLeDevice wrappedDev;
            synchronized (mDeviceMap) {
                String key = dev.getAddress();
                Log.e(TAG, "key: " + key);
                if (!mDeviceMap.containsKey(key)) {
                    wrappedDev = new BtLeDevice(dev);
                    mDeviceMap.put(key, wrappedDev);
                } else {
                    wrappedDev = mDeviceMap.get(key);
                }
            }
            notifyDeviceFound(wrappedDev);
        }
    };

    private void notifyDeviceFound(BtLeDevice dev) {
        for (ScanListener l: mScanListenerList) {
            try {
                l.onDeviceFound(dev);
            } catch (Exception e) {
                // ignore
            }
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

    private class ScanTimerTask extends TimerTask {

        private final int last;

        ScanTimerTask(int seq) {
            last = seq;
        }

        @Override
        public void run() {
            if (mSeqGen.last() == last) {
                stopScan();
            }
        }
    }

    private boolean mScanning = false;
    private SequenceGenerator mSeqGen = new SequenceGenerator(0, 0xFFFFF);
    public int startScan() {
        Log.e(TAG, "startScan");
        stopScan();
        mScanning = true;
        getScanner().startScan(mScanCallback);
        mTimer.schedule(new ScanTimerTask(mSeqGen.next()), MAX_SCAN_TIME);
        return BtCode.OK;
    }

    public int stopScan() {
        getScanner().stopScan(mScanCallback);
        mScanning = false;
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

    public void clearDevices() {
        synchronized (mDeviceMap) {
            for (Map.Entry<String, BtLeDevice> e: mDeviceMap.entrySet()) {
                e.getValue().disconnectGatt();
            }
            mDeviceMap.clear();
        }
    }

    public BtLeDevice getDevice(String addr) {
        if (addr == null || addr.length() <= 0) {
            return null;
        }
        synchronized (mDeviceMap) {
            return mDeviceMap.get(addr);
        }
    }
}
