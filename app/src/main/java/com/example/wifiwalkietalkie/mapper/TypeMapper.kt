package com.example.wifiwalkietalkie.mapper

import android.net.wifi.p2p.WifiP2pDevice
import com.example.wifiwalkietalkie.data.WifiDirectDevice

fun WifiP2pDevice.toWifiP2PList():WifiDirectDevice{
    return WifiDirectDevice(
        name=deviceName,
        address = deviceAddress
    )
}