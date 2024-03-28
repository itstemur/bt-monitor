package com.tz.btmonitor.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.tz.btmonitor.model.Channel
import com.tz.btmonitor.ui.navigation.LocalViewModel
import com.tz.btmonitor.ui.navigation.Screen

@Composable
fun TileListScreen(navController: NavController) {
    val viewModel = LocalViewModel.current
    val isConnectedToDevice by viewModel.isConnectedToDevice.observeAsState(false)
    val channels by viewModel.channels.observeAsState()

    if (!isConnectedToDevice || viewModel.selectedDevice == null) {
        navController.navigate(Screen.DeviceList.route)
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Connected to ${viewModel.selectedDevice.name}",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
            )
            Text(
                text = viewModel.selectedDevice.address,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
            )
            Button(
                modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
                onClick = {
                    viewModel.disconnect()
                }
            ) {
                Text(text = "Disconnect")
            }

            BaudRateDropDown { baudRateNum ->
                viewModel.setBaudRate(baudRateNum)
            }

            channels?.let {
                ChannelList(channels = it) { id, channel ->
                    viewModel.configChannel(channel)
                }
            }
        }
    }
}

@Composable
fun ChannelList(channels: Map<Int, Channel>, onUpdateChannel: (Int, Channel) -> Unit) {
    var selectedChannelId by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(channels.toList().chunked(2)) { channelsPair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                channelsPair.forEach { (channelId, channel) ->
                    ChannelTile(
                        channelId, channel,
                        modifier = Modifier
                            .weight(1f) // Occupy equal space
                            .aspectRatio(1f)
                    ) {
                        selectedChannelId = channelId
                    }
                }
            }
        }
    }

    selectedChannelId?.let { channelId ->
        channels[channelId]?.run {
            ChannelDialog(
                this,
                onSave = { newChannel ->
                    selectedChannelId = null
                    onUpdateChannel(channelId, newChannel)
                },
                onCancel = {
                    selectedChannelId = null
                }
            )
        }
    }
}

@Composable
fun ChannelTile(
    channelId: Int,
    channel: Channel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .padding(8.dp)
            .background(getChannelTileColor(channelId))
            .clickable { onClick() }
    ) {
        Text(
            text = "Channel $channelId",
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.TopCenter)
        )

        Text(
            text = channel.value.toString(),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.Center)
        )


        Column(
            modifier = Modifier.align(alignment = Alignment.BottomStart)
        ) {
            Text(text = "Min: ${channel.min}")
            Text(text = "Max: ${channel.max}")
        }

        Text(text = "D: ${channel.d}", modifier = Modifier.align(alignment = Alignment.BottomEnd))
    }
}

fun getChannelTileColor(channelId: Int): Color {
    return if (channelId % 2 == 0) {
        Color.LightGray
    } else {
        Color.Gray
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigEditor(label: String, value: Int?, onValueChange: (Int?) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "$label:", modifier = Modifier.weight(1f))

        OutlinedTextField(
            value = value?.toString() ?: "",
            onValueChange = {
                onValueChange(it.toIntOrNull())
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .weight(2f),
            singleLine = true,
            maxLines = 1,
            textStyle = TextStyle.Default.copy(textAlign = TextAlign.End),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.Gray
            )
        )
    }
}

@Composable
fun ChannelDialog(
    channel: Channel,
    onSave: (Channel) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current

    var newMin by remember { mutableStateOf<Int?>(channel.min) }
    var newMax by remember { mutableStateOf<Int?>(channel.max) }
    var newD by remember { mutableStateOf<Int?>(channel.d) }

    Dialog(
        onDismissRequest = onCancel
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Channel Configuration", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(32.dp))
                ConfigEditor("Min", newMin) { newMin = it }
                Spacer(modifier = Modifier.height(8.dp))
                ConfigEditor("Max", newMax) { newMax = it }
                Spacer(modifier = Modifier.height(8.dp))
                ConfigEditor("D", newD) { newD = it }
                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = {
                        if(newMin == null || newMin!! < 100)
                        {
                            Toast.makeText(context, "Min can't be less than 100", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if(newMax == null || newMax!! > 20000)
                        {
                            Toast.makeText(context, "Max can't be bigger than 20000", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if(newD == null)
                        {
                            Toast.makeText(context, "D value can't be empty", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val newChannel = channel.copy()
                        newChannel.min = newMin!!
                        newChannel.max = newMax!!
                        newChannel.d = newD!!

                        onSave(newChannel)
                    }, modifier = Modifier.padding(end = 8.dp)) {
                        Text(text = "Save")
                    }
                    Button(onClick = onCancel) {
                        Text(text = "Cancel")
                    }
                }
            }
        }
    }
}


@Composable
fun BaudRateDropDown(onSelect: (Int) -> Unit) {
    // Dummy list of baud rates for demonstration
    val baudRates =
        arrayOf("1200", "2400", "4800", "9600", "19200", "38400", "57600", "115200", "230400")

    var selectedBaudRate by remember { mutableStateOf(baudRates[3]) }
    var isDropDownOpen by remember {
        mutableStateOf(false)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .clip(shape = RoundedCornerShape(8.dp))
            .background(color = Color(0xFFB2DFDB))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "Selected Baud Rate:")
        Box(
        ) {
            Row(
                modifier = Modifier.clickable { isDropDownOpen = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = selectedBaudRate)
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "")
            }
            DropdownMenu(
                expanded = isDropDownOpen,
                onDismissRequest = { isDropDownOpen = false }
            ) {
                baudRates.forEach { baudRate ->
                    DropdownMenuItem(
                        text = {
                            Text(text = baudRate)
                        },
                        onClick = {
                            selectedBaudRate = baudRate
                            isDropDownOpen = false
                            onSelect(baudRates.indexOf(baudRate) + 1)
                        })
                }
            }
        }
    }
}