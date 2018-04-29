package com.nainfox.bluetoothmodule.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nainfox.bluetoothmodule.R;

public class DeviceScanActivity extends AppCompatActivity {
    private final static String TAG = "###DeviceScanActivity";
    private final static int REQUEST_ENABLE_BT = 1;
    private final long SCAN_PERIOD = 40000L;

    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private ListView deviceListView;

    private RelativeLayout titleLayout;
    private TextView titleTextView;
    private Button bottomCancelButton;


    private Config config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_scan_device);

        initLayout();
        init();
    }

    private void initLayout(){
        config = (Config)getIntent().getSerializableExtra(Data.CONFIG);

        titleLayout = (RelativeLayout) findViewById(R.id.titleBar);
        if(config.isTitleBarBgGradient()){
            titleLayout.setBackgroundResource(config.getTitleBarBackroundRes());
        }else {
            titleLayout.setBackgroundColor(Color.parseColor(config.getTitleBarBackgroundColor()));
        }

        titleTextView = (TextView) findViewById(R.id.title_textview);
        titleTextView.setTextColor(Color.parseColor(config.getTitleTextColor()));
        titleTextView.setTextSize(config.getTitleTextSize());
        titleTextView.setText(config.getTitleText());

        bottomCancelButton = (Button) findViewById(R.id.bottom_cancel_button);
        bottomCancelButton.setBackgroundColor(Color.parseColor(config.getBottomButtonBackgroundColor()));
        bottomCancelButton.setText(config.getBottomButtonText());
        bottomCancelButton.setTextColor(Color.parseColor(config.getBottomButtonTextColor()));
        bottomCancelButton.setTextSize(config.getBottomButtonTextSize());

    }


    private void init(){
        mHandler = new Handler();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if(mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON){
            Log.d("####", "on");
            mBluetoothAdapter.setName("jjongkwon2");
        }


        // 검색된 장치 리스트 셋팅
        deviceListView = (ListView) findViewById(R.id.deviceListView);
        deviceListView.setDivider(null);
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
                if (device == null) return;

                final Intent intent = new Intent();
                intent.putExtra(Data.EXTRAS_DEVICE_NAME, device.getName());
                intent.putExtra(Data.EXTRAS_DEVICE_ADDRESS, device.getAddress());

                Log.d(TAG, "name : " + device.getName() + " / address : " + device.getAddress());
                setResult(Data.REQUEST_ENABLE_BT, intent);


                if (mScanning) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanning = false;
                }
                finish();
            }
        });


        // 취소 버튼 셋팅
        bottomCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanLeDevice(false);
                finish();
            }
        });
    }

    /**
     * 디바이스 장치 검색
     * @param enable
     */
    private void scanLeDevice(boolean enable){
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };


    @Override
    protected void onResume() {
        super.onResume();
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter(getApplicationContext(), config);
        deviceListView.setAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }

    @Override
    public void onBackPressed() {
        scanLeDevice(false);
        super.onBackPressed();
    }
}
