package com.example.zero.androidskeleton.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by zero on 2016/4/4.
 */
public class Utils {
    public static void makeToast(Context context, String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }

    public static String b16encode(byte[] value) {
        if (value == null) {
            return "<null>";
        }

        final char[] m = "0123456789ABCDEF".toCharArray();
        StringBuilder builder = new StringBuilder(16);
        for (byte b: value) {
            byte hi = (byte) ((b >> 4) & 0xF);
            byte lo = (byte)        (b & 0xF);
            builder.append(m[hi]);
            builder.append(m[lo]);
        }

        return builder.toString();
    }

    public static boolean isFlagSet(int prop, int flag) {
        return (prop & flag) > 0;
    }

}
