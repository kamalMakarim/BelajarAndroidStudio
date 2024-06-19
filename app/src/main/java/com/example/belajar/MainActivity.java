package com.example.belajar;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;
    private static final int REQUEST_BLUETOOTH_CONNECT_PERMISSION = 3;
    private static final int REQUEST_BLUETOOTH_SCAN_PERMISSION = 4;

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> arrayAdapter;
    public ArrayList<String> deviceList;
    public ArrayList<BluetoothDevice> devices;
    public ArrayList<Integer> rssiList;
    private TextView floorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = findViewById(R.id.list_view);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceList = new ArrayList<>();
        devices = new ArrayList<>();
        rssiList = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
        listView.setAdapter(arrayAdapter);
        floorTextView = findViewById(R.id.floor);


        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available on this device", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Start scanning when the activity is created
        checkBluetoothPermissionsAndEnable();
    }

    private void checkBluetoothPermissionsAndEnable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    REQUEST_BLUETOOTH_CONNECT_PERMISSION);
        } else {
            enableBluetooth();
        }
    }

    private void enableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        REQUEST_BLUETOOTH_CONNECT_PERMISSION);
                return;
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            checkLocationPermissionAndScan();
        }
    }

    private void checkLocationPermissionAndScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            checkBluetoothScanPermissionAndScan();
        }
    }

    private void checkBluetoothScanPermissionAndScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN},
                    REQUEST_BLUETOOTH_SCAN_PERMISSION);
        } else {
            startBluetoothScan();
        }
    }

    private void startBluetoothScan() {
        deviceList.clear();
        devices.clear();
        rssiList.clear();
        arrayAdapter.notifyDataSetChanged();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN},
                    REQUEST_BLUETOOTH_SCAN_PERMISSION);
            return;
        }
        bluetoothAdapter.startDiscovery();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                String deviceName = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) ==
                            PackageManager.PERMISSION_GRANTED) {

                        deviceName = device.getName();
                        System.out.println(device.getName());
                    }
                } else {
                    deviceName = device.getName();
                }
                if (deviceName == null) {
                    deviceName = device.getAddress();  // Use address if name is null
                }
                String deviceInfo = "Name: " + deviceName + ", RSSI: " + rssi + "dBm";
                deviceList.add(deviceInfo);
                devices.add(device);
                rssiList.add(rssi);
                arrayAdapter.notifyDataSetChanged();
            }
            floorTextView.setText("Predicted floor: " + floorPicker());
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN},
                    REQUEST_BLUETOOTH_SCAN_PERMISSION);
            return;
        }
        bluetoothAdapter.cancelDiscovery();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkBluetoothPermissionsAndEnable();
            } else {
                Toast.makeText(this, "Location permission is required for Bluetooth scanning", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_BLUETOOTH_CONNECT_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableBluetooth();
            } else {
                Toast.makeText(this, "Bluetooth Connect permission is required to enable Bluetooth", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_BLUETOOTH_SCAN_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBluetoothScan();
            } else {
                Toast.makeText(this, "Bluetooth Scan permission is required to scan for Bluetooth devices", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bluetoothAdapter.isEnabled()) {
            checkBluetoothPermissionsAndEnable();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN},
                    REQUEST_BLUETOOTH_SCAN_PERMISSION);
            return;
        }
        bluetoothAdapter.cancelDiscovery();
    }

    private Integer floorPicker() {
        int floorPrediction = 0;
        int rssiFloor1 = 0;
        int rssiFloor2 = 0;
        Context context = this;
        ArrayList<String> floor1 = new ArrayList<>();
        ArrayList<String> floor2 = new ArrayList<>();
        floor1.add("1A");
        floor1.add("1B");
        floor1.add("1C");
        floor2.add("2A");
        floor2.add("2B");
        floor2.add("2C");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                for (BluetoothDevice device : devices) {
                    if (device.getName() != null) {
                        if (floor1.contains(device.getName())) {
                            rssiFloor1 += rssiList.get(devices.indexOf(device));
                        } else if (floor2.contains(device.getName())) {
                            rssiFloor2 += rssiList.get(devices.indexOf(device));
                        }
                    }
                }
            }
        }
        if (rssiFloor1 < rssiFloor2) {
            floorPrediction = 1;
        } else {
            floorPrediction = 2;
        }
        return floorPrediction;
    }
}
