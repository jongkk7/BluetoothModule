package com.nainfox.bluetoothmodule.bluetooth;

/**
 * Created by yjk on 2017. 12. 4..
 */


import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * BluetoothController
 * 목적 : 블루투스 장치 검색, 연결, 통신을 전반적으로 관리하는 컨트롤러
 */
public class BluetoothController {
    private static final String TAG = "###BluetoothController";
    private boolean isGranted = false;    // 권한 확인 변수
    private boolean isConnected = false;  // 연결 확인 변수

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private Activity activity;
    private Config config;

    private BluetoothLeService bluetoothLeService;
    private ServiceConnection serviceConnection;

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();   // 장치 이름, 주소 리스트

    public BluetoothController(Activity activity){
        this.activity = activity;
        config = new Config();
    }

    public BluetoothController(Activity activity, Config config){
        this.activity = activity;
        this.config = config;
    }

    /**
     * 블루투스 장치 검색
     * 호출 액티비티에서 onActivityForResult() 메소드를 작성해줄 것
     * 장치이름, 주소값 저장 / 사용
     */
    public void startScan(){
        Intent i = new Intent(activity, DeviceScanActivity.class);
        i.putExtra(Data.CONFIG, config);
        activity.startActivityForResult(i, Data.REQUEST_ENABLE_BT);
    }


    /**
     * 주소값으로 연결 시도
     * 액티비티에서 블루투스 연결 서비스를 등록
     */
    public ServiceConnection getBluetoothLeServiceConnection(final String deviceAddress){
        serviceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
                if (!bluetoothLeService.initialize()) {
                    Log.e(TAG, "블루투스 초기화에 실패했습니다.");
                    isConnected = false;
                }
                // Automatically connects to the device upon successful start-up initialization.
                bluetoothLeService.connect(deviceAddress);

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(TAG, "onServiceDisconnected : 서비스 연결해제");
                bluetoothLeService = null;
                isConnected = false;
            }
        };

        return serviceConnection;
    }

    public ServiceConnection getServiceConnection(){
        return serviceConnection;
    }


    /**
     * 연결 확인 변수
     */
    public void setConnected(boolean isConnected){
        this.isConnected = isConnected;
    }
    public boolean getConnected(){
        return isConnected;
    }


    public void initBluetoothLeService(){
        this.bluetoothLeService = null;
    }
    public BluetoothLeService getBluetoothLeService(){
        return bluetoothLeService;
    }

    public ArrayList<ArrayList<BluetoothGattCharacteristic>> getmGattCharacteristics(){
        return mGattCharacteristics;
    }


    /**
     * 장치 등록 서비스
     * @param gattServices
     */
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

    /**
     * 블루투스 통신 액션 등록
     * @return
     */
    public IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }



}
