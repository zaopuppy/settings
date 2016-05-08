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
import com.example.zero.androidskeleton.bt.BtLeUtil;
import com.example.zero.androidskeleton.bt.DoorProtocol;
import com.example.zero.androidskeleton.storage.BtDeviceStorage;
import com.example.zero.androidskeleton.utils.Utils;

/**
 *
 * * check if system support bluetooth-le
 * * auto in range unlock
 * * report mobile phone number
 */
public class ShowDeviceActivity extends AppCompatActivity implements BtLeDevice.DeviceListener {
    private static final String TAG = "ShowDeviceActivity";

    private TextView mDetailText;
    private Button openButton;

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
        if (intent == null || intent.getExtras() == null) {
            finish();
            return;
        }
        Bundle bundle = intent.getExtras();

        mDevice = BtLeService.INSTANCE.getDevice(bundle.getString("addr"));
        if (mDevice == null) {
            Utils.makeToast(this, "no device supplied");
            finish();
            return;
        }

        ActionBar actionBar = getSupportActionBar();
        Log.e(TAG, "action bar: " + actionBar);
        if (actionBar != null) {
            actionBar.setTitle(mDevice.getName());
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setUiComp();

    }

    @Override
    protected void onResume() {
        mDevice.addDeviceListener(this);
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.show_device_menu, menu);

        if (mDevice != null && mDevice.getState() == BtLeDevice.State.READY) {
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
        mDevice.removeDeviceListener(this);
        mDevice.disconnectGatt();
        super.onPause();
    }

    private void setUiComp() {
        mDetailText = (TextView) findViewById(R.id.device_detail);
        assert mDetailText != null;

        openButton = (Button) findViewById(R.id.open_button);
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
            Log.i(TAG, "service: " + BtLeUtil.uuidStr(service.getUuid()));

            for (final BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                final int uuid16 = BtLeUtil.extractBtUuid(characteristic.getUuid());
                if (BtLeUtil.isReservedUuid(uuid16)) {
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

        mDevice.makeNotify(characteristic4, new BtLeDevice.ResultListener<Boolean>() {
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
        mDevice.writeCharacteristic(characteristic1, msg, new BtLeDevice.ResultListener<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                Log.e(TAG, "write result: " + result);
            }
        });
    }


    @Override
    public void onDeviceStateChanged(BtLeDevice.State state) {
        invalidateOptionsMenu();
        if (state == BtLeDevice.State.READY) {
            openButton.setEnabled(true);
        } else {
            openButton.setEnabled(false);
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        byte[] value = characteristic.getValue();
        if (value == null || value.length <= 0) {
            // ignore
            return;
        }

        final byte result = characteristic.getValue()[0];
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
}
