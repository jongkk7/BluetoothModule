# Bluetooth 모듈

## gradle 추가
+ app gradle

``` java
  allprojects {
		repositories {
			maven { url 'https://jitpack.io' }
		}
	}
```

+ 프로젝트 gradle

``` java
  dependencies {
		compile 'com.github.jjongkwon2:BluetoothLibrary:1.0.1'
	}
```


## 권한 허가
+ Manifests

``` xml
  <uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>
  <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
  <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>

  <uses-permission android:name="android.permission.BLUETOOTH"/>
  <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>

  <!-- 블루투스 통신 서비스 추가 -->
  <service android:name=".bluetooth.BluetoothLeService" android:enabled="true"/>
```

+ java

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


## 블루투스 장치 검색 ( MainActivity.java )
+ 내장되어있는 DeviceScanActivity를 사용

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


## 블루투스 통신 ( DeviceControlActivity 클래스 )

+ 서비스 등록, 통신 준비

``` java
  private DeviceControl deviceControl;

  private void init(){
        // 선택된 장치의 주소값
        String deviceAddress = getIntent().getStringExtra(Data.EXTRAS_DEVICE_ADDRESS);

        // 통신을 위한 클래스 생성
        deviceControl = new DeviceControl(deviceAddress);

        // BLE 서비스 등록
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, deviceControl.mServiceConnection, BIND_AUTO_CREATE);
    }
```

+ 브로드캐스트 리시버 등록 & 해제

``` java
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
```

## 메세지 전달

``` java
  deviceControl.sendData("sensor1");
```


## 그 외

1) 장치 이름 변경

``` java
  deviceControl.setDeviceName("device01");
```

2) 브로드캐스트 리시버 변경 ( 유효한 센서 채널을 찾아서 true 시켜줄 것 ! )

``` java
public final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
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
                                      Log.d(TAG,"index error : " + e.getMessage());
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
```

### Full Source

1.MainActivity
``` java
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
        if(resultCode == RESULT_OK) {
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
}
```

2.DeviceControlActivity
``` java
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
```
