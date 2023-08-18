package com.example.wifiwalkietalkie.receivers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.util.Log
import com.example.wifiwalkietalkie.data.WifiDirectDevice
import com.example.wifiwalkietalkie.mapper.toWifiP2PList
import java.net.InetAddress

class WifiDevicesBroadcastReceiver(
    private val manager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel,
    private val isWifiEnabled: (Boolean) -> Unit,
    private val deviceList: (WifiDirectDevice) -> Unit,
    private val connectionInfo: (Boolean,Boolean,InetAddress?)->Unit
) : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                when (intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)) {
                    WifiP2pManager.WIFI_P2P_STATE_ENABLED -> {
                        isWifiEnabled(true)
                    }
                    else -> {
                        isWifiEnabled(false)
                        Log.e("hereee", "does it")
                    }
                }
            }

            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                manager.requestPeers(channel) { peers: WifiP2pDeviceList? ->
                    peers?.deviceList?.forEach{
                        deviceList(it.toWifiP2PList())
                        Log.e("hereee",it.deviceName.toString())
                    }
                }
            }

            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
//                Log.e("groupFormed","true")
                val wifiInfo= if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO,WifiP2pInfo::class.java)
                } else {
                    intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO)
                }
                Log.e("connection changed","yes")

                if(wifiInfo!=null){
                    if(wifiInfo.groupFormed && wifiInfo.isGroupOwner){
                        Log.e("connection changed2",wifiInfo.groupFormed.toString())
                        connectionInfo(true,true,null)
                    }
                    else if(wifiInfo.groupFormed && !wifiInfo.isGroupOwner){
                        Log.e("groupFormed","false")
                        connectionInfo(true,false,wifiInfo.groupOwnerAddress)
                    }
                    else{
                        connectionInfo(false,false,null)
                    }
                }else{
                    Log.e("groupFormednull","false")
                    connectionInfo(false,false,null)
                }

            }

            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                // Respond to this device's wifi state changing
            }
        }
    }
}