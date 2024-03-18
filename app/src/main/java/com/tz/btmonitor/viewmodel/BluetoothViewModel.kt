package com.tz.btmonitor.viewmodel

import androidx.lifecycle.ViewModel
import com.tz.btmonitor.bluetooth.BluetoothManager
import com.tz.btmonitor.model.Device
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BluetoothViewModel : ViewModel() {
    private lateinit var bluetoothManager: BluetoothManager

    private val _Devices: MutableStateFlow<List<Device>> = MutableStateFlow(emptyList())
    val devices: StateFlow<List<Device>> = _Devices

    fun setBluetoothManager(bluetoothManager: BluetoothManager) {
        this.bluetoothManager = bluetoothManager
    }

    fun start(){
        startDeviceDiscovery()
    }

    fun startDeviceDiscovery(){
        bluetoothManager.checkPermissions {
            bluetoothManager.startDeviceDiscovery {
                _Devices.value = _Devices.value + it
            }
        }
    }
}