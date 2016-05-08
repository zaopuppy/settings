package com.example.zero.androidskeleton.ui;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.example.zero.androidskeleton.R;
import com.example.zero.androidskeleton.bt.BtLeDevice;
import com.example.zero.androidskeleton.bt.BtLeService;
import com.example.zero.androidskeleton.utils.Utils;

import java.util.Timer;
import java.util.TimerTask;

public class SelectDeviceActivity extends AppCompatActivity {

    private static final String TAG = "SelectDeviceActivity";

    private EditText mCopyArea;
    private SimpleArrayAdapter mListViewAdapter;

    private void log(final String msg) {
        Log.i(TAG, msg + '\n');
    }

    private class MyScanListener implements BtLeService.ScanListener {

        @Override
        public void onDeviceFound(BtLeDevice dev) {
            mListViewAdapter.add(dev);
        }

        @Override
        public void onScanChange(boolean isScanning) {
            invalidateOptionsMenu();
        }

        //private void open(final BluetoothDevice dev, final int password) {
        //    final BtLeDevice device = new BtLeDevice(dev);
        //    device.onReady(new Runnable() {
        //        @Override
        //        public void run() {
        //            device.onReady(null);
        //            device.onCharacteristicChanged(new BtLeDevice.CharacteristicChangedListener() {
        //                @Override
        //                public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic chara) {
        //                    byte[] value = chara.getValue();
        //                    if (value == null || value.length <= 0) {
        //                        // ignore
        //                        return;
        //                    }
        //
        //                    final byte result = chara.getValue()[0];
        //                    Log.d(TAG, "onCharacteristicChanged: " + result);
        //                    switch (result) {
        //                        case DoorProtocol.RESULT_PASSWORD_CORRECT: {
        //                            Log.d(TAG, "save password: " + password);
        //                            BtDeviceStorage.INSTANCE.put(device.getAddress(), password);
        //                            runOnUiThread(new Runnable() {
        //                                @Override
        //                                public void run() {
        //                                    Utils.makeToast(getApplicationContext(), device.getName() + ": 开门密码正确");
        //                                }
        //                            });
        //                            break;
        //                        }
        //                        case DoorProtocol.RESULT_PASSWORD_WRONG: {
        //                            Log.d(TAG, "bad password clear");
        //                            BtDeviceStorage.INSTANCE.put(device.getAddress(), -1);
        //                            runOnUiThread(new Runnable() {
        //                                @Override
        //                                public void run() {
        //                                    Utils.makeToast(getApplicationContext(), device.getName() + ": 开门密码错误");
        //                                }
        //                            });
        //                            break;
        //                        }
        //                        default:
        //                            // ignore
        //                            break;
        //                    }
        //                    device.disconnectGatt();
        //                }
        //            });
        //            DoorProtocol.openDoor(device, password);
        //        }
        //    });
        //    device.connectGatt(getApplicationContext());
        //}
    }

    private final MyScanListener mScanListener = new MyScanListener();

    private void checkAllMyPermission() {
        final String[] permission_list = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        };
        for (String permission: permission_list) {
            checkMyPermission(permission);
        }
    }

    private void checkMyPermission(String permission) {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), permission);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        ActivityCompat.requestPermissions(this, new String[] { permission }, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkAllMyPermission();

        mListViewAdapter = new SimpleArrayAdapter(this, R.layout.select_list_item_device);

        setContentView(R.layout.activity_select_device);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Select Device");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Utils.makeToast(this, "该手机不支持低功耗蓝牙");
            finish();
        }

        if (BtLeService.INSTANCE.getAdapter() == null || BtLeService.INSTANCE.getScanner() == null) {
            Utils.makeToast(this, "蓝牙功能不支持或者开关未打开");
            finish();
        }

        setupUiComp();

    }

    private void startScan() {
        mListViewAdapter.clear();
        BtLeService.INSTANCE.startScan();
        invalidateOptionsMenu();
    }

    private void stopScan() {
        BtLeService.INSTANCE.stopScan();
        invalidateOptionsMenu();
    }

    private Timer mTimer;
    @Override
    protected void onResume() {
        super.onResume();
        BtLeService.INSTANCE.addScanListener(mScanListener);
        mTimer = new Timer("le-scan-timer");
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (BtLeService.INSTANCE.isScanning()) {
                            stopScan();
                            startScan();
                        }
                    }
                });
            }
        }, 0, 20*1000);
    }

    @Override
    protected void onPause() {
        BtLeService.INSTANCE.removeScanListener(mScanListener);
        mTimer.cancel();
        stopScan();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_device_menu, menu);
        // if (mScanner.isScanning()) {
        if (BtLeService.INSTANCE.isScanning()) {
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(
                R.layout.actionbar_indeterminate_progress);
        } else {
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_stop).setVisible(false);
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
            case R.id.menu_scan:
                startScan();
                break;
            case R.id.menu_stop:
                stopScan();
                break;
            default:
                break;
        }
        return true;
    }

    private ToggleButton mAutoButton;
    private void setupUiComp() {

        mCopyArea = (EditText) findViewById(R.id.copy_area);
        assert mCopyArea != null;

        mAutoButton = (ToggleButton) findViewById(R.id.auto_button);
        assert mAutoButton != null;
        mAutoButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    stopScan();
                    startScan();
                }
            }
        });

        ListView device_list_view = (ListView) findViewById(R.id.device_list_view);
        assert device_list_view != null;
        device_list_view.setAdapter(mListViewAdapter);
        device_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BtLeService.INSTANCE.stopScan();

                // get selected info
                final BtLeDevice device = mListViewAdapter.getItem(position);
                log("device: " + device.getName() + ", " + device.getAddress());

                Bundle bundle = new Bundle();
                bundle.putString("addr", device.getAddress());

                Intent intent = new Intent(SelectDeviceActivity.this, ShowDeviceActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
            }
        });
    }
}
