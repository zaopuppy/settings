package com.example.zero.androidskeleton.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.*;
import android.content.Intent;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.zero.androidskeleton.R;
import com.example.zero.androidskeleton.bt.DoorProtocol;

import java.util.UUID;
import java.util.zip.CRC32;

/**
 * http://stackoverflow.com/questions/18315508/is-it-possible-in-android-to-transmit-broadcast-mode-in-ble
 *
 * Look like the answer for android 4.3 and 4.4 is no.
 * Android 4.3 and 4.4 does not support BLE peripheral/broadcaster role
 * see https://code.google.com/p/android/issues/detail?id=59693 and https://code.google.com/p/android/issues/detail?id=58582
 * allow see this stackoverflow thread about the same issue
 * Android 4.3 as a Bluetooth LE Peripheral (http://stackoverflow.com/questions/18008507/android-4-3-as-a-bluetooth-le-peripheral)
 */
public class BroadcastDeviceActivity extends AppCompatActivity {

    private TextView logView_;

    private void showMessage(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logView_.append(msg + '\n');
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast_device);

        logView_ = (TextView) findViewById(R.id.log_view);
        assert logView_ != null;

        Button openButton = (Button) findViewById(R.id.open_button);
        assert openButton != null;
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDoor();
            }
        });

        Button gotoButton = (Button) findViewById(R.id.goto_button);
        assert gotoButton != null;
        gotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BroadcastDeviceActivity.this, SelectDeviceActivity.class);
                startActivity(intent);
            }
        });

    }

    private void openDoor1() {
        // get scanner
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();

        // scan
        // scanner
        throw new IllegalAccessError();
    }

    private void openDoor() {
        // get advertiser
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothLeAdvertiser advertiser = adapter.getBluetoothLeAdvertiser();
        if (!adapter.isMultipleAdvertisementSupported() || advertiser == null) {
            showMessage("bluetooth is not switched on, or this device doesn't support advertisement");
            return;
        }

        showMessage("supports advertisement");

        // build settings
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .build();

        // build data
        // byte[] rawData = DoorProtocol.openDoor(0);
        byte[] rawData = new byte[] {
            0x16, // length
            0x43, 0x01, // control
            0x01, // sequence
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, // password
            0x01, 0x08, 0x06, 0x00, 0x02, 0x05, 0x00, 0x01, 0x09, 0x02, 0x04, // phone number (should be BCD encoded)
        };
        CRC32 crc32 = new CRC32();
        crc32.update(rawData);
        showMessage("raw data length: " + rawData.length);
        ParcelUuid uuid = ParcelUuid.fromString("887B0D8B-768C-42FB-B3F5-1B87E3F16EAE");
        AdvertiseData data = new AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceData(uuid, rawData)
            .build();

        advertiser.startAdvertising(settings, data, new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                showMessage("onStartSuccess: \n" + settingsInEffect.toString());
            }

            @Override
            public void onStartFailure(int errorCode) {
                showMessage("onStartFailure: " + errorCode);
            }
        });
    }
}
