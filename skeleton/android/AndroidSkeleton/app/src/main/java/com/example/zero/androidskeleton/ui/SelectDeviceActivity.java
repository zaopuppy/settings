package com.example.zero.androidskeleton.ui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import com.example.zero.androidskeleton.R;
import com.example.zero.androidskeleton.bt.BluetoothService;
import com.example.zero.androidskeleton.utils.UiUtils;

import java.io.IOException;
import java.util.UUID;

public class SelectDeviceActivity extends Activity {

    private SimpleArrayAdapter mListViewAdapter;
    //private final ArrayList<DeviceInfo> mDeviceList = new ArrayList<>(4);

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }

            String action = intent.getAction();
            if (action == null) {
                return;
            }

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mListViewAdapter.add(device);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mListViewAdapter = new SimpleArrayAdapter(this, R.layout.device_item);

        setContentView(R.layout.activity_select_device);
        setupUiComp();

        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);

        super.onDestroy();
    }

    private void setupUiComp() {
        Button scan_button = (Button) findViewById(R.id.scan_button);
        scan_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothService.INSTANCE.startDiscovery();
            }
        });

        ListView device_list_view = (ListView) findViewById(R.id.device_list_view);
        device_list_view.setAdapter(mListViewAdapter);
        device_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // cancel discovery first
                BluetoothService.INSTANCE.cancelDiscovery();

                // get selected info
                final BluetoothDevice device = mListViewAdapter.getItem(position);
                //UiUtils.makeToast(
                //        getApplicationContext(),
                //        // "position=" + position + ", id=" + id);
                //        "device: " + device.getName() + ", " + device.getAddress());

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            UiUtils.makeToast(getApplicationContext(), "begin");

                            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID.randomUUID());
                            socket.connect();
                            UiUtils.makeToast(getApplicationContext(), "success");
                        } catch (IOException e) {
                            e.printStackTrace();
                            UiUtils.makeToast(getApplicationContext(), "failed: " + e.getMessage());
                        }
                    }
                }).start();
            }
        });
    }
}
