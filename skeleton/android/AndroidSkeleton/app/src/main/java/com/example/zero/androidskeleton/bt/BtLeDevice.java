package com.example.zero.androidskeleton.bt;

import android.bluetooth.*;
import android.content.Context;
import android.util.Log;
import com.example.zero.androidskeleton.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by zero on 2016/4/8.
 */
public class BtLeDevice extends BluetoothGattCallback {

    private static final String TAG = "BtLeDevice";

    public enum State {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        DISCOVERING_SERVICE,
        READY,
        DISCONNECTING,
    }

    public void onReady(Runnable runnable) {
        mReadyEventHandler = runnable;
    }

    public void onDisconnected(Runnable runnable) {
        mDisconnectedEventHandler = runnable;
    }

    private final BluetoothDevice mDevice;

    private State mState = State.DISCONNECTED;
    private BluetoothGatt mGatt = null;
    //private BluetoothGattCharacteristic mCharacteristic;

    private static class ReadTask {
        private final BluetoothGattCharacteristic characteristic;
        private final Listener<byte[]> listener;

        private ReadTask(BluetoothGattCharacteristic characteristic, Listener<byte[]> listener) {
            this.characteristic = characteristic;
            this.listener = listener;
        }
    }

    private static class WriteTask {
        private final Listener<Integer> listener;

        private WriteTask(Listener<Integer> listener) {
            this.listener = listener;
        }
    }

    /**
     * read queue for one operation every time
     *
     * TODO: do we need a lock?
     */
    private final Queue<ReadTask> mReadQueue = new ConcurrentLinkedQueue<>();

    private final Queue<ReadTask> mWaitReadQueue = new ConcurrentLinkedQueue<>();

    /**
     * same reason as read queue
     */
    private final List<WriteTask> onWriteQueue = new ArrayList<>(8);

    /**
     * current saved read listener
     * @param <T>
     */
    //private Listener<byte[]> mReadListener;

    // TODO: currently only support one device
    private Runnable mReadyEventHandler = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "no ready event handler was set");
        }
    };

    private Runnable mDisconnectedEventHandler = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "no disconnected event handler was set");
        }
    };

    public interface Listener<T> {
        void onResult(T result);
    }

    public BtLeDevice(BluetoothDevice device) {
        mDevice = device;
    }

    public String getName() {
        return mDevice.getName();
    }

    public String getAddress() {
        return mDevice.getAddress();
    }

    public void connectGatt(Context context) {
        mGatt = mDevice.connectGatt(context, false, this);
    }

    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        //if (mCharacteristic == null) {
        //    return false;
        //}
        //
        //return mGatt.writeCharacteristic(mCharacteristic);
        return false;
    }

    public State getState() {
        return mState;
    }

    private void setState(State state) {
        if (state == mState) {
            return;
        }
        this.mState = state;

        switch (state) {
            case READY:
                mReadyEventHandler.run();
                break;
            case DISCONNECTING:
            case DISCONNECTED:
                mDisconnectedEventHandler.run();
                break;
            default:
                // ignore
                break;
        }
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

    public List<BluetoothGattService> getServiceList() {
        return mGatt.getServices();
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.e(TAG, "failed to discover service");
            gatt.disconnect();
            return;
        }

        List<BluetoothGattService> service_list = gatt.getServices();

        //if (service_list.size() <= 0) {
        //    Log.e(TAG, "no service was discovered");
        //    gatt.disconnect();
        //    return;
        //}

        Log.e(TAG, "device is ready, service count: " + service_list.size());

        setState(State.READY);
    }

    private static boolean innerReadCharacteristic(
            BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

        int prop = characteristic.getProperties();
        if (!Utils.isFlagSet(prop, BluetoothGattCharacteristic.PROPERTY_READ)) {
            Log.d(TAG, "no read permission");
            return false;
        }

        if (!Utils.isFlagSet(prop, BluetoothGattCharacteristic.PROPERTY_NOTIFY)) {
            Log.d(TAG, "no notify property, modify");
            if (!gatt.setCharacteristicNotification(characteristic, true)) {
                Log.e(TAG, "failed to set notify");
                return false;
            }
        }
        return gatt.readCharacteristic(characteristic);
    }

    private void fireRead() {
        // FIXME: wrong! this may cause concurrent problem
        if (mWaitReadQueue.size() > 0) {
            Log.d(TAG, "still waiting read event");
            return;
        }

        ReadTask task = mReadQueue.poll();
        if (task == null) {
            Log.i(TAG, "empty read queue");
            return;
        }

        if (innerReadCharacteristic(mGatt, task.characteristic)) {
            Log.e(TAG, "failed to read characteristic");
            task.listener.onResult(null);
            fireRead();
        } else {
            mWaitReadQueue.add(task);
        }
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic, Listener<byte[]> listener) {
        Log.d(TAG, "readCharacteristic: " + characteristic.getUuid());
        mReadQueue.offer(new ReadTask(characteristic, listener));
        fireRead();
    }

    // FIXME: Are all these callback called in the same thread? if not, there will be some problem of this
    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.d(TAG, "onCharacteristicRead");
        Log.d(TAG, "characteristic uuid=" + characteristic.getUuid());

        ReadTask task = mWaitReadQueue.poll();
        if (!task.characteristic.equals(characteristic)) {
            throw new IllegalStateException("expect=" + characteristic.getUuid() + ", actual=" + task.characteristic.getUuid());
        }
        task.listener.onResult(characteristic.getValue());

        fireRead();
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.d(TAG, "onCharacteristicWrite");
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "onCharacteristicChanged");
    }
}
