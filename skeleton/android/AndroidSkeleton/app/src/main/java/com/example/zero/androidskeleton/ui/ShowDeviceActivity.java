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

                for (BluetoothGattService service: mDevice.getServiceList()) {
                    Log.i(TAG, "service: " + BtLeService.uuidStr(service.getUuid()));
                    for (final BluetoothGattCharacteristic characteristic: service.getCharacteristics()) {
                        final int uuid16 = BtLeService.extractBtUuid(characteristic.getUuid());
                        if (BtLeService.isReservedUuid(uuid16)) {
                            continue;
                        }

                        if (uuid16 != 0xfff1) {
                            continue;
                        }


                        BtLeDevice.Listener<byte[]> l = new BtLeDevice.Listener<byte[]>() {
                            @Override
                            public void onResult(final byte[] result) {
                                if (BtLeService.isReservedUuid(uuid16)) {
                                    handleReservedCharacteristic(uuid16, result);
                                    return;
                                }
                                if (result == null || result.length == 0) {
                                    Log.e(TAG, "uuid=" + BtLeService.uuidStr(characteristic.getUuid()) + " result=" + (result == null ? "<null>" : "empty"));
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            devDetailView.append("uuid=" + BtLeService.uuidStr(characteristic.getUuid()) + " result=<null>\n");
                                        }
                                    });
                                    return;
                                }
                                Log.e(TAG, "uuid=" + BtLeService.uuidStr(characteristic.getUuid()) + " length=" + result.length + ", value=" + Utils.b16encode(result));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        devDetailView.append(
                                                "uuid=" + BtLeService.uuidStr(characteristic.getUuid())
                                                        + " length=" + result.length + ", value=" + Utils.b16encode(result) + '\n');
                                    }
                                });
                                // mDevice.readCharacteristic(characteristic, this);
                            }

                            private void handleReservedCharacteristic(int uuid, final byte[] result) {
                                switch (uuid) {
                                    case BtLeService.UUID_DEVICE_NAME:
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                devDetailView.append("Device Name: [" + new String(result, Charset.forName("GBK")));
                                            }
                                        });
                                        break;
                                    default:
                                        // ignore
                                        break;
                                }
                            }
                        };

                        // mDevice.readCharacteristic(characteristic, l);
                        ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE/8);
                        buffer.putLong(0x0A0000000000000BL);
                        Log.e(TAG, "write: " + mDevice.writeCharacteristic(characteristic, buffer.array()));
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
