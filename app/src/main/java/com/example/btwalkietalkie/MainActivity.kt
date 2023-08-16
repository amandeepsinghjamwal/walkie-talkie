package com.example.btwalkietalkie

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.UserManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.node.modifierElementOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.btwalkietalkie.screens.ConnectionScreen
import com.example.btwalkietalkie.screens.MainScreen
import com.example.btwalkietalkie.ui.theme.BTWalkieTalkieTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.jar.Manifest

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var requestTimeLauncher: ActivityResultLauncher<Intent>

    private val bluetoothManager by lazy {
        applicationContext.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }
    private lateinit var viewModel: BtViewModel
    var isPermissionAsked = mutableStateOf(true)
    private val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    var dialogVisibility = mutableStateOf(false)

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestTimeLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            }

        val enableBluetoothLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && isBluetoothEnabled) {
                viewModel.startScan()
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
                if(bluetoothAdapter?.scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
                    launchBtVisibility()
                }
            } else if (canEnableBluetooth && isBluetoothEnabled) {
                if(bluetoothAdapter?.scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
                    launchBtVisibility()
                }
//                viewModel.startScan()
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
                viewModel = hiltViewModel()
                val state by viewModel.state.collectAsState()
                // A surface container using the 'background' color from the theme
                viewModel.startScan()
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
                    ShowDialog()
                    when {
                        state.isConnecting -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                LoadingAnimation()
                                Text(text = "Waiting for connection", Modifier.padding(top = 15.dp))
                                TextButton(
                                    onClick = { viewModel.disconnectFromDevice() }
                                ) {
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
                                onInfoClicked = {
                                    dialogVisibility.value = !dialogVisibility.value
                                },
                                onDeviceClicked = viewModel::connectToDevice,
                                onStartServer = viewModel::waitForIncomingConnection
                            )
                        }
                    }

                }
            }
        }
    }

    @Composable
    fun ShowDialog() {
        if (dialogVisibility.value) {
            AlertDialog(
                onDismissRequest = { dialogVisibility.value = !dialogVisibility.value },
                confirmButton = {
                    Text(
                        text = "Ok",
                        modifier = Modifier.clickable {
                            launchBtVisibility()
                            dialogVisibility.value = !dialogVisibility.value
                        }
                    )
                },
                dismissButton = {
                    Text(text = "cancel", modifier = Modifier
                        .clickable {
                            dialogVisibility.value = !dialogVisibility.value
                        }
                        .padding(horizontal = 10.dp)
                    )

                },
//            icon = { Icon(imageVector = Icons.Default.Info, contentDescription = "") },
                title = { Text(text = "Can't see your device?") },
                text =
                { Text(text = "If you can't see your device in other device list, please allow your bluetooth to be discoverable by pressing ok") },
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            )
        }
    }

    @Composable
    fun LoadingAnimation(
        modifier: Modifier = Modifier,
        circleSize: Dp = 15.dp,
        circleColor: Color = MaterialTheme.colorScheme.primary,
        spaceBetween: Dp = 10.dp,
        travelDistance: Dp = 20.dp
    ) {
        val circles = listOf(
            remember { Animatable(initialValue = 0f) },
            remember { Animatable(initialValue = 0f) },
            remember { Animatable(initialValue = 0f) }
        )

        circles.forEachIndexed { index, animatable ->
            LaunchedEffect(key1 = animatable) {
                delay(index * 100L)
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = 1200
                            0.0f at 0 with LinearOutSlowInEasing
                            1.0f at 300 with LinearOutSlowInEasing
                            0.0f at 600 with LinearOutSlowInEasing
                            0.0f at 1200 with LinearOutSlowInEasing
                        },
                        repeatMode = RepeatMode.Restart
                    )
                )
            }
        }

        val circleValues = circles.map { it.value }
        val distance = with(LocalDensity.current) { travelDistance.toPx() }

        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(spaceBetween)
        ) {
            circleValues.forEach { value ->
                Box(
                    modifier = Modifier
                        .size(circleSize)
                        .graphicsLayer {
                            translationY = -value * distance
                        }
                        .background(
                            color = circleColor,
                            shape = CircleShape
                        )
                )
            }
        }

    }

    private fun launchBtVisibility() {
            requestTimeLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            })
    }
}





