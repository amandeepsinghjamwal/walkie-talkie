package com.example.wifiwalkietalkie

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.wifiwalkietalkie.screens.ConnectionScreen
import com.example.wifiwalkietalkie.screens.MainScreen
import com.example.wifiwalkietalkie.screens.ServerClientScreen
import com.example.wifiwalkietalkie.ui.theme.WifiWalkieTalkieTheme
import com.example.wifiwalkietalkie.viewmodel.WifiViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WifiWalkieTalkieTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel = hiltViewModel<WifiViewModel>()
                    val state by viewModel.state.collectAsState()
                    LaunchedEffect(key1 = state.isWifiEnabled) {
                        if (!state.isWifiEnabled) {
                            val panelIntent = Intent(Settings.ACTION_WIFI_SETTINGS)
                            startActivity(panelIntent)
                        }
                    }
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
                                    onClick = { viewModel.disconnectFromDevice()}
                                ) {
                                    Text(text = "Cancel")
                                }
                            }
                        }

                        state.isTransferStarted->{
                            ConnectionScreen(
                                onStopRecording = viewModel::stopRecording,
                                onSendMessage = viewModel::sendMessage,
                                onDisconnect = viewModel::disconnectFromDevice
                            )
                        }

                        state.isConnected -> {
                            ServerClientScreen(
                                state = state,
                                onClientConnect = viewModel::connectToTheServer,
                                onServerStart = viewModel::startServer)
                        }

                        else -> {
                            MainScreen(
                                state = state,
                                onStartScan = viewModel::startScan,
                                onInfoClicked = viewModel::startScan,
                                onDeviceClicked = viewModel::connectToDevice,
                            )
                        }
                    }
                }
            }
        }
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
