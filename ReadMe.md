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
		compile 'com.github.jjongkwon2:BluetoothLibrary:{last release version}'
    //compile 'com.github.jjongkwon2:BluetoothLibrary:{1.1.1}'
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


## 블루투스 컨트롤러 ( BluetoothController )
1. 컨트롤러 생성
``` java
  BluetoothController BluetoothController = new BluetoothController(this, config);
```

2. 블루투스 통신(서비스)으로 부터 데이터를 받을 BroadcastReceiver를 생성, 등록한다. <br>
그 외에 종료 시 서비스를 언바인딩, 리시버를 해제하는 작업을 추가한다.
``` java
  // Receiver 생성
  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
          final String action = intent.getAction();
          if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
              // 연결 성공
              bluetoothController.setConnected(true);
          } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
              // 연결 해제
              bluetoothController.setConnected(false);
          } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
              // 장치 발견 ( 통신이 가능하도록 Notification 값을 true로 바꿔준다. )
              bluetoothController.displayGattServices(bluetoothController.getBluetoothLeService().getSupportedGattServices());
              new Handler().postDelayed(new Runnable()
              {
                  public void run()
                  {
                      BluetoothGattCharacteristic localBluetoothGattCharacteristic;
                      // 장치 센서들의 정확한 위치를 알 수 없어서 일단은 for문으로 해결
                      for(int i=0 ; i<=4 ; i++){
                          for(int j=0; j<4 ; j++){
                              try {
                                  localBluetoothGattCharacteristic = (BluetoothGattCharacteristic) ((ArrayList) bluetoothController.getmGattCharacteristics().get(i)).get(j);
                                  bluetoothController.getBluetoothLeService().setCharacteristicNotification(localBluetoothGattCharacteristic, true);
                              }catch (Exception e){
                                  // Log.d(TAG,"index error : " + e.getMessage());
                              }
                          }
                      }

                  }
              }, 1000L);
          } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
              // 장치로부터 데이터 수신
              Log.d("### getStringExtra", intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
          }
      }
  };

  // Receiver 등록
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
```

3. 장치 검색 -> 검색된 장치 클릭 -> 주소값 저장 -> 주소값으로 연결 시도
``` java
  public void startScan(){
      // 장치 검색
      bluetoothController.startScan();
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
                  // 연결 시도
                  Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
                  bindService(gattServiceIntent, bluetoothController.getBluetoothLeServiceConnection(deviceAddress), BIND_AUTO_CREATE);
              }
          }catch (Exception e){
              Log.e(TAG, "error : " + e.getMessage());
          }
      }
  }
```
> 주의할 점 ! <br>
> 서비스가 날려줄 데이터를 받는 브로드 캐스트 리시버를 등록을 해놓아야 제대로 된 값들을 받을 수 있다.
> onResume()이나 onCreate()에서 미리 생성 후 등록해 주도록 한다.

3. 그 외 기능들
  + 연결상태 확인
  ``` java
    controller.getConnected();  // 연결상태 반환 (true / false)
  ```
  + ScanActivity 스타일 변경
  ``` java
    Config config = new Config();
    config.setTitleBarColor("#FF35F3DE");
    config.setTitleTextColor("#FF000000");
    config.setTitleTextSize(25);

    BluetoothController controller = new BluetoothController(this, config);
    controller.startScan();
  ```
  + Config
  ``` java
  // title
    private String titleBarColor = "#FFFFFFFF";
    private String titleTextColor = "#FF000000";
    private int titleTextSize = 24;

    // 리스트 아이템
    private String deviceItemTextColor = "#FF000000";
    private int deviceItemTextSize = 30;

    private String cancelButtonBackground = "#FFFFFFFF";
    private String cancelButtonTextColor = "#FF000000";
    private int cancleButtonTextSize = 30;
    private int cancelButtonImage = R.drawable.cancel_btn;
  ```
