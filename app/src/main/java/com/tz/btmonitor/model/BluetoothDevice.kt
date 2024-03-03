package com.tz.btmonitor.model

data class BluetoothDevice(
    val name: String,
    val address: String,
    val signalStrength: Int
)