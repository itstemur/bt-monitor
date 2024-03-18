package com.tz.btmonitor.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.tz.btmonitor.model.Device

@SuppressLint("MissingPermission")
fun BluetoothDevice.toDevice(): Device{
    return Device(
        name = name,
        address = address,
        bluetoothDevice = this
    )
}