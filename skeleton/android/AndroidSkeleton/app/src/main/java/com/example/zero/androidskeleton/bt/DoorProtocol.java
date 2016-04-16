package com.example.zero.androidskeleton.bt;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;
import com.example.zero.androidskeleton.storage.BtDeviceStorage;

import java.nio.ByteBuffer;

/**
 * Created by zero on 4/11/16.
 */
public class DoorProtocol {

    private static final String TAG = "DoorProtocol";

    // shared byte buffer
    private static final ByteBuffer mBuffer = ByteBuffer.allocate(10*1024);

    public static final byte RESULT_PASSWORD_CORRECT       = 0x01;
    public static final byte RESULT_PASSWORD_WRONG         = 0x02;
    public static final byte RESULT_ADMIN_PASSWORD_CORRECT = 0x03;
    public static final byte RESULT_ADMIN_PASSWORD_WRONG   = 0x04;
    public static final byte RESULT_PASSWORD_CHANGED       = 0x05;
    public static final byte RESULT_PHONE_NUMBER_PASSED    = 0x06;
    public static final byte RESULT_PHONE_NUMBER_NO_PASSED = 0x07;

    /**
     *
     * |0x0A|p1 p2 p3 p4 p5 p6|0x0B|
     *
     * @param password
     * @return
     */
    public static byte[] openDoor(int password) {
        if (password < 0 || password >= 1000000) {
            return null;
        }

        synchronized (mBuffer) {
            mBuffer.clear();
            mBuffer.put((byte) 0x0A);
            for (int i = 0; i < 6; ++i) {
                byte b = (byte) (password % 10);
                mBuffer.put(b);
                password = password / 10;
            }
            mBuffer.put((byte) 0x0B);
            mBuffer.flip();
            byte[] b = new byte[mBuffer.remaining()];
            mBuffer.get(b);
            return b;
        }
    }

    public static void openDoor(final BtLeDevice device, final int password) {
        if (device.getState() != BtLeDevice.State.READY) {
            Log.w(TAG, "device is not ready yet");
            return;
        }

        BluetoothGattCharacteristic characteristic1 = device.getCharacteristic(0xfff1);
        BluetoothGattCharacteristic characteristic4 = device.getCharacteristic(0xfff4);
        device.makeNotify(characteristic4, new BtLeDevice.Listener<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                Log.e(TAG, "make notify: " + result);
            }
        });
        byte[] msg = DoorProtocol.openDoor(password);
        if (msg == null) {
            // showMsg("invalid password?");
            return;
        }
        device.writeCharacteristic(characteristic1, msg, new BtLeDevice.Listener<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                Log.e(TAG, "write result: " + result);
            }
        });
    }
}
