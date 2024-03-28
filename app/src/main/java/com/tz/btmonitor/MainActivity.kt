package com.tz.btmonitor

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.tz.btmonitor.bluetooth.BluetoothClientManager
import com.tz.btmonitor.file_writer.FileWriter
import com.tz.btmonitor.model.LatLng
import com.tz.btmonitor.ui.navigation.BluetoothApp
import com.tz.btmonitor.viewmodel.BluetoothViewModel


class MainActivity : ComponentActivity(), ActivityBridge {
    private var viewModel: BluetoothViewModel? = null
    private var btClientManager: BluetoothClientManager? = null
    private var permissionsGranted: Runnable? = null
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var allGood = true

            for (permission in permissions) {
                if (!permission.value) {
                    allGood = false
                    break
                }
            }

            if (allGood) permissionsGranted?.run()
        }

    private var createFileCallback: Callback<FileWriter>? = null
    private val createFileLauncher =
        registerForActivityResult<String, Uri>(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
            if (createFileCallback != null) {
                createFileCallback!!.call(FileWriter(applicationContext, uri))
            } else {
                Toast.makeText(this, "Failed to create file", Toast.LENGTH_SHORT).show()
            }
        }

    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var locationRequest: LocationRequest? = null
    var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            for (location in locationResult.locations) {
                val userLocation = LatLng();
                userLocation.lat = location.latitude
                userLocation.lng = location.longitude
                viewModel?.setLastUserLocation(userLocation)
                Log.d("location", userLocation.toString())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = BluetoothViewModel()
        setContent {
            BluetoothApp(viewModel = viewModel!!)
        }

        viewModel!!.setActivityBridge(this)
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        btClientManager = BluetoothClientManager(bluetoothManager.adapter)
        btClientManager!!.registerReceiver(this)
        viewModel!!.setBluetoothManager(btClientManager)


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create()
        locationRequest?.setInterval(4000);
        locationRequest?.setFastestInterval(2000);
        locationRequest?.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    override fun onStart() {
        super.onStart()
        checkSettingsAndStartLocationUpdates()
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (btClientManager != null) {
            btClientManager!!.unregisterReceiver(this)
        }
    }

    override fun checkPermissions(permissionsGranted: Runnable) {
        val permissionsToRequest: MutableList<String> = ArrayList()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        if (!permissionsToRequest.isEmpty()) {
            this.permissionsGranted = permissionsGranted
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray<String>())
        } else {
            permissionsGranted.run()
        }
    }

    override fun createFile(callback: Callback<FileWriter>) {
        createFileCallback = callback
        createFileLauncher.launch("test.csv")
    }


    private fun checkSettingsAndStartLocationUpdates() {
        val request: LocationSettingsRequest =
            LocationSettingsRequest.Builder().addLocationRequest(locationRequest!!).build()
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask: Task<LocationSettingsResponse> =
            client.checkLocationSettings(request)
        locationSettingsResponseTask.addOnSuccessListener(object :
            OnSuccessListener<LocationSettingsResponse?> {
            override fun onSuccess(locationSettingsResponse: LocationSettingsResponse?) {
                // Settings of device are satisfied and we can start location updates
                startLocationUpdates()
            }
        })
        locationSettingsResponseTask.addOnFailureListener(object : OnFailureListener {
            override fun onFailure(e: Exception) {
                if (e is ResolvableApiException) {
                    val apiException: ResolvableApiException = e as ResolvableApiException
                    try {
                        apiException.startResolutionForResult(this@MainActivity, 1001)
                    } catch (ex: SendIntentException) {
                        throw RuntimeException(ex)
                    }
                }
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationProviderClient?.requestLocationUpdates(
            locationRequest!!,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
    }
}