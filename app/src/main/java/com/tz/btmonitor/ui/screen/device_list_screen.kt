package com.tz.btmonitor.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tz.btmonitor.model.Device
import com.tz.btmonitor.ui.navigation.LocalViewModel
import com.tz.btmonitor.ui.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeviceListScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    val viewModel = LocalViewModel.current
    val connectingToDevice by viewModel.connectingToDevice.observeAsState(false)
    val isConnectedToDevice by viewModel.isConnectedToDevice.observeAsState(false)
    val fileSelected by viewModel.fileSelected.observeAsState(false)
    val devices by viewModel.devices.observeAsState()
    val pagerState = rememberPagerState(pageCount = {
        3
    })

    val gotoNextPage: () -> Unit = {
        coroutineScope.launch {
            pagerState.scrollToPage(pagerState.currentPage + 1)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.startDeviceDiscovery()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Pager for multiple pages
        HorizontalPager(state = pagerState) { page ->
            when (page) {
                0 -> ChooseDevicePage(
                    devices = devices,
                    onItemClick = { device ->
                        viewModel.setSelectedDevice(device)
                        gotoNextPage()
                    }
                ) {
                    viewModel.startDeviceDiscovery()
                }

                1 -> ChooseFileDestinationPage(
                    selectedFilename = if (fileSelected) viewModel.fileWriter.fileName else null
                ) {
                    viewModel.chooseFileDestination()
                }
            }
        }

        if (isConnectedToDevice) navController.navigate(Screen.TileList.route)

        // Button to navigate to the next screen
        if (pagerState.currentPage > 0) {
            Button(
                onClick = {
                    if (connectingToDevice) return@Button
                    viewModel.connectToSelectedDevice()
                },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .align(alignment = Alignment.BottomCenter)
            ) {
                Text(text = if (connectingToDevice) "Connecting..." else "Connect")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChooseDevicePage(devices: List<Device>?, onItemClick: (Device) -> Unit, onRefresh: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        LazyColumn {
            stickyHeader {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Pick a device",
                        modifier = Modifier.align(alignment = Alignment.Center)
                    )
                    TextButton(onClick = onRefresh) {
                        Text(text = "Scan", style = TextStyle(color = Color(0xFF2196F3)))
                    }
                }
            }
            devices?.let {
                items(devices) { device ->
                    DeviceListItem(
                        deviceName = device.name,
                        deviceAddress = device.address
                    ) {
                        onItemClick(device)
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceListItem(
    deviceName: String,
    deviceAddress: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(color = MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
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

@Composable
fun ChooseFileDestinationPage(selectedFilename: String?, onClick: () -> Unit) {
    val stroke = Stroke(
        width = 2f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    )

    Column {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Choose file destination",
            modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(32.dp)
                .clip(shape = RoundedCornerShape(8.dp))
                .background(color = Color.LightGray.copy(alpha = 0.3f))
                .drawBehind {
                    drawRoundRect(
                        color = Color.Gray,
                        style = stroke,
                        cornerRadius = CornerRadius(8.dp.toPx())
                    )
                }
                .clickable(onClick = onClick)
        ) {
            Text(
                text = selectedFilename ?: "Click to choose",
                modifier = Modifier.align(alignment = Alignment.Center)
            )
        }
    }
}