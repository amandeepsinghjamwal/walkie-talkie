package com.example.btwalkietalkie.data

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.btwalkietalkie.BtBroadcastReceiver
import com.example.btwalkietalkie.controller.BluetoothController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@SuppressLint("MissingPermission")
class AndroidBtController(
    private val context: Context
) : BluetoothController {

    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private val _scannedDevices = MutableStateFlow<List<BtDevices>>(emptyList())
    override val scannedDevices: StateFlow<List<BtDevices>>
        get() = _scannedDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BtDevices>>(emptyList())
    override val pairedDevices: StateFlow<List<BtDevices>>
        get() = _pairedDevices.asStateFlow()

    private val onDeviceFoundReceiver = BtBroadcastReceiver{bluetoothDevice ->
        _scannedDevices.update {devices->
            val newDevice = bluetoothDevice.toBtDevices()
            Log.e("deviceee",newDevice.address)
            if(newDevice in devices) devices else devices + newDevice
        }
    }

    init {
        updatePairedDevices()
    }

    override fun startDiscovery() {
        if(!hasPermission(Manifest.permission.BLUETOOTH_SCAN)){
            return
        }
        Log.e("deviceeee","here")
        context.registerReceiver(onDeviceFoundReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        updatePairedDevices()
        bluetoothAdapter?.startDiscovery()

    }

    override fun stopDiscovery() {
        if(!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)){
            return
        }
        bluetoothAdapter?.cancelDiscovery()
    }

    override fun release() {
        context.unregisterReceiver(onDeviceFoundReceiver)
    }


    private fun updatePairedDevices() {
        if(!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)){
            return
        }
        bluetoothAdapter
            ?.bondedDevices
            ?.map {
                it.toBtDevices()
        }?.also {devices->
            _pairedDevices.update { devices }
            }
    }

    private fun hasPermission(perm:String):Boolean{
        if(Build.VERSION.SDK_INT > 31){
            if (ActivityCompat.checkSelfPermission(
                    context,
                    perm
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }
}