package com.example.zero.androidskeleton.ui;

import android.app.ActionBar;
import android.bluetooth.*;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import com.example.zero.androidskeleton.R;
import com.example.zero.androidskeleton.bt.BtLeService;

import java.util.List;

public class SelectDeviceActivity extends AppCompatActivity {
    private static final String TAG = "SelectDeviceActivity";

    private SimpleArrayAdapter mListViewAdapter;

    private void log(final String msg) {
        //runOnUiThread(new Runnable() {
        //    @Override
        //    public void run() {
        //        mLogView.append(msg + '\n');
        //    }
        //});
        Log.i(TAG, msg + '\n');
    }

    private boolean mScanning = false;

    private class MyScanCallback extends ScanCallback {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.e(TAG, "type=" + callbackType);
            BluetoothDevice device = result.getDevice();
            mListViewAdapter.add(device);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mListViewAdapter = new SimpleArrayAdapter(this, R.layout.select_list_item_device);

        setContentView(R.layout.activity_select_device);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Select Device");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setupUiComp();

    }

    @Override
    protected void onPause() {
        BtLeService.INSTANCE.stopScan(mScanCallback);
        mScanning = false;

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mListViewAdapter.clear();
        BtLeService.INSTANCE.startScan(mScanCallback);
        mScanning = true;
        invalidateOptionsMenu();
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
            case R.id.menu_scan:
                mListViewAdapter.clear();
                BtLeService.INSTANCE.startScan(mScanCallback);
                mScanning = true;
                invalidateOptionsMenu();
                break;
            case R.id.menu_stop:
                BtLeService.INSTANCE.stopScan(mScanCallback);
                mScanning = false;
                invalidateOptionsMenu();
                break;
            default:
                break;
        }
        return true;
    }

    private void setupUiComp() {
        //final Button scan_button = (Button) findViewById(R.id.scan_button);
        //assert scan_button != null;
        //scan_button.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        scan_button.setEnabled(false);
        //        // mListViewAdapter.clear();
        //        BtLeService.INSTANCE.startScan(mScanCallback);
        //    }
        //});
        //
        //final Button clean_button = (Button) findViewById(R.id.clean_button);
        //assert clean_button != null;
        //clean_button.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        scan_button.setEnabled(true);
        //        BtLeService.INSTANCE.stopScan(mScanCallback);
        //    }
        //});

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
