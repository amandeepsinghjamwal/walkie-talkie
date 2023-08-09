package com.example.btwalkietalkie.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.btwalkietalkie.data.BluetoothUiState
import com.example.btwalkietalkie.data.BtDevices

@Composable
fun MainScreen(
    state: BluetoothUiState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onStartServer: () -> Unit,
    onDeviceClicked: (BtDevices) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        DeviceList(
            pairedDevices = state.pairedDevices,
            scannedDevices = state.scannedDevices,
            onClick = onDeviceClicked,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment= Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
                    .height(40.dp)
                    .background(color = MaterialTheme.colorScheme.primary)
                    .clip(shape = RectangleShape)
                    .clickable {
                        onStartScan()
                    },
            ) {
                Text(
                    text = "Look for devices",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Divider(modifier = Modifier.fillMaxWidth(), color = Color.Gray, thickness = 1.dp)
            Box(
                contentAlignment= Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
                    .height(40.dp)
                    .background(color = MaterialTheme.colorScheme.primary)
                    .clip(shape = RectangleShape)
                    .clickable {
                        onStartServer()
                    },
            ) {
                Text(
                    text = "Host on this device",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
fun DeviceList(
    pairedDevices: List<BtDevices>,
    scannedDevices: List<BtDevices>,
    onClick: (BtDevices) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
//        item {
//            Text(
//                text = "Paired Devices",
//                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
//                modifier = Modifier.padding(10.dp)
//            )
//        }
//        items(pairedDevices) { device ->
//            Column {
//                Text(
//                    text = device.name ?: "Unknown Device",
//                    style = MaterialTheme.typography.bodyLarge,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clickable { onClick(device) }
//                        .padding(horizontal = 10.dp, vertical = 15.dp)
//                )
//                Divider(modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 10.dp))
//            }
//
//        }
        item {
            Text(
                text = "Available Devices",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(10.dp)
            )
        }
        items(scannedDevices) { device ->
            if (device.name != null) {
                Column {
                    Text(
                        text = device.name ?: "Unknown Device",
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClick(device) }
                            .padding(horizontal = 10.dp, vertical = 15.dp)
                    )
                    Divider(modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp))
                }
            }

        }
    }
}