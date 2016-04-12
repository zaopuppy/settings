package com.example.zero.androidskeleton.bt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import com.example.zero.androidskeleton.utils.Utils;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * Created by zero on 2016/4/8.
 */
public class BtLeDevice extends BluetoothGattCallback {

    private static final String TAG = "BtLeDevice";

    //private final ThreadPoolExecutor mExecutor;

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


    private interface Task {
        boolean exec();
        void onReadResult(BluetoothGattCharacteristic characteristic, int status);
        void onWriteResult(BluetoothGattCharacteristic characteristic, int status);
    }

    private static class ReadTask implements Task {
        private final BluetoothGattCharacteristic characteristic;
        private final Listener<byte[]> listener;
        private final BluetoothGatt gatt;

        private ReadTask(
            BluetoothGatt gatt,
            BluetoothGattCharacteristic characteristic,
            Listener<byte[]> listener) {
            this.gatt = gatt;
            this.characteristic = characteristic;
            this.listener = listener;
        }

        @Override
        public boolean exec() {
            if (!innerReadCharacteristic(gatt, characteristic)) {
                return false;
            }

            return true;
        }

        @Override
        public void onReadResult(BluetoothGattCharacteristic characteristic, int status) {
            if (!this.characteristic.equals(characteristic)) {
                throw new IllegalStateException(
                    "expect=" + BtLeService.uuidStr(this.characteristic.getUuid())
                        + ", actual=" + BtLeService.uuidStr(characteristic.getUuid()));
            }
            if (status != BluetoothGatt.GATT_SUCCESS) {
                listener.onResult(null);
                return;
            }

            listener.onResult(characteristic.getValue());
        }

        @Override
        public void onWriteResult(BluetoothGattCharacteristic characteristic, int status) {
            throw new IllegalStateException("unexpected write result");
        }
    }

    private static class WriteTask implements Task {

        private final BluetoothGatt gatt;
        private final BluetoothGattCharacteristic characteristic;
        private final byte[] data;
        private final Listener<Boolean> listener;

        private WriteTask(
            BluetoothGatt gatt,
            BluetoothGattCharacteristic characteristic,
            byte[] data,
            Listener<Boolean> listener) {
            this.gatt = gatt;
            this.characteristic = characteristic;
            this.data = data;
            this.listener = listener;
        }

        @Override
        public boolean exec() {
            return innerWriteCharacteristic(gatt, characteristic, data);
        }

        @Override
        public void onReadResult(BluetoothGattCharacteristic characteristic, int status) {
            throw new IllegalStateException("unexpected read result");
        }

        @Override
        public void onWriteResult(BluetoothGattCharacteristic characteristic, int status) {
            if (!this.characteristic.equals(characteristic)) {
                throw new IllegalStateException(
                    "expect=" + BtLeService.uuidStr(this.characteristic.getUuid())
                        + ", actual=" + BtLeService.uuidStr(characteristic.getUuid()));
            }

            if (status != BluetoothGatt.GATT_SUCCESS) {
                listener.onResult(false);
                return;
            }

            listener.onResult(true);
        }
    }

    private final Object mTaskLock = new Object();
    private final Queue<Task> mTaskQueue = new ConcurrentLinkedQueue<>();
    private Task mCurrentTask = null;

    private void processTask() {
        synchronized (mTaskLock) {
            while (mCurrentTask == null) {
                Task task = mTaskQueue.poll();
                if (task == null) {
                    Log.d(TAG, "empty task queue");
                    return;
                }

                if (task.exec()) {
                    mCurrentTask = task;
                } else {
                    Log.e(TAG, "failed to execute task");
                }
            }
        }
    }

    ///**
    // * read queue for one operation every time
    // *
    // * TODO: do we need a lock?
    // */
    //private final Queue<ReadTask> mReadQueue = new ConcurrentLinkedQueue<>();
    //
    //// FIXME: no need to use queue
    //private final Queue<ReadTask> mWaitReadQueue = new ConcurrentLinkedQueue<>();
    //
    ///**
    // * same reason as read queue
    // */
    //private final List<WriteTask> onWriteQueue = new ArrayList<>(8);

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
        //mExecutor = new ThreadPoolExecutor(
        //    1, 1, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(2));
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

    //public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value) {
    //    characteristic.setValue(value);
    //    return mGatt.writeCharacteristic(characteristic);
    //}

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

    public boolean makeNotify(BluetoothGattCharacteristic characteristic) {
        //int prop = characteristic.getProperties();
        //if (Utils.isFlagSet(prop, BluetoothGattCharacteristic.PROPERTY_NOTIFY)) {
        //    Log.d(TAG, BtLeService.uuidStr(characteristic.getUuid()) + " already notify");
        //    return true;
        //}

        return mGatt.setCharacteristicNotification(characteristic, true);
    }

    private static boolean checkProperties(
        BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int expectProp) {

        int prop = characteristic.getProperties();

        Log.d(TAG,
            "checkProperties uuid=" + BtLeService.uuidStr(characteristic.getUuid())
                + ", prop=" + prop
                + ", expect-prop=" + expectProp);

        Log.d(TAG, "current prop=" + prop);
        if (!Utils.isFlagSet(prop, expectProp)) {
            Log.d(TAG, "not expect prop: " + BtLeService.uuidStr(characteristic.getUuid()));
            return false;
        }

        if (!Utils.isFlagSet(prop, BluetoothGattCharacteristic.PROPERTY_NOTIFY)) {
            Log.d(TAG, "no notify property, modify");
            if (!gatt.setCharacteristicNotification(characteristic, true)) {
                Log.e(TAG, "failed to set notify");
                return false;
            }
        }

        return true;
    }

    private static boolean innerReadCharacteristic(
        BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

        if (!checkProperties(gatt, characteristic, BluetoothGattCharacteristic.PROPERTY_READ)) {
            Log.e(TAG, "failed to check properties");
            return false;
        }

        return gatt.readCharacteristic(characteristic);
    }

    private static boolean innerWriteCharacteristic(
        BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] data) {

        if (!checkProperties(gatt, characteristic, BluetoothGattCharacteristic.PROPERTY_WRITE)) {
            Log.e(TAG, "failed to check properties");
            return false;
        }

        if (!characteristic.setValue(data)) {
            Log.e(TAG, "failed to store the data to write");
            return false;
        }

        return gatt.writeCharacteristic(characteristic);
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic, Listener<byte[]> listener) {
        Log.d(TAG, "readCharacteristic: " + BtLeService.uuidStr(characteristic.getUuid()));
        mTaskQueue.offer(new ReadTask(mGatt, characteristic, listener));
        processTask();
    }

    // 128bit
    // 0x00000000000000000000000000020304
    //
    // 64bit, 8bytes
    // 0x0A0000000000000B
    //
    // FIXME: Are all these callback called in the same thread? if not, there will be some problem of this
    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.d(TAG, "onCharacteristicRead: " + BtLeService.uuidStr(characteristic.getUuid()) + " status=" + status);

        synchronized (mTaskLock) {
            if (mCurrentTask == null) {
                throw new IllegalStateException("no waiting task");
            }
            mCurrentTask.onReadResult(characteristic, status);
            mCurrentTask = null;
            processTask();
        }
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] data, Listener<Boolean> listener) {
        Log.d(TAG, "writeCharacteristic: " + BtLeService.uuidStr(characteristic.getUuid()));
        mTaskQueue.offer(new WriteTask(mGatt, characteristic, data, listener));
        processTask();
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.d(TAG, "onCharacteristicWrite: " + BtLeService.uuidStr(characteristic.getUuid()) + " status=" + status);

        synchronized (mTaskLock) {
            if (mCurrentTask == null) {
                throw new IllegalStateException("no waiting task");
            }
            mCurrentTask.onWriteResult(characteristic, status);
            mCurrentTask = null;
            processTask();
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "onCharacteristicChanged");
    }
}
