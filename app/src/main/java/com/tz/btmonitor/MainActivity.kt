package com.tz.btmonitor


import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.tz.btmonitor.bluetooth.BluetoothManager
import com.tz.btmonitor.ui.navigation.BluetoothApp
import com.tz.btmonitor.viewmodel.BluetoothViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: BluetoothViewModel by viewModels()
    private val bluetoothManager: BluetoothManager = BluetoothManager(this, this::checkPermissions)

    var permissionsGranted: (()->Unit)? = null
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var allGood = true

            for (permission in permissions) {
                if (!permission.value) {
                    allGood = false
                    break
                }
            }

            if(allGood) permissionsGranted?.invoke()
        }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BluetoothApp()
        }

        viewModel.setBluetoothManager(bluetoothManager)
        viewModel.start()
    }

    override fun onStart() {
        super.onStart()
        viewModel.start()
    }

    override fun onResume() {
        super.onResume()
        bluetoothManager.registerReceiver()
    }

    override fun onPause() {
        bluetoothManager.unregisterReceiver()
        super.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    fun checkPermissions(permissionsGranted: () -> Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(android.Manifest.permission.BLUETOOTH_CONNECT)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(android.Manifest.permission.BLUETOOTH_SCAN)
        }

        if(permissionsToRequest.isNotEmpty()){
            this.permissionsGranted = permissionsGranted
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            permissionsGranted.invoke()
        }
    }
}