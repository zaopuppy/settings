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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.UUID;

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
        final TextView devDetailView = (TextView) findViewById(R.id.device_detail);
        assert devDetailView != null;

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

                BluetoothGattCharacteristic characteristic1 = null;
                BluetoothGattCharacteristic characteristic2 = null;
                BluetoothGattCharacteristic characteristic3 = null;
                BluetoothGattCharacteristic characteristic4 = null;

                for (BluetoothGattService service : mDevice.getServiceList()) {
                    Log.i(TAG, "service: " + BtLeService.uuidStr(service.getUuid()));

                    for (final BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        final int uuid16 = BtLeService.extractBtUuid(characteristic.getUuid());
                        if (BtLeService.isReservedUuid(uuid16)) {
                            continue;
                        }

                        switch (uuid16) {
                            case 0xfff1:
                                characteristic1 = characteristic;
                                break;
                            case 0xfff2:
                                characteristic2 = characteristic;
                                break;
                            case 0xfff3:
                                characteristic3 = characteristic;
                                break;
                            case 0xfff4:
                                characteristic4 = characteristic;
                                break;
                            default:
                                // ignore
                                break;
                        }
                    }
                }

                mDevice.makeNotify(characteristic4);

                ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE/8);
                buffer.putLong(0x0A0000000000000BL);
                final BluetoothGattCharacteristic tempChara = characteristic4;
                mDevice.writeCharacteristic(characteristic1, buffer.array(), new BtLeDevice.Listener<Boolean>() {
                    @Override
                    public void onResult(Boolean result) {
                        Log.e(TAG, "write result: " + result);
                        //mDevice.readCharacteristic(tempChara, new BtLeDevice.Listener<byte[]>() {
                        //    @Override
                        //    public void onResult(byte[] result) {
                        //        Log.e(TAG, "read result: " + Utils.b16encode(result));
                        //    }
                        //});
                    }
                });
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
