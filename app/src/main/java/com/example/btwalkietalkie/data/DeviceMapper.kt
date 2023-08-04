package com.example.btwalkietalkie.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice

@SuppressLint("MissingPermission")
fun BluetoothDevice.toBtDevices():BtDevices{
    return BtDevices(
        name = name,
        address = address
    )
}