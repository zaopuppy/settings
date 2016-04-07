package com.example.zero.androidskeleton.ui;

import android.app.Activity;
import android.bluetooth.*;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.example.zero.androidskeleton.R;
import com.example.zero.androidskeleton.bt.BluetoothLeService;
import com.example.zero.androidskeleton.utils.Utils;

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

    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            mListViewAdapter.add(device);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mListViewAdapter = new SimpleArrayAdapter(this, R.layout.device_item);

        setContentView(R.layout.activity_select_device);
        setupUiComp();

    }

    @Override
    protected void onPause() {
        BluetoothLeService.INSTANCE.stopScan(mScanCallback);

        super.onPause();
    }

    private void setupUiComp() {
        final Button scan_button = (Button) findViewById(R.id.scan_button);
        scan_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scan_button.setEnabled(false);
                // mListViewAdapter.clear();
                BluetoothLeService.INSTANCE.startScan(mScanCallback);
            }
        });

        final Button clean_button = (Button) findViewById(R.id.clean_button);
        clean_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scan_button.setEnabled(true);
                BluetoothLeService.INSTANCE.stopScan(mScanCallback);
            }
        });

        ListView device_list_view = (ListView) findViewById(R.id.device_list_view);
        device_list_view.setAdapter(mListViewAdapter);
        device_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothLeService.INSTANCE.stopScan(mScanCallback);

                // get selected info
                final BluetoothDevice device = mListViewAdapter.getItem(position);
                log("device: " + device.getName() + ", " + device.getAddress());

                Intent intent = new Intent(SelectDeviceActivity.this, ShowDeviceActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("device", device);


                // device.connectGatt(getApplicationContext(), false, mGattCallback);
            }
        });
    }
}
