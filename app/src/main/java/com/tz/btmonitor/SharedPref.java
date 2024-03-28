package com.tz.btmonitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.ArrayMap;

import com.tz.btmonitor.model.Channel;

import java.util.Map;

public class SharedPref {

    private static final String PREF_NAME = "channel_prefs";

    // Keys for storing channel information
    // Keys for storing channel information
    private static final String KEY_PREFIX = "channel_";

    private static SharedPref instance;
    private SharedPreferences preferences;
    private SharedPref(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void init(Context context){
        instance = new SharedPref(context);
    }

    public static SharedPref getInstance() {
        return instance;
    }

    // Save channel configuration for a specific device
    public SharedPref saveChannelConfig(String deviceAddress, Channel channel) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_PREFIX + deviceAddress + "_" + channel.getId() + "_min", channel.getMin());
        editor.putInt(KEY_PREFIX + deviceAddress + "_" + channel.getId() + "_max", channel.getMax());
        editor.putInt(KEY_PREFIX + deviceAddress + "_" + channel.getId() + "_d", channel.getD());
        editor.apply();

        return this;
    }

    // Save multiple channel configurations for a specific device
    public SharedPref saveChannels(String deviceAddress, Map<Integer, Channel> channels) {
        SharedPreferences.Editor editor = preferences.edit();
        for (Map.Entry<Integer, Channel> entry : channels.entrySet()) {
            Channel channel = entry.getValue();
            editor.putInt(KEY_PREFIX + deviceAddress + "_" + channel.getId() + "_min", channel.getMin());
            editor.putInt(KEY_PREFIX + deviceAddress + "_" + channel.getId() + "_max", channel.getMax());
            editor.putInt(KEY_PREFIX + deviceAddress + "_" + channel.getId() + "_d", channel.getD());
        }
        editor.apply();

        return this;
    }

    // Retrieve channel configuration for a specific device and channel
    public Channel getChannelConfig(String deviceAddress, int channelId) {
        Channel channel = new Channel();
        channel.setId(channelId);
        channel.setMin(preferences.getInt(KEY_PREFIX + deviceAddress + "_" + channelId + "_min", 0));
        channel.setMax(preferences.getInt(KEY_PREFIX + deviceAddress + "_" + channelId + "_max", 0));
        channel.setD(preferences.getInt(KEY_PREFIX + deviceAddress + "_" + channelId + "_d", 0));
        return channel;
    }

    // Get all channels for a specific device
    public Map<Integer, Channel> getAllChannels(String deviceAddress) {
        Map<Integer, Channel> channels = new ArrayMap<>();
        Map<String, ?> allPrefs = preferences.getAll();
        for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(KEY_PREFIX + deviceAddress)) {
                String[] parts = key.split("_");
                int channelId = Integer.parseInt(parts[2]);
                Channel channel = getChannelConfig(deviceAddress, channelId);
                channels.put(channelId, channel);
            }
        }
        return channels;
    }
}
