package com.example.btwalkietalkie.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.btwalkietalkie.data.BluetoothUiState
import com.example.btwalkietalkie.data.BtDevices

@Composable
fun MainScreen(
    state: BluetoothUiState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        DeviceList(
            pairedDevices = state.pairedDevices,
            scannedDevices = state.scannedDevices,
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = onStartScan) {
                Text(text = "Start scan")
            }
            Button(onClick = onStopScan) {
                Text(text = "Stop scan")
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
        item {
            Text(
                text = "Paired Devices",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(10.dp)
            )
        }
        items(pairedDevices) { device ->
            Text(
                text = device.name ?: "Unknown Device",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .clickable { onClick(device) }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }
        item {
            Text(
                text = "Scanned Devices",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(10.dp)
            )
        }
        items(scannedDevices) { device ->
            if(device.name!=null){
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.clickable {
                        onClick(device)
                    }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

        }
    }
}