package com.example.btwalkietalkie.data

data class BluetoothUiState(
    val scannedDevices: List<BtDevices> = emptyList(),
    val pairedDevices: List<BtDevices> =  emptyList()
)