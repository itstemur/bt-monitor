package com.tz.btmonitor.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.tz.btmonitor.model.Device


class BluetoothManager(
    private val context: Context,
    private val permissionBridge: (callback: () -> Unit) -> Unit
) {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    // Callback interface for Bluetooth events
    interface BluetoothManagerListener {
        fun onDeviceDiscovered(device: Device)
        fun onDiscoveryStarted()
        fun onDiscoveryFinished()
        fun onBluetoothEnabled()
        fun onBluetoothDisabled()
    }

    private val listeners = mutableListOf<BluetoothManagerListener>()

    fun addListener(listener: BluetoothManagerListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: BluetoothManagerListener) {
        listeners.remove(listener)
    }

    @SuppressLint("MissingPermission")
    fun startDeviceDiscovery(discoveryCallback: (Device) -> Unit) {
        notifyDiscoveryStarted()
        bluetoothAdapter?.startDiscovery()
    }

    @SuppressLint("MissingPermission")
    fun stopDeviceDiscovery() {
        bluetoothAdapter?.cancelDiscovery()
        notifyDiscoveryFinished()
    }

    @SuppressLint("MissingPermission")
    fun enableBluetooth() {
        bluetoothAdapter?.takeIf { !it.isEnabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            context.startActivity(enableBtIntent)
        }
    }

    /**
     * handled in activity
     */
    fun checkPermissions(permissionsGranted: () -> Unit) {
        permissionBridge(permissionsGranted)
    }

    //    @SuppressLint("MissingPermission")
//    fun disableBluetooth() {
//        bluetoothAdapter?.takeIf { it.isEnabled }?.apply {
//            cancelDiscovery()
//            disable()
//        }
//    }

    private val deviceDiscoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice? =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(
                            BluetoothDevice.EXTRA_DEVICE,
                            BluetoothDevice::class.java
                        )
                    } else {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                device?.let {
                    notifyDeviceDiscovered(it.toDevice())
                }
            }
        }
    }

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR
                )
                when (state) {
                    BluetoothAdapter.STATE_OFF -> notifyBluetoothDisabled()
//                    BluetoothAdapter.STATE_TURNING_OFF -> setButtonText("Turning Bluetooth off...")
                    BluetoothAdapter.STATE_ON -> notifyBluetoothEnabled()
//                    BluetoothAdapter.STATE_TURNING_ON -> setButtonText("Turning Bluetooth on...")
                }
            }
        }
    }

    fun registerReceiver() {
        // register discovery receiver
        val f1 = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(deviceDiscoveryReceiver, f1)

        // register state receiver
        val f2 = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(bluetoothStateReceiver, f2)
    }

    fun unregisterReceiver() {
        context.unregisterReceiver(deviceDiscoveryReceiver)
        context.unregisterReceiver(bluetoothStateReceiver)
    }

    private fun notifyDeviceDiscovered(device: Device) {
        listeners.forEach { it.onDeviceDiscovered(device) }
    }

    private fun notifyDiscoveryStarted() {
        listeners.forEach { it.onDiscoveryStarted() }
    }

    private fun notifyDiscoveryFinished() {
        listeners.forEach { it.onDiscoveryFinished() }
    }

    private fun notifyBluetoothEnabled() {
        listeners.forEach { it.onBluetoothEnabled() }
    }

    private fun notifyBluetoothDisabled() {
        listeners.forEach { it.onBluetoothDisabled() }
    }
}
