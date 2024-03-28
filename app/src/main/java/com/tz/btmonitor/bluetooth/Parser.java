package com.tz.btmonitor.bluetooth;

import android.util.Log;

import com.tz.btmonitor.model.Channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {
    // Function to parse configuration message and generate channel map
    public static Map<Integer, Channel> parseConfigMessage(String message) {
        Map<Integer, Channel> channelsMap = new HashMap<>();
        // Split the message by comma to separate key-value pairs
        String[] pairs = message.split(",");
        for (String pair : pairs) {
            // skip first
            if (pair.startsWith("CH=")) continue;

            // Split each pair by '=' to get key and value
            String[] keyValue = pair.replaceAll(" ", "").split("=");
            if (keyValue.length == 2) {
                try {
                    int channelId = Integer.parseInt(keyValue[0].substring(1)); // Extract channel ID from the key

                    int min = 100; // use default
                    int max = 20000; // use default
                    int d = Integer.parseInt(keyValue[1]);
                    // Create a new Channel object with parsed values
                    Channel channel = new Channel();
                    channel.setId(channelId);
                    channel.setMin(min);
                    channel.setMax(max);
                    channel.setD(d);
                    // Add the channel to the map
                    channelsMap.put(channelId, channel);
                } catch (NumberFormatException e) {
                    Log.e("BLUETOOTH_VIEWMODEL", "Error parsing configuration message: " + e.getMessage());
                }
            }
        }
        return channelsMap;
    }

    public static List<String> parseValuesMessage(String message) {
        List<String> values = new ArrayList<>();
        String[] parts = message.split(": ");
        if (parts.length == 2 && parts[0].equals("VALUES")) {
            String[] valueStrings = parts[1].split(",");
            for (String valueString : valueStrings) {
                values.add(valueString.trim());
            }
        }
        return values;
    }
}
