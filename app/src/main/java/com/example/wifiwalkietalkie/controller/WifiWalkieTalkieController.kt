package com.example.wifiwalkietalkie.controller

import com.example.wifiwalkietalkie.data.ConnectionResult
import com.example.wifiwalkietalkie.data.WifiDirectDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.Thread.State
import java.net.InetAddress

interface WifiWalkieTalkieController {
    val isWifiP2PEnabled:StateFlow<Boolean>
    val isConnected:StateFlow<Boolean>
    val isGroupOwner:StateFlow<Boolean>
    val error:SharedFlow<String>
    val groupOwnerAddress:StateFlow<InetAddress?>
    val scannedDevices:StateFlow<List<WifiDirectDevice>>
    val isWifiEnabled:StateFlow<Boolean>
    fun startSearch()
    fun startServer():Flow<ConnectionResult>
    fun connectToServer(ownerAddress: InetAddress?):Flow<ConnectionResult>
    fun connectToDevice(device: WifiDirectDevice):Flow<ConnectionResult>
    fun closeConnection()
    fun stopRecording()
    suspend fun sendMessage()
    fun onRelease()
}