package com.example.wifiwalkietalkie.data

import java.net.InetAddress

data class AppUiState(
    val scannedDevices: List<WifiDirectDevice> = emptyList(),
    val errorMessage:String? = null,
    val isConnected:Boolean=false,
    val isConnecting:Boolean=false,
    val isWifiEnabled:Boolean=false,
    val isGroupOwner:Boolean=false,
    val groupAddress:InetAddress?=null,
    val isTransferStarted:Boolean=false
)
