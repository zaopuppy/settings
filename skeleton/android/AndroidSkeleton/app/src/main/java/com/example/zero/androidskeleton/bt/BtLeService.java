package com.example.zero.androidskeleton.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.util.Log;

import java.util.UUID;


/**
 * Created by zero on 2016/4/7.
 */
public class BtLeService {
    private static final String TAG = "BtLeService";

    // see https://www.bluetooth.com/specifications/assigned-numbers/generic-attribute-profile
    /**
     * Services
     */
    public static final int UUID_GAP_SERVICE               = 0x1800;
    public static final int UUID_GENERIC_ATTRIBUTE_PROFILE = 0x1801;

    /**
     * Attribute Types
     */
    public static final int UUID_PRIMARY_SERVICE   = 0x2800;
    public static final int UUID_SECONDARY_SERVICE = 0x2801;
    public static final int UUID_INCLUDE           = 0x2802;
    public static final int UUID_CHARACTERISTIC    = 0x2803;

    /**
     * Characteristic Descriptors
     */
    public static final int UUID_CHARACTERISTIC_EXTENDED_PROPERTIES  = 0x2900;
    public static final int UUID_CHARACTERISTIC_USER_DESCRIPTION     = 0x2901;
    public static final int UUID_CLIENT_CHARACTERISTIC_CONFIGURATION = 0x2902;
    public static final int UUID_SERVER_CHARACTERISTIC_CONFIGURATION = 0x2903;
    public static final int UUID_CHARACTERISTIC_FORMAT               = 0x2904;
    public static final int UUID_CHARACTERISTIC_AGGREGATE_FORMAT     = 0x2905;

    /**
     * Characteristic Types
     */
    public static final int UUID_DEVICE_NAME                                = 0x2A00;
    public static final int UUID_APPEARANCE                                 = 0x2A01;
    public static final int UUID_PERIPHERAL_PRIVACY_FLAG                    = 0x2A02;
    public static final int UUID_RECONNECTION_ADDRESS                       = 0x2A03;
    public static final int UUID_PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS = 0x2A04;
    public static final int UUID_SERVICE_CHANGED                            = 0x2A05;


    public static final BtLeService INSTANCE = new BtLeService();

    private BluetoothAdapter mBtAdapter;
    private BluetoothLeScanner mBtLeScanner;

    private BtLeService() {
        init();
    }

    public void init() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        mBtLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
    }

    public static int extractBtUuid(UUID uuid) {
        return (int) ((uuid.getMostSignificantBits() & 0x0000FFFF00000000L) >> 32);
    }

    public static String uuidStr(UUID uuid) {
        return Integer.toHexString(extractBtUuid(uuid));
    }

    public static boolean isReservedUuid(UUID uuidObj) {
        int uuid = extractBtUuid(uuidObj);

        return isReservedUuid(uuid);
    }

    public static boolean isReservedUuid(int uuid) {
        return uuid < 0xFFE0;
    }

    public BluetoothAdapter getAdapter() {
        return mBtAdapter;
    }

    public BluetoothLeScanner getScanner() {
        return mBtLeScanner;
    }

    public void startScan(ScanCallback callback) {
        Log.e(TAG, "startScan");
        mBtLeScanner.startScan(callback);
    }

    public void stopScan(ScanCallback callback) {
        mBtLeScanner.stopScan(callback);
    }

    public void startScanOld(BluetoothAdapter.LeScanCallback callback) {
        mBtAdapter.startLeScan(callback);
    }

    public void stopScanOld(BluetoothAdapter.LeScanCallback callback) {
        mBtAdapter.stopLeScan(callback);
    }
}
