package com.nainfox.bluetoothmodule;

import android.Manifest;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.nainfox.bluetoothmodule.bluetooth.BluetoothLeService;
import com.nainfox.bluetoothmodule.bluetooth.Data;
import com.nainfox.bluetoothmodule.bluetooth.DeviceControl;
import com.nainfox.bluetoothmodule.bluetooth.DeviceScanActivity;
import com.nainfox.bluetoothmodule.bluetooth.SampleGattAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeviceControlActivity extends AppCompatActivity {
    private final String TAG = "### DeviceControl";

    String deviceName, deviceAddress;

    private DeviceControl deviceControl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);

        init();

        Button sensor1 = (Button) findViewById(R.id.sensor1);
        sensor1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    deviceControl.sendData("sensor1");
                }catch (Exception e){
                    Log.d(TAG,"sendData error : " + e.getMessage());
                }
            }
        });
    }

    private void init(){

        deviceAddress = getIntent().getStringExtra(Data.EXTRAS_DEVICE_ADDRESS);
        Log.d(TAG, "address : " + deviceAddress);

        deviceControl = new DeviceControl(deviceAddress);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, deviceControl.mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(deviceControl.mGattUpdateReceiver, deviceControl.makeGattUpdateIntentFilter());
        if (deviceControl.mBluetoothLeService != null) {
            final boolean result = deviceControl.mBluetoothLeService.connect(deviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(deviceControl.mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(deviceControl.mServiceConnection);
        deviceControl.mBluetoothLeService = null;
    }


}
