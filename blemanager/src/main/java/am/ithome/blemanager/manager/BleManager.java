package am.ithome.blemanager.manager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

import am.ithome.blemanager.listener.BleConnectCallBack;

public class BleManager {
    /**
     * Bluetooth objects
     */
    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;
    private BluetoothLeScanner btScanner;
    private BluetoothGatt mBluetoothGatt;
    /**
     * Bluetooth listener
     */
    private BleConnectCallBack bleConnectCallBack;

    /**
     * Store all service and Characteristic
     */
    private List<BluetoothGattCharacteristic> chars = null;
    private ArrayList<BluetoothGattCharacteristic> mReadCharacteristics, mWriteCharacteristics, mNotifyCharacteristics;
    private ArrayList<BluetoothGattService> mReadServices, mWriteServices, mNotifyServices;
    /**
     * Characteristic write and read values
     */
    private String writeCharacteristicValue;
    /**
     * R S S I Params
     */
    private Handler mTimerHandler = new Handler();
    private int RSSI_UPDATE_TIME_INTERVAL = 2000;
    private boolean mTimerEnabled = false;

    private String macAddress;
    private Context context;
    private boolean mConnected = false;
    private final static int REQUEST_ENABLE_BT = 1;

    @SuppressLint("NewApi")
    public BleManager initBle(Context context, Activity activity) {
        this.context = context;
        btManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();
        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        return this;
    }

    public BleManager startConnect(String macAddress, BleConnectCallBack bleConnectCallBack) {
        this.macAddress = macAddress;
        this.bleConnectCallBack = bleConnectCallBack;
        AsyncTask.execute(new Runnable() {
            @SuppressLint("NewApi")
            @Override
            public void run() {
                btScanner.startScan(leScanCallback);
            }
        });
        return this;
    }

    private ScanCallback leScanCallback = new ScanCallback() {
        @SuppressLint("NewApi")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (result != null && result.getDevice() != null && result.getDevice().getAddress() != null) {
                if (result.getDevice().getAddress().equals(macAddress)) {
                    mBluetoothGatt = result.getDevice().connectGatt(context, true, mGattCallback);
                }
            }
        }
    };

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                bleConnectCallBack.onConnected(true, null);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            for (BluetoothGattService service : getSupportedGattServices()) {
                chars = service.getCharacteristics();
                if (chars.size() > 0) {
                    for (BluetoothGattCharacteristic characteristic : chars) {
                        if (characteristic != null) {
                            final int charaProp = characteristic.getProperties();
                            final int read = charaProp & BluetoothGattCharacteristic.PROPERTY_READ;
                            final int write = charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE;
                            final int notify = charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY;
                            if (read > 0) {
                                mReadServices.add(service);
                                mReadCharacteristics.add(characteristic);
                            }

                            if (notify > 0) {
                                mNotifyServices.add(service);
                                mNotifyCharacteristics.add(characteristic);
                            }

                            if (write > 0) {
                                mWriteServices.add(service);
                                mWriteCharacteristics.add(characteristic);
                            }
                        }

                    }
                     BluetoothGattCharacteristic bluetoothGattCharacteristic =
                             chars.get(0);
                    if (bluetoothGattCharacteristic !=null && bluetoothGattCharacteristic.getDescriptors() !=null) {
                        for (BluetoothGattDescriptor bluetoothGattDescriptor : bluetoothGattCharacteristic.getDescriptors()) {
                            bluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            mBluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, true);
                        }
                    }
                }
            }
            writeCharacteristic(writeCharacteristicValue.getBytes(), chars);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            System.out.println("wdnwndkjwkdm");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            System.out.println("dhbwhdbwhbd---->"+"called onCharacteristicWrite method");

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            System.out.println("bnfdenhjfedfnk");

        }
    };

    private void writeCharacteristic(byte[] bytes, List<BluetoothGattCharacteristic> chars) {
        if (chars !=null && chars.size() > 0) {
            if (bytes.length >0) {
                System.out.println("dhbwhdbwhbd---->"+"writed");
                BluetoothGattCharacteristic characteristic = chars.get(0);
                characteristic.setValue(bytes);
                mBluetoothGatt.writeCharacteristic(characteristic);
            }
        }
    }

    public void subscribeReadCharacteristic() {
        System.out.println("wkdnkwndkwn");
    }

    private void initCharacteristic() {
        mReadServices = new ArrayList<>();
        mReadCharacteristics = new ArrayList<>();
        mWriteServices = new ArrayList<>();
        mWriteCharacteristics = new ArrayList<>();
        mNotifyServices = new ArrayList<>();
        mNotifyCharacteristics = new ArrayList<>();
    }

    public void handleRequests() {
        readPeriodicallyRssiValue(true);
        initCharacteristic();
        mBluetoothGatt.discoverServices();
    }

    public void sendData(String value) {
        writeCharacteristicValue = value;
    }

    private List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    private void ReadRSSI() {
        if (mBluetoothGatt == null) return;
        mBluetoothGatt.readRemoteRssi();

    }

    private void readPeriodicallyRssiValue(final boolean repeat) {
        mTimerEnabled = repeat;
        if (!mConnected || !mTimerEnabled) {
            mTimerEnabled = false;
            return;
        }
        mTimerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mTimerEnabled)
                    ReadRSSI();
                readPeriodicallyRssiValue(mTimerEnabled);
            }
        }, RSSI_UPDATE_TIME_INTERVAL);
    }
}
