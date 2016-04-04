package com.example.zero.androidskeleton;

import com.example.zero.androidskeleton.bt.BluetoothService;

/**
 * Created by zero on 2016/4/4.
 */
public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();

        BluetoothService.INSTANCE.init(getApplicationContext());
    }
}
