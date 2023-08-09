package com.example.btwalkietalkie

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.btwalkietalkie.screens.ConnectionScreen
import com.example.btwalkietalkie.screens.MainScreen
import com.example.btwalkietalkie.ui.theme.BTWalkieTalkieTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.jar.Manifest

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val bluetoothManager by lazy {
        applicationContext.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }
    private lateinit var viewModel: BtViewModel

    private val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestTimeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){

        }

        val enableBluetoothLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && isBluetoothEnabled) {
                viewModel.startScan()
//                requestTimeLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE))
            }
        }

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { perms ->
            val canEnableBluetooth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                perms[android.Manifest.permission.BLUETOOTH_CONNECT] == true
            } else true

            if (canEnableBluetooth && !isBluetoothEnabled) {
                enableBluetoothLauncher.launch(
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                )
                requestTimeLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                    putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                })
                viewModel.startScan()
            }
            else if (canEnableBluetooth && isBluetoothEnabled){
                requestTimeLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                    putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                })
                viewModel.startScan()
            }
        }

        permissionLauncher.launch(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.RECORD_AUDIO
                )
            } else {
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.RECORD_AUDIO
                )
            }
        )


        setContent {
            BTWalkieTalkieTheme {
                viewModel = hiltViewModel<BtViewModel>()
                val state by viewModel.state.collectAsState()
                // A surface container using the 'background' color from the theme

                LaunchedEffect(key1 = state.errorMessage) {
                    state.errorMessage?.let {
                        Toast.makeText(
                            applicationContext,
                            it,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                LaunchedEffect(key1 = state.isConnected) {
                    if (state.isConnected) {
                        Toast.makeText(
                            applicationContext,
                            "Connected",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    state.msg
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        state.isConnecting -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Text(text = "Connecting...")
                                TextButton(onClick = {viewModel.disconnectFromDevice()}) {
                                    Text(text = "Cancel")
                                }
                            }
                        }

                        state.isConnected -> {
                            ConnectionScreen(
                                onStopRecording = viewModel::stopRecording,
                                onSendMessage = viewModel::sendMessage,
                                onDisconnect = viewModel::disconnectFromDevice
                            )
                        }

                        else -> {
                            MainScreen(
                                state = state,
                                onStartScan = viewModel::startScan,
                                onStopScan = viewModel::stopScan,
                                onDeviceClicked = viewModel::connectToDevice,
                                onStartServer = viewModel::waitForIncomingConnection
                            )
                        }
                    }

                }
            }
        }
    }

}
