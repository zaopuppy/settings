package com.example.zero.androidskeleton.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by zero on 2016/4/4.
 */
public class UiUtils {
    public static void makeToast(Context context, String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }
}
