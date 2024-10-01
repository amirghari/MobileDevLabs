package com.example.bluetoothscanner

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluetoothscanner.ui.theme.BluetoothScannerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class MyBluetoothViewModel : ViewModel() {
    val scanResults = MutableLiveData<List<ScanResult>>(null)
    val isScanning = MutableLiveData<Boolean>(false)
    private val resultsMap = HashMap<String, ScanResult>()

    fun scanDevices(scanner: BluetoothLeScanner) {
        viewModelScope.launch(Dispatchers.IO) {
            isScanning.postValue(true)
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()
            scanner.startScan(null, settings, leScanCallback)
            delay(SCAN_DURATION)
            scanner.stopScan(leScanCallback)
            scanResults.postValue(resultsMap.values.toList())
            isScanning.postValue(false)
        }
    }

    companion object {
        const val SCAN_DURATION: Long = 5000 // Scanning for 5 seconds
        val UUID_SERVICE = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb") // Heart rate service
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val deviceAddress = result.device.address
            resultsMap[deviceAddress] = result
            Log.d("BluetoothScan", "Found device: $deviceAddress (Connectable: ${result.isConnectable})")
        }
    }
}

@Composable
fun DeviceScanScreen(bluetoothAdapter: BluetoothAdapter, model: MyBluetoothViewModel = viewModel()) {
    val context = LocalContext.current
    val scanResults by model.scanResults.observeAsState(null)
    val isScanning by model.isScanning.observeAsState(false)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { model.scanDevices(bluetoothAdapter.bluetoothLeScanner) },
            modifier = Modifier.padding(8.dp)
        ) {
            Text(if (isScanning) "Scanning for Devices..." else "Start Bluetooth Scan")
        }

        Spacer(modifier = Modifier.padding(8.dp))

        if (scanResults.isNullOrEmpty()) {
            Text(text = "No devices found", modifier = Modifier.padding(8.dp))
        } else {
            scanResults!!.forEach { result ->
                val deviceName = result.device.name ?: "Unknown Device"
                val deviceAddress = result.device.address
                Text(
                    text = "$deviceName ($deviceAddress)",
                    modifier = Modifier.padding(8.dp),
                    color = if (result.isConnectable) Color.Black else Color.Gray
                )
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    private var bluetoothAdapter: BluetoothAdapter? = null

    private fun hasPermissions(): Boolean {
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            Log.d("BluetoothScan", "Bluetooth LE not supported")
            return false
        } else if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return false
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        setContent {
            BluetoothScannerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        when {
                            bluetoothAdapter == null -> {
                                Text("Bluetooth is not supported on this device")
                            }
                            !bluetoothAdapter!!.isEnabled -> {
                                Text("Bluetooth is turned off.")
                            }
                            else -> {
                                Log.i("Bluetooth", "Bluetooth is enabled")
                                DeviceScanScreen(bluetoothAdapter!!)
                            }
                        }
                    }
                }
            }
        }
    }
}
