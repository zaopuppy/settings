package com.example.zero.androidskeleton.bt;

import android.util.Log;

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
}
