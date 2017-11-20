## Bluetooth 모듈

### gradle 추가
1. app gradle
``` java
  allprojects {
		repositories {
			maven { url 'https://jitpack.io' }
		}
	}
```
2. 프로젝트 gradle
``` java
  dependencies {
		compile 'com.github.jjongkwon2:BluetoothLibrary:1.0.1'
	}
```


### 사용 전 권한 허가
1. Manifests
``` java
  <uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>
  <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
  <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>

  <uses-permission android:name="android.permission.BLUETOOTH"/>
  <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
```
2. java
``` java
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
            // 권한 허가 -> 블루투스 통신
            bluetoothScan();
        }
        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(getApplicationContext(), "권한 거부로 인하여 블루투스를 사용할 수 없습니다.\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
        }
    };          
```


### 장치 검색
MainActivity.java
``` java
  private void bluetoothScan(){
        Intent i = new Intent(this, DeviceScanActivity.class);
        // REQUEST_ENABLE_BT : bluetoothlib에서 제공하는 Data 클래스의 정수값
        startActivityForResult(i, Data.REQUEST_ENABLE_BT);
  }

  @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Data.REQUEST_ENABLE_BT){
            // 장치 리스트에서 클릭한 장치이름과 주소를 넘겨준다. ( 주소값 중요 )
            deviceName =  data.getStringExtra(Data.EXTRAS_DEVICE_NAME);
            deviceAddress = data.getStringExtra(Data.EXTRAS_DEVICE_ADDRESS);

            Log.d(TAG, "name : " + deviceName + ", address : " + deviceAddress);
            if(!deviceAddress.equals("null")){
                // 장치와 1:1 통신을 하는 Activity로 넘어간다.
                // 주소값으로 연결을 시도한 뒤 성공하면 데이터를 셋팅하고 정보를 주고 받는다.
                Intent intent = new Intent(MainActivity.this, DeviceControlActivity.class);
                intent.putExtra(Data.ADDRESS, deviceAddress);
                intent.putExtra(Data.NAME, deviceName);

                startActivity(intent);
            }
        }
    }
```


### 통신
1. DeviceControl클래스
``` java
  deviceControl = new DeviceControl(getApplicationContext(), deviceName, deviceAddress);
    deviceControl.initReceiver(new DeviceControl.ReceiverCallback() {
        @Override
        public void getData(String data) {
            switch (data){
                case Data.CONNECT:
                    // 연결 성공

                    break;
                case Data.DISCONNECT:
                    // 연결 실패

                    break;
                default:
                    // 데이터 수신

            }
        }
  });
```

2. 리시버 등록 & 해제
``` java
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(deviceControl.mGattUpdateReceiver, deviceControl.makeGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(deviceControl.mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(deviceControl.mGattUpdateReceiver);
    }
```

3. 메세지 전달
``` java
  deviceControl.sendData("sensor1");
```


4. 그 외
> 1) 장치 이름 변경
``` java
  deviceControl.setDeviceName("device01");
```

> 2) 리시버 변경 ( 유효한 센서 채널을 찾아서 true 시켜줄 것 ! )
``` java
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
                            // 유효한 센서 채널을 찾아서 그 부분의 값만 true로 변경 해 줄 것~!
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
                    // 데이터를 받은 경우
                    callback.getData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                }
            }
        };
    }
```
