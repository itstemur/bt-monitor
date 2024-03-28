package com.tz.btmonitor.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.tz.btmonitor.model.Device;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothClientManager {
    private static final String TAG = "BluetoothClientManager";
    private static final UUID SSP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SerialPortService ID

    private final BluetoothAdapter bluetoothAdapter;
    private final List<Device> discoveredDevices = new ArrayList<>();
    private final List<Callback> listeners = new ArrayList<>();

    private BluetoothDevice connectedDevice;
    private BluetoothSocket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private boolean connected = false;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Handler postMessageHandler = new Handler();

    public BluetoothClientManager(BluetoothAdapter bluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter;
    }

    public void addListener(Callback listener) {
        listeners.add(listener);
    }

    public void removeListener(Callback listener) {
        listeners.remove(listener);
    }

    // life cycle actions
    public void registerReceiver(Context context) {
        // register discovery receiver
        IntentFilter f1 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(deviceDiscoveryReceiver, f1);

        // register state receiver
        IntentFilter f2 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(bluetoothStateReceiver, f2);
    }

    public void unregisterReceiver(Context context) {
        context.unregisterReceiver(deviceDiscoveryReceiver);
        context.unregisterReceiver(bluetoothStateReceiver);
    }

    // actions
    @SuppressLint("MissingPermission")
    public void startDeviceDiscovery() {
        if (connected) return;

        Log.d(TAG, "Start discovery");
        // clear first
        discoveredDevices.clear();

        // list already known devices
        for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
            discoveredDevices.add(Device.create(device));
        }
        notifyDevicesDiscovered(discoveredDevices);

        // discover new devices
        notifyDiscoveryStarted();
        bluetoothAdapter.startDiscovery();
        Log.d(TAG, "Discovery started");
    }

    @SuppressLint("MissingPermission")
    public void stopDeviceDiscovery() {
        Log.d(TAG, "Stop discovery");
        bluetoothAdapter.cancelDiscovery();
        notifyDiscoveryFinished();
        Log.d(TAG, "Discovery stopped");
    }

    @SuppressLint("MissingPermission")
    public void enableBluetooth(Context context) {
        Log.d(TAG, "Enable bluetooth");
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivity(enableBtIntent);
        }
    }

    @SuppressLint("MissingPermission")
    public void connect(Device device) {
        if (connected) {
            Log.e(TAG, "Already connected to a device");
            return;
        }

        new Thread(() -> {
            try {
                socket = device.getBluetoothDevice().createRfcommSocketToServiceRecord(SSP_UUID);
                socket.connect();
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                connectedDevice = device.getBluetoothDevice();
                connected = true;
                handler.post(this::notifyDeviceConnected);
                startReceivingMessages();
            } catch (IOException e) {
                Log.e(TAG, "Error connecting to device: " + e.getMessage());
                connected = false;
                handler.post(this::notifyDeviceDisconnected);
            }
        }).start();
    }

    public void disconnect() {
        if (!connected) {
            Log.e(TAG, "Not connected to any device");
            return;
        }

        try {
            connected = false;
            if (socket != null) {
                socket.close();
            }
            handler.post(this::notifyDeviceDisconnected);
        } catch (IOException e) {
            Log.e(TAG, "Error disconnecting: " + e.getMessage());
        }
    }

    public void sendMessage(String message) {
        if (!connected) {
            Log.e(TAG, "Not connected to any device");
            return;
        }

        postMessageHandler.post(() -> {
            try {
                outputStream.write(message.getBytes());
                outputStream.flush();
                Log.d(TAG, "Message sent: " + message);
            } catch (IOException e) {
                Log.e(TAG, "Error sending message: " + e.getMessage());
            }
        });
    }

    private void startReceivingMessages() {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int numBytes;
            try {
                while (connected) {
                    numBytes = inputStream.read(buffer);
                    String receivedMessage = new String(buffer, 0, numBytes);
                    Log.d(TAG, "Received message: " + receivedMessage);
                    handler.post(() -> notifyNewMessageReceived(receivedMessage));
                }
            } catch (IOException e) {
                Log.e(TAG, "Error receiving message: " + e.getMessage());
                connected = false;
                handler.post(this::notifyDeviceDisconnected);
            }
        }).start();
    }

    private void notifyDevicesDiscovered(List<Device> devices) {
        for (Callback listener : listeners) {
            listener.onDevicesDiscovered(devices);
        }
    }

    private void notifyDiscoveryStarted() {
        for (Callback listener : listeners) {
            listener.onDiscoveryStarted();
        }
    }

    private void notifyDiscoveryFinished() {
        for (Callback listener : listeners) {
            listener.onDiscoveryFinished();
        }
    }

    private void notifyBluetoothEnabled() {
        for (Callback listener : listeners) {
            listener.onBluetoothEnabled();
        }
    }

    private void notifyBluetoothDisabled() {
        for (Callback listener : listeners) {
            listener.onBluetoothDisabled();
        }
    }

    private void notifyDeviceConnected() {
        for (Callback listener : listeners) {
            listener.onDeviceConnected();
        }
    }

    private void notifyDeviceDisconnected() {
        for (Callback listener : listeners) {
            listener.onDeviceDisconnected();
        }
    }

    private void notifyNewMessageReceived(String message) {
        for (Callback listener : listeners) {
            listener.onNewMessageReceived(message);
        }
    }

    // Broadcast receivers
    private final BroadcastReceiver deviceDiscoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("deviceDiscoveryReceiver", "---------> results came");
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    discoveredDevices.add(Device.create(device));
                    notifyDevicesDiscovered(discoveredDevices);
                }
            }
        }
    };

    private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        notifyBluetoothDisabled();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        notifyBluetoothEnabled();
                        break;
                }
            }
        }
    };

    // Callback interface for Bluetooth events
    public interface Callback {
        void onDevicesDiscovered(List<Device> devices);

        void onDiscoveryStarted();

        void onDiscoveryFinished();

        void onBluetoothEnabled();

        void onBluetoothDisabled();

        void onDeviceConnected();

        void onDeviceDisconnected();

        void onNewMessageReceived(String message);
    }
}
