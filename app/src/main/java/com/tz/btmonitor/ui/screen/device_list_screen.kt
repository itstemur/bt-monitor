package com.tz.btmonitor.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tz.btmonitor.model.BluetoothDevice

@Composable
fun DeviceListScreen(navController: NavController) {
    // Dummy list of Bluetooth devices for demonstration
    val devices = remember {
        listOf(
            BluetoothDevice("Device 1", "00:11:22:33:AA:BB", 80),
            BluetoothDevice("Device 2", "11:22:33:44:BB:CC", 60),
            BluetoothDevice("Device 3", "22:33:44:55:CC:DD", 40)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // List of devices
        LazyColumn {
            items(devices) { device ->
                DeviceListItem(
                    deviceName = device.name,
                    deviceAddress = device.address,
                    signalStrength = device.signalStrength
                )
            }
        }

        // Dropdown for baud rate selection
        BaudRateDropDown()

        // File destination selection
        FileDestinationSelection()
    }
}

@Composable
fun BaudRateDropDown() {
    // Dummy list of baud rates for demonstration
    val baudRates = listOf("9600", "19200", "38400", "57600", "115200")
    var selectedBaudRate by remember { mutableStateOf(baudRates[0]) }

    Column {
        Text("Select Baud Rate")
        Spacer(modifier = Modifier.height(8.dp))
        DropdownMenu(
            expanded = false,
            onDismissRequest = { /* Dismiss the dropdown */ }
        ) {
            baudRates.forEach { baudRate ->
                DropdownMenuItem(
                    text = {
                        Text(text = baudRate)
                    },
                    onClick = {
                        selectedBaudRate = baudRate
                    })
            }
        }
        Text(text = "Selected Baud Rate: $selectedBaudRate")
    }
}

@Composable
fun FileDestinationSelection() {
    // Dummy list of file destinations for demonstration
    val fileDestinations = listOf("Internal Storage", "External Storage", "Custom Path")
    var selectedFileDestination by remember { mutableStateOf(fileDestinations[0]) }

    Column {
        Text("Select File Destination")
        Spacer(modifier = Modifier.height(8.dp))
        DropdownMenu(
            expanded = false,
            onDismissRequest = { /* Dismiss the dropdown */ }
        ) {
            fileDestinations.forEach { destination ->
                DropdownMenuItem(
                    text = {
                        Text(text = destination)
                    },
                    onClick = {
                        selectedFileDestination = destination
                    })
            }
        }
        Text(text = "Selected Destination: $selectedFileDestination")
    }
}


@Composable
fun DeviceListItem(
    deviceName: String,
    deviceAddress: String,
    signalStrength: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(color = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = deviceName,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Address: $deviceAddress",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Signal Strength: $signalStrength",
                style = MaterialTheme.typography.bodyMedium,
                color = if (signalStrength >= 50) Color.Green else Color.Red
            )
        }
    }
}