package com.example.btwalkietalkie.controller

import com.example.btwalkietalkie.data.BtDevices
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {
    val scannedDevices:StateFlow<List<BtDevices>>
    val pairedDevices:StateFlow<List<BtDevices>>

    fun startDiscovery()
    fun stopDiscovery()

    fun release()
}