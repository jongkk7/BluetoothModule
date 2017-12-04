package com.nainfox.bluetoothlib.bluetooth;

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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nainfox.bluetoothlib.R;


public class DeviceScanActivity extends AppCompatActivity {
    private final static String TAG = "###DeviceScanActivity";
    private final static int REQUEST_ENABLE_BT = 1;
    private final long SCAN_PERIOD = 40000L;

    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private ListView deviceListView;
    private Button cancelButton;
    private ImageButton cancelTopButton;
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

        RelativeLayout titleBar = (RelativeLayout) findViewById(R.id.titleBar);
        titleBar.setBackgroundColor(Color.parseColor(config.getTitleBarColor()));

        TextView title_textview = (TextView) findViewById(R.id.title_textview);
        title_textview.setTextColor(Color.parseColor(config.getTitleTextColor()));
        title_textview.setTextSize(config.getTitleTextSize());


        // 취소 버튼 셋팅
        cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setBackgroundColor(Color.parseColor(config.getCancelButtonBackground()));
        cancelButton.setTextColor(Color.parseColor(config.getCancelButtonTextColor()));
        cancelButton.setTextSize(config.getCancleButtonTextSize());
        cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanLeDevice(false);
                finish();
            }
        });

        cancelTopButton = (ImageButton) findViewById(R.id.cancel_top_button);
        cancelTopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanLeDevice(false);
                finish();
            }
        });
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

        // 검색된 장치 리스트 셋팅
        deviceListView = (ListView) findViewById(R.id.deviceListView);
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
