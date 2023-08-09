package com.example.btwalkietalkie.data

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.btwalkietalkie.recievers.BtBroadcastReceiver
import com.example.btwalkietalkie.controller.BluetoothController
import com.example.btwalkietalkie.recievers.BtStateReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

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
    private var serverSocket: BluetoothServerSocket? = null
    private var clientSocket: BluetoothSocket? = null

    private var dataTransferService:BtTransferService? = null

    private val _isConnected = MutableStateFlow<Boolean>(false)
    override val isConnected: StateFlow<Boolean>
        get() = _isConnected.asStateFlow()
    private val _scannedDevices = MutableStateFlow<List<BtDevices>>(emptyList())
    override val scannedDevices: StateFlow<List<BtDevices>>
        get() = _scannedDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BtDevices>>(emptyList())
    override val pairedDevices: StateFlow<List<BtDevices>>
        get() = _pairedDevices.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    override val error: SharedFlow<String>
        get() = _error.asSharedFlow()

    private val onDeviceFoundReceiver = BtBroadcastReceiver{bluetoothDevice ->
        _scannedDevices.update {devices->
            val newDevice = bluetoothDevice.toBtDevices()
            Log.e("deviceee",newDevice.address)
            if(newDevice in devices) devices else devices + newDevice
        }
    }

    private val btStateReceiver = BtStateReceiver{isConnected,device ->
        if(bluetoothAdapter?.bondedDevices?.contains(device)==true){
            _isConnected.update {
                isConnected
            }
            }else{
                CoroutineScope(Dispatchers.IO).launch{
                    _error.emit("Cannot connect")
                }
            }
        }

    init {
        updatePairedDevices()
        context.registerReceiver(
            btStateReceiver,
            IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
        )
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
        context.unregisterReceiver(btStateReceiver)
        closeConnection()
    }

    override fun startBtServer(): Flow<ConnectionResult> {
        return flow {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException("No permission")
            }
            serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                "walkie-talkie",
                UUID.fromString(SERVICE_UUID)
            )
            var shouldLoop = true
            try {
                while (shouldLoop) {
                    Log.e("isLoopRunningStill","true")
                    clientSocket = serverSocket?.accept()
                    if (clientSocket != null) {
                        shouldLoop = false
                        emit(ConnectionResult.ConnectionEstablished)
                        val service = BtTransferService(clientSocket!!)
                        dataTransferService = service
                        emitAll(
                            service.listenForIncomingMessages()
                                .map {
                                    ConnectionResult.TransferSucceeded(it)
                                }
                        )
                    }
                }
            } catch (e: IOException) {
                Log.e("serverException", e.message.toString())
                emit(ConnectionResult.Error("Server exception"))
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }



    override suspend fun trySendMessage(isRecording: Boolean) {
        if(!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)){
            return
        }
        if(dataTransferService==null){
            return
        }
        dataTransferService?.sendMessage(isRecording)
    }

    override fun stopRecording() {
        dataTransferService?.stopRecording()
    }

    override fun connectToDevice(devices: BtDevices): Flow<ConnectionResult> {
        return flow {
            if(!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)){
                throw SecurityException("No permission")
            }
            clientSocket = bluetoothAdapter
                ?.getRemoteDevice(devices.address)
                ?.createRfcommSocketToServiceRecord(
                    UUID.fromString(SERVICE_UUID)
                )
            stopDiscovery()
            clientSocket?.let {socket->
                try {
                    socket.connect()
                    emit(ConnectionResult.ConnectionEstablished)
                    BtTransferService(socket).also {
                        dataTransferService=it
                        emitAll(
                            it.listenForIncomingMessages()
                                .map {msg->
                                    ConnectionResult.TransferSucceeded(msg)
                                }
                        )
                    }
                }catch (e:IOException){
                    socket.close()
                    clientSocket = null
                    Log.e("hereee","it comes")
                    emit(ConnectionResult.Error("Connection interrupted"))
                }
            }
        }.onCompletion {
            Log.e("hereee","it ")
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override fun closeConnection() {
        serverSocket?.close()
        clientSocket?.close()
        serverSocket=null
        clientSocket=null
        Log.e("heree","connection closed")
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
    companion object{
        private const val SERVICE_UUID="27b7d1da-08c7-4505-a6d1-2459987e5e2d"
    }
}