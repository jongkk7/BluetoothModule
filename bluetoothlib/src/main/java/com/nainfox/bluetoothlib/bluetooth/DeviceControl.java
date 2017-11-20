package com.nainfox.bluetoothlib.bluetooth;

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

import com.nainfox.bluetoothlib.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by yjk on 2017. 11. 15..
 */

public class DeviceControl {
    private final String TAG = "###DeviceControl";
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private Context context;

    private String deviceName;
    private String deviceAddress;

    public BluetoothLeService mBluetoothLeService;
    public ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private ServiceConnection serviceConnection;        // 연결
    public BroadcastReceiver mGattUpdateReceiver;      // 메세지 리시버


    /**
     * onResume에서 레지스터 등록 ( registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter()); )
     * 리시버 등록 initReceiver(callback);
     * onPause() / onDestroy() 에서 unregisterReceiver(mGattUpdateReceiver);
     *
     * @param context
     * @param deviceName
     * @param deviceAddress
     */
    public DeviceControl(Context context, String deviceName, final String deviceAddress){
        this.context = context;
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;

        initConnect();
    }

    private void initConnect(){
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
                if (!mBluetoothLeService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                }
                // Automatically connects to the device upon successful start-up initialization.
                mBluetoothLeService.connect(deviceAddress);

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mBluetoothLeService = null;
            }
        };
    }

    public void initReceiver(final ReceiverCallback callback){
        mGattUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                    mConnected = true;
                    callback.getData(Data.CONNECT);
                } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    mConnected = false;
                    callback.getData(Data.DISCONNECT);
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
                            for(int i=0 ; i <= 5 ; i++){
                                for(int j=0 ; j <= 5 ; j++){
                                    try{
                                        localBluetoothGattCharacteristic = (BluetoothGattCharacteristic) ((ArrayList) mGattCharacteristics.get(i)).get(j);
                                        mBluetoothLeService.setCharacteristicNotification(localBluetoothGattCharacteristic, true);
                                    }catch (Exception e){
                                        Log.d(TAG, e.getMessage());
                                    }
                                }
                            }
                        }
                    }, 1000L);
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    callback.getData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                }
            }
        };
    }


    /**
     * 연결된 디바이스로 데이터 전송
     * 찾아지는 센서 위치가 다르므로 bluetoothLeService를 가져다가 커스텀마이징 할 것!
     * @param data
     */
    public void sendData(String data){
        if(mGattCharacteristics != null){
            try {
                // 블루투스의 이름 변경
                BluetoothGattCharacteristic localBluetoothGattCharacteristic = (BluetoothGattCharacteristic) ((ArrayList) this.mGattCharacteristics.get(0)).get(0);
                //this.mBluetoothLeService.writeCharacteristics(localBluetoothGattCharacteristic, "CHOC2_v1.0.1");

                localBluetoothGattCharacteristic = (BluetoothGattCharacteristic) ((ArrayList) this.mGattCharacteristics.get(3)).get(0);
                this.mBluetoothLeService.writeCharacteristics(localBluetoothGattCharacteristic, data);

            }catch (Exception e){
                Log.d(TAG,"sendData error : " + e.getMessage());
            }
        }
    }


    /**
     * 디바이스 이름 변경     * @param name
     */
    public void setDeviceName(String name){
        if(mGattCharacteristics != null){
            try {
                // 블루투스의 이름 변경
                BluetoothGattCharacteristic localBluetoothGattCharacteristic = (BluetoothGattCharacteristic) ((ArrayList) this.mGattCharacteristics.get(0)).get(0);
                this.mBluetoothLeService.writeCharacteristics(localBluetoothGattCharacteristic, name);
            }catch (Exception e){
                Log.d(TAG,"setDeviceName() error : " + e.getMessage());
            }
        }
    }



    // 연결된 디바이스 정보 저장?
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = context.getResources().getString(R.string.unknown_service);
        String unknownCharaString = context.getResources().getString(R.string.unknown_characteristic);
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
     * 레지스터 등록
     * onResume에서 레지스터 등록해줄 것
     * registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
     * @return
     */
    public static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public interface ReceiverCallback{
        void getData(String data);
    }
}
