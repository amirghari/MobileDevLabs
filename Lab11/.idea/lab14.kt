package com.example.lab14

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lab14.ui.theme.Lab14Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyViewModel : ViewModel() {

    // LiveData for scanning and connecting
    val scanResults = MutableLiveData<List<ScanResult>>(listOf())
    val fScanning = MutableLiveData<Boolean>(false)
    val mConnectionState = MutableLiveData<Int>(-1)

    // Scanning results map
    private val mResults = HashMap<String, ScanResult>()
    private var mBluetoothGatt: BluetoothGatt? = null

    // Scan devices with BLE scanner
    fun scanDevices(scanner: BluetoothLeScanner) {
        viewModelScope.launch(Dispatchers.IO) {
            fScanning.postValue(true)
            mResults.clear()
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()
            scanner.startScan(null, settings, leScanCallback)
            delay(3000)  // Scan period
            scanner.stopScan(leScanCallback)
            scanResults.postValue(mResults.values.toList())
            fScanning.postValue(false)
        }
    }

    // Connect to the selected device
    fun connectDevice(context: Context, device: BluetoothDevice) {
        Log.d("DBG", "Connecting to ${device.name} (${device.address})")
        mBluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    // BLE ScanCallback
    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            mResults[result.device.address] = result
            Log.d("DBG", "Device found: ${result.device.name} (${result.device.address})")
        }
    }

    // BLE GATT callback for connection handling
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("DBG", "Connected to GATT server")
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("DBG", "Disconnected from GATT server")
                mBluetoothGatt?.disconnect()
            }
        }
    }
}

@Composable
fun ShowDevices(mBluetoothAdapter: BluetoothAdapter, model: MyViewModel = viewModel()) {
    val scanResults by model.scanResults.observeAsState(listOf())
    val fScanning by model.fScanning.observeAsState(false)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { model.scanDevices(mBluetoothAdapter.bluetoothLeScanner) },
            enabled = !fScanning,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(if (fScanning) "Scanning..." else "Scan Devices")
        }

        if (scanResults.isEmpty()) {
            Text("No devices found")
        } else {
            scanResults.forEach { result ->
                val deviceName = result.device.name ?: "Unknown Device"
                Text("$deviceName (${result.device.address})")
            }
        }
    }
}

@Composable
fun App(mBluetoothAdapter: BluetoothAdapter) {
    val viewModel: MyViewModel = viewModel()
    ShowDevices(mBluetoothAdapter, viewModel)
}

class MainActivity : ComponentActivity() {
    private var mBluetoothAdapter: BluetoothAdapter? = null

    // Check permissions
    private fun hasPermissions(): Boolean {
        if (mBluetoothAdapter == null || !mBluetoothAdapter!!.isEnabled) {
            Log.e("DBG", "No Bluetooth LE capability or Bluetooth is disabled")
            return false
        }
        else if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADMIN
                ), 1
            )
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get Bluetooth adapter
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter

        // Set content with Compose
        setContent {
            Lab14Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        Log.i("DBG", "Bluetooth support: ${hasPermissions()}")
                        when {
                            mBluetoothAdapter == null -> Text("Bluetooth not supported on this device.")
                            !mBluetoothAdapter!!.isEnabled -> Text("Please enable Bluetooth.")
                            else -> App(mBluetoothAdapter!!)
                        }
                    }
                }
            }
        }
    }
}
