package com.nainfox.bluetoothmodule;

import android.Manifest;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.nainfox.bluetoothmodule.bluetooth.*;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "###MainActivity";

    private BluetoothController bluetoothController;

    String deviceName, deviceAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initBluetooth();

        checkPermission();
    }

    /**
     * ACCESS_COARSE_LOCATION / ACCESS_FINE_LOCATION
     */
    private void checkPermission(){
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setRationaleMessage("블루투스 통신을 위해 권한을 설정해주세요.")
                .setDeniedMessage("거부하셨습니다.\n[설정] > [권한] 에서 권한을 허용할 수 있어요.")
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                .check();
    }

    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
//            Toast.makeText(getApplicationContext(), "권한 허가", Toast.LENGTH_SHORT).show();
            bluetoothController.startScan();
        }
        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(getApplicationContext(), "권한 거부로 인하여 블루투스를 사용할 수 없습니다.\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    // 스캔
    private void initBluetooth(){
        Config config = new Config();
        config.setTitleBarColor("#99DA8F4A");

        bluetoothController = new BluetoothController(this, config);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Data.REQUEST_ENABLE_BT) {
            try {
                deviceName = data.getStringExtra(Data.EXTRAS_DEVICE_NAME);
                deviceAddress = data.getStringExtra(Data.EXTRAS_DEVICE_ADDRESS);

                Log.d(TAG, "name : " + deviceName + ", address : " + deviceAddress);
                if (!deviceAddress.equals("null")) {
                    Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
                    bindService(gattServiceIntent, bluetoothController.getBluetoothLeServiceConnection(deviceAddress), BIND_AUTO_CREATE);
                }
            }catch (Exception e){
                Log.e(TAG, "error : " + e.getMessage());
            }
        }
    }


    /**
     * 리시버 ( 연결 성공시 , 연결 해제 , 장치 발견 시 , 데이터 수신 시 )
     */
    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                bluetoothController.setConnected(true);
                Log.d(TAG, "connected!");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                bluetoothController.setConnected(false);
                Log.d(TAG, "disconnected!");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // 장치 연결이 됐을 경우
                // 해당 UUID의 설정값을 true로 변경해준다.
                Log.d("###", "ACTION_GATT_SERVICES_DISCOVERED!!!!");
                bluetoothController.displayGattServices(bluetoothController.getBluetoothLeService().getSupportedGattServices());
                new Handler().postDelayed(new Runnable()
                {
                    public void run()
                    {

                        BluetoothGattCharacteristic localBluetoothGattCharacteristic;
                        for(int i=0 ; i<=4 ; i++){
                            for(int j=0; j<4 ; j++){
                                try {
                                    localBluetoothGattCharacteristic = (BluetoothGattCharacteristic) ((ArrayList) bluetoothController.getmGattCharacteristics().get(i)).get(j);
                                    bluetoothController.getBluetoothLeService().setCharacteristicNotification(localBluetoothGattCharacteristic, true);
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

    @Override
    protected void onResume() {
        super.onResume();

        if(bluetoothController != null) {
            registerReceiver(broadcastReceiver, bluetoothController.makeGattUpdateIntentFilter());
            if (bluetoothController.getBluetoothLeService() != null) {
                final boolean result = bluetoothController.getBluetoothLeService().connect(deviceAddress);
                Log.d(TAG, "Connect request result=" + result);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(bluetoothController.getServiceConnection());
        bluetoothController.initBluetoothLeService();
    }
}
