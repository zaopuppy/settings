package com.example.zero.androidskeleton.ui;

import android.bluetooth.*;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.zero.androidskeleton.R;
import com.example.zero.androidskeleton.bt.BtLeService;
import com.example.zero.androidskeleton.bt.BtLeDevice;
import com.example.zero.androidskeleton.utils.Utils;

public class ShowDeviceActivity extends AppCompatActivity {
    private static final String TAG = "ShowDeviceActivity";

    private void log(String msg) {
        Log.i(TAG, msg + '\n');
    }

    private BtLeDevice mDevice = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_device);

        Intent intent = getIntent();
        BluetoothDevice device = intent.getParcelableExtra("device");
        if (device == null) {
            Utils.makeToast(this, "no device supplied");
            finish();
            return;
        }

        mDevice = new BtLeDevice(device);

        setUiComp();
    }

    private void setUiComp() {
        TextView nameView = (TextView) findViewById(R.id.name);
        assert nameView != null;
        nameView.setText(mDevice.getName());

        TextView addressView = (TextView) findViewById(R.id.address);
        assert addressView != null;
        addressView.setText(mDevice.getAddress());

        final Button openButton = (Button) findViewById(R.id.open_button);
        assert openButton != null;
        openButton.setEnabled(false);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDevice.getState() != BtLeDevice.State.READY) {
                    Utils.makeToast(ShowDeviceActivity.this, "device is not yet ready");
                    return;
                }

                for (BluetoothGattService service: mDevice.getServiceList()) {
                    Log.i(TAG, "service: " + service.getUuid());
                    for (final BluetoothGattCharacteristic characteristic: service.getCharacteristics()) {
                        mDevice.readCharacteristic(characteristic, new BtLeDevice.Listener<byte[]>() {
                            @Override
                            public void onResult(byte[] result) {
                                if (result == null) {
                                    // Utils.makeToast(getApplicationContext(), "read null returned");
                                    Log.e(TAG, "uuid=" + characteristic.getUuid() + " null returned");
                                    return;
                                }
                                // Utils.makeToast(getApplicationContext(), "value read length: " + result.length);
                                Log.e(TAG, "uuid=" + characteristic.getUuid() + " length=" + result.length + ", value=" + Utils.b16encode(result));
                            }
                        });
                    }
                }
            }
        });

        // setup event handler
        mDevice.onReady(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        openButton.setEnabled(true);
                    }
                });
            }
        });
        mDevice.onDisconnected(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        openButton.setEnabled(false);
                    }
                });
            }
        });

        mDevice.connectGatt(getApplicationContext());
    }

}
