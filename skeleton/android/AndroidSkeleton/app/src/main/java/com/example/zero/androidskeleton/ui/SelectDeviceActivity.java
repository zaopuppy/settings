package com.example.zero.androidskeleton.ui;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
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
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ToggleButton;
import com.example.zero.androidskeleton.R;
import com.example.zero.androidskeleton.bt.BtLeDevice;
import com.example.zero.androidskeleton.bt.BtLeService;
import com.example.zero.androidskeleton.bt.DoorProtocol;
import com.example.zero.androidskeleton.storage.BtDeviceStorage;
import com.example.zero.androidskeleton.utils.Utils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SelectDeviceActivity extends AppCompatActivity {

    private static final String TAG = "SelectDeviceActivity";

    private SimpleArrayAdapter mListViewAdapter;

    private void log(final String msg) {
        Log.i(TAG, msg + '\n');
    }

    private boolean mScanning = false;

    private class MyScanCallback extends ScanCallback {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.e(TAG, "onScanResult");

            BluetoothDevice device = result.getDevice();

            // output scan result
            {
                ScanRecord record = result.getScanRecord();
                if (record != null) {
                    byte[] data = record.getBytes();
                    Log.e(TAG, Utils.b16encode(data));
                } else {
                    Log.e(TAG, "no scan record for " + device.getName());
                }

            }

            int oldSize = mListViewAdapter.getCount();
            mListViewAdapter.add(device);
            if (mAutoButton.isChecked() && mListViewAdapter.getCount() > oldSize) {
                int password = BtDeviceStorage.INSTANCE.get(device.getAddress());
                if (password >= 0) {
                    open(device, password);
                }
            }
        }

        private void open(final BluetoothDevice dev, final int password) {
            final BtLeDevice device = new BtLeDevice(dev);
            device.onReady(new Runnable() {
                @Override
                public void run() {
                    device.onReady(null);
                    device.onCharacteristicChanged(new BtLeDevice.CharacteristicChangedListener() {
                        @Override
                        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic chara) {
                            byte[] value = chara.getValue();
                            if (value == null || value.length <= 0) {
                                // ignore
                                return;
                            }

                            final byte result = chara.getValue()[0];
                            Log.d(TAG, "onCharacteristicChanged: " + result);
                            switch (result) {
                                case DoorProtocol.RESULT_PASSWORD_CORRECT: {
                                    Log.d(TAG, "save password: " + password);
                                    BtDeviceStorage.INSTANCE.put(device.getAddress(), password);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Utils.makeToast(getApplicationContext(), device.getName() + ": 开门密码正确");
                                        }
                                    });
                                    break;
                                }
                                case DoorProtocol.RESULT_PASSWORD_WRONG: {
                                    Log.d(TAG, "bad password clear");
                                    BtDeviceStorage.INSTANCE.put(device.getAddress(), -1);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Utils.makeToast(getApplicationContext(), device.getName() + ": 开门密码错误");
                                        }
                                    });
                                    break;
                                }
                                default:
                                    // ignore
                                    break;
                            }
                            device.disconnectGatt();
                        }
                    });
                    DoorProtocol.openDoor(device, password);
                }
            });
            device.connectGatt(getApplicationContext());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            mScanning = false;
            invalidateOptionsMenu();
        }

        @Override
        public void onScanFailed(int errorCode) {
            mScanning = false;
            invalidateOptionsMenu();
        }

        public boolean isScanning() {
            return mScanning;
        }
    }

    private final MyScanCallback mScanCallback = new MyScanCallback();

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

        setupUiComp();


        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Utils.makeToast(this, "该手机不支持低功耗蓝牙");
            finish();
        }

        BtLeService.INSTANCE.init();
        if (BtLeService.INSTANCE.getAdapter() == null || BtLeService.INSTANCE.getScanner() == null) {
            Utils.makeToast(this, "蓝牙功能不支持或者开关未打开");
            finish();
        }
    }

    private void startScan() {
        mListViewAdapter.clear();
        BtLeService.INSTANCE.startScan(mScanCallback);
        mScanning = true;
        invalidateOptionsMenu();
    }

    private void stopScan() {
        BtLeService.INSTANCE.stopScan(mScanCallback);
        mScanning = false;
        invalidateOptionsMenu();
    }

    private Timer mTimer;
    @Override
    protected void onResume() {
        super.onResume();
        mTimer = new Timer("le-scan-timer");
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mScanning) {
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
        mTimer.cancel();
        stopScan();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_device_menu, menu);
        // if (mScanner.isScanning()) {
        if (mScanning) {
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
                BtLeService.INSTANCE.stopScan(mScanCallback);

                // get selected info
                final BluetoothDevice device = mListViewAdapter.getItem(position);
                log("device: " + device.getName() + ", " + device.getAddress());

                Bundle bundle = new Bundle();
                bundle.putParcelable("device", device);

                Intent intent = new Intent(SelectDeviceActivity.this, ShowDeviceActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
            }
        });
    }
}
