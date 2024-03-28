package com.tz.btmonitor.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tz.btmonitor.ui.screen.DeviceListScreen
import com.tz.btmonitor.ui.screen.TileListScreen
import com.tz.btmonitor.ui.theme.BtMonitorTheme
import com.tz.btmonitor.viewmodel.BluetoothViewModel


val LocalViewModel = compositionLocalOf<BluetoothViewModel> {
    throw Exception("ViewModel not provided")
}

class ComposeStarter{
    @Composable
    fun getContent(viewModel: BluetoothViewModel){
        BluetoothApp(viewModel = viewModel)
    }
}

@Composable
fun BluetoothApp(viewModel: BluetoothViewModel) {
    CompositionLocalProvider(LocalViewModel provides viewModel) {
        BtMonitorTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = Screen.DeviceList.route) {
                    composable(Screen.DeviceList.route) {
                        DeviceListScreen(navController = navController)
                    }
                    composable(Screen.TileList.route) {
                        TileListScreen(navController = navController)
                    }
                }
            }
        }
    }
}

sealed class Screen(val route: String) {
    object DeviceList : Screen("device_list")
    object TileList : Screen("tile_list")
}
