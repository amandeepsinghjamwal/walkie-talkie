package com.example.btwalkietalkie.data

import java.security.MessageDigest

data class BluetoothUiState(
    val scannedDevices: List<BtDevices> = emptyList(),
    val pairedDevices: List<BtDevices> =  emptyList(),
    val isConnected:Boolean=false,
    val isConnecting:Boolean= false,
    val errorMessage:String? = null,
    val msg:ByteArray = byteArrayOf()
)