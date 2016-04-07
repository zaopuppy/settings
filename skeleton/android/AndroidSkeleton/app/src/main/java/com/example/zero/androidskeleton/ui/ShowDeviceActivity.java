package com.example.zero.androidskeleton.ui;

import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.zero.androidskeleton.R;
import com.example.zero.androidskeleton.utils.Utils;

import java.util.List;

public class ShowDeviceActivity extends AppCompatActivity {
    private static final String TAG = "ShowDeviceActivity";

    private void log(String msg) {
        Log.i(TAG, msg + '\n');
    }

    public static class BtLeDevice {
        private final BluetoothDevice mDevice;
        private BluetoothGatt mGatt = null;

        public interface Listener<T> {
            void onResult(T result);
        }

        public static class MyBtGattCallback extends BluetoothGattCallback {

            private State mState = State.DISCONNECTED;
            private BluetoothGattCharacteristic mCharacteristic;

            public State getState() {
                return mState;
            }

            public void setState(State state) {
                this.mState = state;
            }

            public enum State {
                DISCONNECTED,
                CONNECTING,
                CONNECTED,
                DISCOVERING_SERVICE,
                SERVICE_DISCOVERED, // ready
                DISCONNECTING,
            }

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                Log.d(TAG, "onConnectionStateChange: status=" + status + ", newState=" + newState);
                switch (newState) {
                    case BluetoothProfile.STATE_DISCONNECTED:
                        setState(State.DISCONNECTED);
                        break;
                    case BluetoothProfile.STATE_CONNECTING:
                        setState(State.CONNECTING);
                        break;
                    case BluetoothProfile.STATE_CONNECTED:
                        gatt.discoverServices();
                        setState(State.CONNECTING);
                        break;
                    case BluetoothProfile.STATE_DISCONNECTING:
                        setState(State.DISCONNECTING);
                        break;
                    default:
                        // ignore
                        break;
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    gatt.disconnect();
                    return;
                }

                List<BluetoothGattService> service_list = gatt.getServices();
                if (service_list.size() <= 0) {
                    gatt.disconnect();
                    return;
                }

                List<BluetoothGattCharacteristic> characteristic_list = service_list.get(0).getCharacteristics();
                if (characteristic_list.size() <= 0) {
                    gatt.disconnect();
                    return;
                }

                mCharacteristic = characteristic_list.get(0);
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                log("onCharacteristicRead");
                log("characteristic uuid=" + characteristic.getUuid() + ", data=" + Utils.b16encode(characteristic.getValue()));
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                log("onCharacteristicWrite");
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                log("onCharacteristicChanged");
            }
        };

        public BtLeDevice(BluetoothDevice device) {
            mDevice = device;
        }

        public void connectGatt(Context context, Listener<Integer> listener) {
            mGatt = mDevice.connectGatt(context, false, new BluetoothGattCallback() {

            });
        }

        public void writeCharacteristic() {
            mGatt.writeCharacteristic();
        }

        public void readCharacteristic() {
            mGatt.readCharacteristic();
        }
    }

    private BluetoothDevice mDevice = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_device);

        Intent intent = getIntent();
        mDevice = intent.getParcelableExtra("device");
        if (mDevice == null) {
            Utils.makeToast(this, "no device supplied");
            finish();
            return;
        }

        setUiComp();
    }

    private void setUiComp() {
        TextView nameView = (TextView) findViewById(R.id.name);
        nameView.setText(mDevice.getName());

        TextView addressView = (TextView) findViewById(R.id.address);
        addressView.setText(mDevice.getAddress());

        Button openButton = (Button) findViewById(R.id.open_button);
        openButton.setEnabled(false);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mDevice.isReady()) {
                    Utils.makeToast(ShowDeviceActivity.this, "device is not yet ready");
                    return;
                }

                mDevice
            }
        });
    }

}
