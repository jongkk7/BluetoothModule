package com.nainfox.bluetoothmodule;

import android.Manifest;
import android.content.Intent;
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

    String deviceName, deviceAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();
    }

    private void bluetoothScan(){
        Intent i = new Intent(this, DeviceScanActivity.class);
        startActivityForResult(i, Data.REQUEST_ENABLE_BT);
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
            bluetoothScan();
        }
        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(getApplicationContext(), "권한 거부로 인하여 블루투스를 사용할 수 없습니다.\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Data.REQUEST_ENABLE_BT){
            deviceName =  data.getStringExtra(Data.EXTRAS_DEVICE_NAME);
            deviceAddress = data.getStringExtra(Data.EXTRAS_DEVICE_ADDRESS);

            Log.d(TAG, "name : " + deviceName + ", address : " + deviceAddress);
            if(!deviceAddress.equals("null")){
                Intent i = new Intent(getApplicationContext(), DeviceControlActivity.class);
                i.putExtra(Data.EXTRAS_DEVICE_NAME, deviceName);
                i.putExtra(Data.EXTRAS_DEVICE_ADDRESS, deviceAddress);
                startActivity(i);
            }
        }
    }
}
