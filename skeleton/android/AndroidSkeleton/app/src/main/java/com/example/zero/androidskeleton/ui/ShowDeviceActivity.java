package com.example.zero.androidskeleton.ui;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.example.zero.androidskeleton.R;
import com.example.zero.androidskeleton.bt.BtLeDevice;
import com.example.zero.androidskeleton.bt.BtLeService;
import com.example.zero.androidskeleton.bt.DoorProtocol;
import com.example.zero.androidskeleton.storage.BtDeviceStorage;
import com.example.zero.androidskeleton.utils.Utils;

import java.nio.ByteBuffer;

/**
 *
 * * check if system support bluetooth-le
 * * auto in range unlock
 * * report mobile phone number
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

        ActionBar actionBar = getSupportActionBar();
        Log.e(TAG, "action bar: " + actionBar);
        if (actionBar != null) {
            actionBar.setTitle(mDevice.getName());
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setUiComp();

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
            case android.R.id.home:
                onBackPressed();
                break;
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

    @Override
    protected void onPause() {
        mDevice.disconnectGatt();
        super.onPause();
    }

    private void setUiComp() {
        mDetailText = (TextView) findViewById(R.id.device_detail);
        assert mDetailText != null;

        final Button openButton = (Button) findViewById(R.id.open_button);
        assert openButton != null;
        openButton.setEnabled(false);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int password = BtDeviceStorage.INSTANCE.get(mDevice.getAddress());
                if (password >= 0) {
                    mPassword = password;
                    open(password);
                    return;
                }
                AlertDialog dialog = createPasswordDialog();
                dialog.show();
            }
        });

        // setup event handler
        mDevice.onReady(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        invalidateOptionsMenu();
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
                        invalidateOptionsMenu();
                        openButton.setEnabled(false);
                    }
                });

            }
        });

    }

    private int mPassword = 0;
    private AlertDialog createPasswordDialog() {
        // create password view
        View passView = getLayoutInflater().inflate(R.layout.input_password, null);
        final EditText passText = (EditText) passView.findViewById(R.id.password_edit);
        assert passText != null;

        // create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        return builder
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        mPassword = Integer.parseInt(passText.getText().toString());
                        open(mPassword);
                    } catch (NumberFormatException e) {
                        showMsg("invalid password");
                    }
                }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Utils.makeToast(ShowDeviceActivity.this, "Cancel");
                }
            })
            .setView(passView)
            .create();
    }

    private void open(final int password) {
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
                        mDevice.onCharacteristicChanged(new BtLeDevice.CharacteristicChangedListener() {
                            @Override
                            public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic chara) {
                                byte[] value = chara.getValue();
                                if (value == null || value.length <= 0) {
                                    // ignore
                                    return;
                                }

                                final byte result = chara.getValue()[0];
                                final String resultStr;
                                switch (result) {
                                    case DoorProtocol.RESULT_PASSWORD_CORRECT: {
                                        resultStr = "开门密码正确";
                                        Log.d(TAG, "save password: " + mPassword);
                                        BtDeviceStorage.INSTANCE.put(mDevice.getAddress(), mPassword);
                                        break;
                                    }
                                    case DoorProtocol.RESULT_PASSWORD_WRONG: {
                                        resultStr = "开门密码错误";
                                        Log.d(TAG, "bad password clear: " + mPassword);
                                        BtDeviceStorage.INSTANCE.put(mDevice.getAddress(), -1);
                                        break;
                                    }
                                    case DoorProtocol.RESULT_PASSWORD_CHANGED: {
                                        resultStr = "修改密码成功";
                                        Log.d(TAG, "password changed: " + mPassword);
                                        BtDeviceStorage.INSTANCE.put(mDevice.getAddress(), mPassword);
                                        break;
                                    }
                                    default: {
                                        resultStr = "" + result;
                                        break;
                                    }
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showMsg("result: " + resultStr);
                                    }
                                });
                            }
                        });
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

        byte[] msg = DoorProtocol.openDoor(password);
        if (msg == null) {
            showMsg("invalid password?");
            return;
        }
        mDevice.writeCharacteristic(characteristic1, msg, new BtLeDevice.Listener<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                Log.e(TAG, "write result: " + result);
            }
        });
    }


}
