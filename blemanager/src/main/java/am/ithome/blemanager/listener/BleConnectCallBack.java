package am.ithome.blemanager.listener;

import android.bluetooth.le.ScanResult;

public interface BleConnectCallBack {
    void onConnected(boolean isConnected, ScanResult result);
}