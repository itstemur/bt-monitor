package com.tz.btmonitor.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.tz.btmonitor.ActivityBridge;
import com.tz.btmonitor.SharedPref;
import com.tz.btmonitor.bluetooth.BluetoothClientManager;
import com.tz.btmonitor.bluetooth.Parser;
import com.tz.btmonitor.file_writer.FileWriter;
import com.tz.btmonitor.model.Channel;
import com.tz.btmonitor.model.Device;
import com.tz.btmonitor.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BluetoothViewModel extends ViewModel implements BluetoothClientManager.Callback {
    private final String TAG = "BluetoothViewModel";
    private BluetoothClientManager bluetoothManager;
    private ActivityBridge activityBridge;
    public Device selectedDevice;
    private final MutableLiveData<Map<Integer, Channel>> _channels = new MutableLiveData<>(Collections.emptyMap());
    public final LiveData<Map<Integer, Channel>> channels = _channels;

    public FileWriter fileWriter;
    private final MutableLiveData<Boolean> _fileSelected = new MutableLiveData<>();
    public final LiveData<Boolean> fileSelected = _fileSelected;

    private final MutableLiveData<List<Device>> _devices = new MutableLiveData<>();
    public final LiveData<List<Device>> devices = _devices;
    private final MutableLiveData<Boolean> _connectingToDevice = new MutableLiveData<>();
    public final LiveData<Boolean> connectingToDevice = _connectingToDevice;
    private final MutableLiveData<Boolean> _isConnectedToDevice = new MutableLiveData<>();
    public final LiveData<Boolean> isConnectedToDevice = _isConnectedToDevice;
    private boolean pauseReceiving = false;
    private LatLng lastUserLocation = new LatLng();


    public void setLastUserLocation(LatLng lastUserLocation) {
        this.lastUserLocation = lastUserLocation;
    }

    public void setSelectedDevice(Device selectedDevice) {
        this.selectedDevice = selectedDevice;
    }

    public void setActivityBridge(ActivityBridge bridge) {
        activityBridge = bridge;
    }

    public void setBluetoothManager(BluetoothClientManager bluetoothClientManager) {
        bluetoothManager = bluetoothClientManager;
        bluetoothManager.addListener(this);
    }

    public void startDeviceDiscovery() {
        activityBridge.checkPermissions(() -> bluetoothManager.startDeviceDiscovery());
    }

    public void chooseFileDestination() {
        _fileSelected.setValue(false);
        activityBridge.createFile(writer -> {
            Log.i(TAG, "File create successfully");
            fileWriter = writer;
            _fileSelected.setValue(true);
        });
    }

    public void connectToSelectedDevice() {
        if (selectedDevice == null) {
            Log.e(TAG, "Device is not selected");
            return;
        }

        if (fileWriter == null) {
            Log.e(TAG, "File is not open");
            return;
        }

        _connectingToDevice.setValue(true);
        bluetoothManager.connect(selectedDevice);
    }

    public void disconnect() {
        fileWriter.close();
        bluetoothManager.disconnect();
        _isConnectedToDevice.setValue(false);
        selectedDevice = null;
    }

    public void setBaudRate(int num) {
        bluetoothManager.sendMessage("AT+BAUD" + num);
    }

    public void configChannel(Channel channel) {
        Log.d(TAG, channel.toString());
        pauseReceiving = true;
        // save
        SharedPref.getInstance().saveChannelConfig(selectedDevice.getAddress(), channel);

        // update ui
        Map<Integer, Channel> chs = _channels.getValue();
        Channel oldChannel = chs.get(channel.getId()).copy();
        chs.put(channel.getId(), channel);
        _channels.setValue(chs);

        // if d changed, set config
        if (oldChannel != null && oldChannel.getD() != channel.getD()) {
            bluetoothManager.sendMessage(String.format(Locale.ENGLISH, "SET_CFG D%d=%d", channel.getId(), channel.getD()));
        }
        pauseReceiving = false;
    }

    @Override
    public void onDevicesDiscovered(List<Device> devices) {
        _devices.setValue(devices);
    }

    @Override
    public void onDiscoveryStarted() {

    }

    @Override
    public void onDiscoveryFinished() {

    }

    @Override
    public void onBluetoothEnabled() {

    }

    @Override
    public void onBluetoothDisabled() {

    }

    @Override
    public void onDeviceConnected() {
        _connectingToDevice.setValue(false);
        _isConnectedToDevice.setValue(true);

        // load channels
        _channels.setValue(SharedPref.getInstance().getAllChannels(selectedDevice.getAddress()));

        // request for config
        bluetoothManager.sendMessage("GET_CFG");
    }

    @Override
    public void onDeviceDisconnected() {
        _connectingToDevice.setValue(false);
        _isConnectedToDevice.setValue(false);
    }

    @Override
    public void onNewMessageReceived(String message) {
        Log.d("BLUETOOTH_VIEWMODEL", "----------------> new message: " + message);
        // Check if the received message is a configuration message
        if (message.startsWith("CH=")) {
            // Extract configuration information from the message
            Map<Integer, Channel> receivedChannels = Parser.parseConfigMessage(message);
            // Update or create channels using shared preferences
            updateOrCreateChannels(selectedDevice.getAddress(), receivedChannels);
            // update livedata
            _channels.setValue(SharedPref.getInstance().getAllChannels(selectedDevice.getAddress()));
        } else if (message.startsWith("VALUES")) {
            if(!pauseReceiving)
            {
                List<String> values = Parser.parseValuesMessage(message);

                Map<Integer, Channel> chs = _channels.getValue();
                Map<Integer, Channel> newChannels = new HashMap<>();
                for (Map.Entry<Integer, Channel> integerChannelEntry : chs.entrySet()) {
                    int channelId = integerChannelEntry.getKey();
                    Channel ch = integerChannelEntry.getValue().copy();
                    if (channelId < values.size()) {
                        ch.setValue(Integer.valueOf(values.get(channelId)));
                    }
                    newChannels.put(channelId, ch);
                }
                _channels.setValue(newChannels);

                // write to file
                values.add(0, lastUserLocation.toString());
                List<String[]> lines = new ArrayList<>();
                lines.add(values.toArray(new String[]{}));
                fileWriter.write(lines);
            }
            else {
                Log.i(TAG, "Skipping values");
            }
        }
    }


    // Function to update or create channels using shared preferences
    private void updateOrCreateChannels(String deviceAddress, Map<Integer, Channel> receivedChannels) {
        Log.d(TAG, receivedChannels.toString());
        // Get the saved channels from shared preferences
        Map<Integer, Channel> savedChannels = SharedPref.getInstance().getAllChannels(deviceAddress);

        // Iterate over received channels and update/create them in saved channels
        for (Map.Entry<Integer, Channel> entry : receivedChannels.entrySet()) {
            int channelId = entry.getKey();
            Channel receivedChannel = entry.getValue();

            if (savedChannels.containsKey(channelId)) {
                // Channel exists, update its configuration
                Channel savedChannel = savedChannels.get(channelId);
                savedChannel.setD(receivedChannel.getD());
            } else {
                // Channel doesn't exist, save it with default min max
                savedChannels.put(channelId, receivedChannel);
            }
        }

        // Save the updated channels to shared preferences
        SharedPref.getInstance().saveChannels(deviceAddress, savedChannels);
    }
}
