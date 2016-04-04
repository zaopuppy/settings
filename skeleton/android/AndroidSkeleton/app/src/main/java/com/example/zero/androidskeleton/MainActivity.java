package com.example.zero.androidskeleton;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.zero.androidskeleton.bt.BluetoothService;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private BluetoothService mBtService = null;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }

            String action = intent.getAction();
            Bundle bundle = intent.getExtras();
            if (action == null || bundle == null) {
                return;
            }

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int old_state = bundle.getInt(
                        BluetoothAdapter.EXTRA_PREVIOUS_STATE,
                        BluetoothAdapter.STATE_OFF);
                int new_state = bundle.getInt(
                        BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.STATE_OFF);
                makeToast(String.format(
                        Locale.US, "%d --> %d", old_state, new_state));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerReceiver(
                mReceiver,
                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        setContentView(R.layout.activity_main);

        mBtService = new BluetoothService(getApplicationContext());

        setupUiComp();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void setupUiComp() {
        Button start_bt_button = (Button) findViewById(R.id.start_bt_service_button);
        start_bt_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBtService();
            }
        });


        Button stop_bt_button = (Button) findViewById(R.id.stop_bt_service_button);
        stop_bt_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopBtService();
            }
        });
    }

    private void makeToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private void startBtService() {
        BluetoothService.Code code = mBtService.start();
        if (code == BluetoothService.Code.BT_NOT_ENABLED) {
            makeToast("Bluetooth is not enabled");
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(intent);
            return;
        }

    private void stopBtService() {
        //
    }


}
