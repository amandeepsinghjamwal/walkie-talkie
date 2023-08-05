package com.example.btwalkietalkie.controller

import com.example.btwalkietalkie.data.BtDevices
import com.example.btwalkietalkie.data.ConnectionResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {
    val scannedDevices:StateFlow<List<BtDevices>>
    val pairedDevices:StateFlow<List<BtDevices>>
    val isConnected:StateFlow<Boolean>
    val error: SharedFlow<String>

    fun startDiscovery()
    fun stopDiscovery()

    fun release()

    fun startBtServer():Flow<ConnectionResult>
    fun connectToDevice(devices: BtDevices):Flow<ConnectionResult>
    fun closeConnection()
}