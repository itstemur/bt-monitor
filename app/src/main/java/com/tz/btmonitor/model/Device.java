package com.tz.btmonitor.model;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;

public class Device {
    private String name;
    private String address;
    private BluetoothDevice bluetoothDevice;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }


    @SuppressLint("MissingPermission")
    public static Device create(BluetoothDevice bluetoothDevice) {
        Device device = new Device();
        device.setName(bluetoothDevice.getName());
        device.setAddress(bluetoothDevice.getAddress());
        device.setBluetoothDevice(bluetoothDevice);
        return device;
    }
}