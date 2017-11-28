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
import android.widget.EditText;
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

        initButton();

    }

    private void init(){

        deviceAddress = getIntent().getStringExtra(Data.EXTRAS_DEVICE_ADDRESS);
        Log.d(TAG, "address : " + deviceAddress);

        deviceControl = new DeviceControl(deviceAddress);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, deviceControl.mServiceConnection, BIND_AUTO_CREATE);
    }

    private void initButton(){
        Button nameBtn = (Button) findViewById(R.id.nameButton);
        Button sensor1 = (Button) findViewById(R.id.sensor1);
        Button sensor2 = (Button) findViewById(R.id.sensor2);
        Button sensor3 = (Button) findViewById(R.id.sensor3);
        Button sensor4 = (Button) findViewById(R.id.sensor4);

        nameBtn.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   EditText editText = (EditText) findViewById(R.id.device_name);
                   String deviceName = editText.getText().toString();
                   if(deviceName.equals("")){
                       Toast.makeText(DeviceControlActivity.this, "이름을 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
                       return;
                   }
                   deviceControl.setDeviceName(editText.getText().toString());
               }
        });

        sensor1.setOnClickListener(new ButtonClickListener());
        sensor2.setOnClickListener(new ButtonClickListener());
        sensor3.setOnClickListener(new ButtonClickListener());
        sensor4.setOnClickListener(new ButtonClickListener());

    }

    private class ButtonClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            try {
                String sensor = ((Button) view).getText().toString();
                deviceControl.sendData(sensor);
            }catch (Exception e){
                Log.d(TAG, "sendData error : " + e.getMessage());
            }
        }
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
