package com.tz.btmonitor.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tz.btmonitor.model.Device
import com.tz.btmonitor.ui.navigation.Screen
import com.tz.btmonitor.viewmodel.BluetoothViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeviceListScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    val viewModel: BluetoothViewModel = viewModel()
    val devices by viewModel.devices.collectAsState()
    val pagerState = rememberPagerState(pageCount = {
        3
    })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pager for multiple pages
        HorizontalPager(state = pagerState) { page ->
            when (page) {
                0 -> ChooseDevicePage(devices = devices)
                1 -> ChooseBaudRatePage()
                2 -> ChooseFileDestinationPage()
            }
        }

        // Button to navigate to the next screen
        Button(
            onClick = {
                if (pagerState.currentPage == 2) {
                    navController.navigate(Screen.TileList.route)
                } else {
                          coroutineScope.launch {
                              pagerState.scrollToPage(pagerState.currentPage + 1)
                          }
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "Next")
        }
    }
}

@Composable
fun ChooseDevicePage(devices: List<Device>) {
    LazyColumn {
        items(devices) { device ->
            DeviceListItem(
                deviceName = device.name,
                deviceAddress = device.address
            )
        }
    }
}

@Composable
fun ChooseBaudRatePage() {
    // Implement Baud rate selection here
    Text("Choose Baud Rate Page")
}

@Composable
fun ChooseFileDestinationPage() {
    // Implement File destination selection here
    Text("Choose File Destination Page")
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
    deviceAddress: String
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
        }
    }
}