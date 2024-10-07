package com.example.lab12

import android.Manifest
package com.example.lab11

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lab11.ui.theme.Lab11Theme

class MainActivity : ComponentActivity() {
    private var mBluetoothAdapter: BluetoothAdapter? = null

    private fun hasPermissions(): Boolean {
        if (mBluetoothAdapter == null || !mBluetoothAdapter!!.isEnabled) {
            Log.d("DBG", "No Bluetooth LE capability")
            return false
        } else if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("DBG", "No fine location access")
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return false
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter

        setContent {
            Lab11Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        when {
                            mBluetoothAdapter == null -> {
                                Text("Bluetooth is not supported on this device")
                            }
                            !mBluetoothAdapter!!.isEnabled -> {
                                Text("Bluetooth is turned off.")
                            }
                            else -> {
                                Log.i("Bluetooth", "Bluetooth is enabled")
                                ShowDevices(mBluetoothAdapter!!, viewModel())
                            }
                        }
                    }
                }
            }
        }
    }
}


class MyViewModel : ViewModel() {
    val scanResults = MutableLiveData<List<ScanResult>>(null)
    val fScanning = MutableLiveData<Boolean>(false)
    private val mResults = HashMap<String, ScanResult>()

    fun scanDevices(scanner: BluetoothLeScanner) {
        viewModelScope.launch(Dispatchers.IO) {
            fScanning.postValue(true)
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(0)
                .build()
            scanner.startScan(null, settings, leScanCallback)
            delay(SCAN_PERIOD)
            scanner.stopScan(leScanCallback)
            scanResults.postValue(mResults.values.toList())
            fScanning.postValue(false)
        }
    }

    companion object {
        const val SCAN_PERIOD: Long = 3000
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device = result.device
            val deviceAddress = device.address
            mResults[deviceAddress] = result
            Log.d("DBG", "Device found: $deviceAddress (${result.isConnectable})")
        }
    }
}
@Composable
fun ShowDevices(mBluetoothAdapter: BluetoothAdapter, model: MyViewModel = viewModel()) {
    val context = LocalContext.current
    val value: List<ScanResult>? by model.scanResults.observeAsState(null)
    val fScanning: Boolean by model.fScanning.observeAsState(false)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Button for starting the scan
        Button(
            onClick = { model.scanDevices(mBluetoothAdapter.bluetoothLeScanner) },
            modifier = Modifier.padding(8.dp)
        ) {
            Text(if (fScanning) "Scanning..." else "Start Scan")
        }

        Spacer(modifier = Modifier.padding(8.dp))

        // Display scanned devices or no devices found message
        if (value.isNullOrEmpty()) {
            Text(text = "No devices found", modifier = Modifier.padding(8.dp))
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                value!!.forEach { result ->
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
}
