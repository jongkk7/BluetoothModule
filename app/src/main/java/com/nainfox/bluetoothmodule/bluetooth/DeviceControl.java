package com.nainfox.bluetoothmodule.bluetooth;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.nainfox.bluetoothmodule.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by yjk on 2017. 11. 15..
 */
public class DeviceControl {
    private final static String TAG = "### DeviceControl";
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private String mDeviceName;
    private String mDeviceAddress;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    public BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    public boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    public DeviceControl(String address){
        this.mDeviceAddress = address;
    }

    // Code to manage Service lifecycle.
    public final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "블루투스 초기화에 실패했습니다.");
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected : 서비스 연결해제");
            mBluetoothLeService = null;
        }
    };


    /**
     * 연결 상태 반환
     * @return
     */
    public boolean getConnectionStatus(){
        if(mBluetoothLeService.getStatus() == BluetoothLeService.STATE_CONNECTED){
            return true;
        }

        return false;
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    /**
     * 리시버 ( 연결 성공시 , 연결 해제 , 장치 발견 시 , 데이터 수신 시 )
     */
    public BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                Log.d(TAG, "connected!");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                Log.d(TAG, "disconnected!");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // 장치 연결이 됐을 경우
                // 해당 UUID의 설정값을 true로 변경해준다.
                Log.d("###", "ACTION_GATT_SERVICES_DISCOVERED!!!!");
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
                new Handler().postDelayed(new Runnable()
                {
                    public void run()
                    {

                        BluetoothGattCharacteristic localBluetoothGattCharacteristic;
                        for(int i=0 ; i<=4 ; i++){
                            for(int j=0; j<4 ; j++){
                                try {
                                    localBluetoothGattCharacteristic = (BluetoothGattCharacteristic) ((ArrayList) mGattCharacteristics.get(i)).get(j);
                                    mBluetoothLeService.setCharacteristicNotification(localBluetoothGattCharacteristic, true);
                                }catch (Exception e){
                                    //Log.d(TAG,"index error : " + e.getMessage());
                                }
                            }
                        }

                    }
                }, 1000L);
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.d("### getStringExtra", intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };


    public void sendData(String data){
        if (this.mGattCharacteristics != null)
        {
            BluetoothGattCharacteristic localBluetoothGattCharacteristic;

            for(int i=0; i<=4; i++){
                for(int j=0; j<=4; j++){
                    try {
                        if( i==0 && j==0 ) continue;
                        localBluetoothGattCharacteristic = (BluetoothGattCharacteristic) ((ArrayList) this.mGattCharacteristics.get(i)).get(j);
                        this.mBluetoothLeService.writeCharacteristics(localBluetoothGattCharacteristic, data);
                    }catch (Exception e){
                        //Log.e(TAG,"sendData() error : " + e.getMessage());
                    }
                }
            }

        }
    }

    /**
     * 연결된 디바이스 이름 변경
     * @param name
     */
    public void setDeviceName(String name){
        try {
            BluetoothGattCharacteristic localBluetoothGattCharacteristic = (BluetoothGattCharacteristic) ((ArrayList) this.mGattCharacteristics.get(0)).get(0);
            this.mBluetoothLeService.writeCharacteristics(localBluetoothGattCharacteristic, name);
        }catch (Exception e){
            Log.e(TAG,"setDevieName() error : " + e.getMessage());
        }
    }



    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    public void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = "Unknown service";
        String unknownCharaString = "Unknown characteristic";
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }

    public IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
