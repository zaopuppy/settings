package com.example.zero.androidskeleton.ui;

import android.app.ActionBar;
import android.bluetooth.*;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

/**
 * 0. check if system support bluetooth-le
 * 1. auto permission
 * 2. better action bar
 * 3. auto in range unlock
 */
public class ShowDeviceActivity extends AppCompatActivity {
    private static final String TAG = "ShowDeviceActivity";

    private TextView mDetailText;

    private void log(String msg) {
        Log.i(TAG, msg + '\n');
    }

    private BtLeDevice mDevice = null;

    private void showMsg(String msg) {
        mDetailText.append(msg + '\n');
    }

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

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mDevice.getName());
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setUiComp();

        // setup event handler
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.w(TAG, "invalidate option menu");
                invalidateOptionsMenu();
            }
        };

        mDevice.onReady(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(runnable);
            }
        });
        mDevice.onDisconnected(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(runnable);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.show_device_menu, menu);

        if (mDevice.getState() == BtLeDevice.State.READY) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mDevice.connectGatt(getApplicationContext());
                invalidateOptionsMenu();
                break;
            case R.id.menu_disconnect:
                mDevice.disconnectGatt();
                invalidateOptionsMenu();
                break;
            default:
                break;
        }
        return true;
    }

    private void setUiComp() {
        mDetailText = (TextView) findViewById(R.id.device_detail);
        assert mDetailText != null;

        final Button openButton = (Button) findViewById(R.id.open_button);
        assert openButton != null;
        // openButton.setEnabled(false);
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

                mDevice.makeNotify(characteristic4, new BtLeDevice.Listener<Boolean>() {
                    @Override
                    public void onResult(Boolean result) {
                        Log.e(TAG, "make notify: " + result);
                    }
                });

                ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE/8);
                buffer.putLong(0x0A0000000000000BL);
                mDevice.writeCharacteristic(characteristic1, buffer.array(), new BtLeDevice.Listener<Boolean>() {
                    @Override
                    public void onResult(Boolean result) {
                        Log.e(TAG, "write result: " + result);
                    }
                });
            }
        });

        // mDevice.connectGatt(getApplicationContext());
    }

}
