package com.example.wifiwalkietalkie.data

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pManager
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.wifiwalkietalkie.controller.WifiWalkieTalkieController
import com.example.wifiwalkietalkie.receivers.WifiDevicesBroadcastReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.Exception
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

class WalkieTalkieController(
    private val context: Context
) : WifiWalkieTalkieController {

    private var dataTransferService: WiFiDataTransferService? = null
    private val manager: WifiP2pManager? by lazy(LazyThreadSafetyMode.NONE) {
        context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager?
    }

    private var wifiP2pChannel: WifiP2pManager.Channel? = null
    private var receiver: BroadcastReceiver? = null

    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null

    init {

        Log.e("hereeee", "reached")
        val intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }
        wifiP2pChannel = manager?.initialize(context, Looper.getMainLooper(), null)
        wifiP2pChannel?.also { channel ->
            receiver =
                WifiDevicesBroadcastReceiver(manager = manager!!, channel = channel,
                    { isEnabled ->
                        Toast.makeText(context, "direct enabled: $isEnabled", Toast.LENGTH_SHORT)
                            .show()
                        _isWifiEnabled.update {
                            isEnabled
                        }
                        _isWifiP2PEnabled.value = isEnabled
                        if (isEnabled) {
                            startSearch()
                        }
                    }, { scannedDevice ->
                        Log.e("hereee", scannedDevice.name)
                        _scannedDevices.update { devices ->
                            if (scannedDevice in devices) devices else devices + scannedDevice
                        }
                    }) { isConnected, isGroupOwner, ownerAddress ->

                    Log.e(
                        "connection changed",
                        "${isConnected.toString()} ${isGroupOwner.toString()}"
                    )
                    _isConnected.update {
                        isConnected
                    }
                    _isGroupOwner.update {
                        isGroupOwner
                    }
                    _groupOwnerAddress.update {
                        ownerAddress
                    }
                }
        }
        receiver?.also { receiver ->
            context.registerReceiver(receiver, intentFilter)
        }
    }

    private var _groupOwnerAddress = MutableStateFlow<InetAddress?>(null)
    override val groupOwnerAddress: StateFlow<InetAddress?>
        get() = _groupOwnerAddress

    private var _isWifiP2PEnabled = MutableStateFlow(false)
    override val isWifiP2PEnabled: StateFlow<Boolean>
        get() = _isWifiP2PEnabled.asStateFlow()

    private var _scannedDevices = MutableStateFlow<List<WifiDirectDevice>>(emptyList())
    override val scannedDevices: StateFlow<List<WifiDirectDevice>>
        get() = _scannedDevices.asStateFlow()

    private var _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean>
        get() = _isConnected.asStateFlow()

    private var _error = MutableSharedFlow<String>()
    override val error: SharedFlow<String>
        get() = _error.asSharedFlow()

    private var _isWifiEnabled = MutableStateFlow(false)
    override val isWifiEnabled: StateFlow<Boolean>
        get() = _isWifiEnabled.asStateFlow()

    private var _isGroupOwner = MutableStateFlow(false)
    override val isGroupOwner: StateFlow<Boolean>
        get() = _isGroupOwner.asStateFlow()

    private val activeSockets = mutableListOf<Socket>()
    private val activeClients = mutableListOf<WiFiDataTransferService>()

    @SuppressLint("MissingPermission")
    override fun startSearch() {
        Log.e("hereee", "${isWifiP2PEnabled.value}")
        if (isWifiP2PEnabled.value) {
            Log.e("hereee", "is is")
            manager?.discoverPeers(wifiP2pChannel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {}
                override fun onFailure(p0: Int) {}
            })
        }
    }

    @SuppressLint("MissingPermission")
    override fun connectToDevice(device: WifiDirectDevice): Flow<ConnectionResult> {
        return callbackFlow {
            val config = WifiP2pConfig().apply {
                deviceAddress = device.address
            }
            val actionListener =
                object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        try {
                            Toast.makeText(context, "Connection Successful", Toast.LENGTH_SHORT)
                                .show()
                            this@callbackFlow.trySend(ConnectionResult.ConnectionEstablished).isSuccess
                        } catch (e: Exception) {
                            e.printStackTrace()
                            this@callbackFlow.trySend(ConnectionResult.Error("Error connecting to device")).isSuccess
                        }
                    }

                    override fun onFailure(p0: Int) {
                        Toast.makeText(context, "Connection Failed", Toast.LENGTH_SHORT).show()
                    }

                }
            manager?.connect(wifiP2pChannel, config, actionListener)
            awaitClose { closeConnection() }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override fun connectToServer(ownerAddress: InetAddress?): Flow<ConnectionResult> {
        Log.e("connection changed", "inside server")
        return flow {
            try {
                clientSocket = Socket()
                clientSocket?.bind(null)
                clientSocket.let { socket ->
                    socket?.connect(InetSocketAddress(ownerAddress, 8989), 500)

                    Log.e("connection changed", "socket created")
                    emit(ConnectionResult.ReadyForDataTransfer)
                    dataTransferService = WiFiDataTransferService(socket,context)
                    activeClients.add(dataTransferService!!)
                    Log.e("connection changed", dataTransferService.toString())
                        dataTransferService!!.listenForIncomingMessage()
                }
            } catch (e: IOException) {
                Log.e("connection changed", e.message.toString())
                e.printStackTrace()
                emit(ConnectionResult.Error("Connection Interrupted"))
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override fun startServer(): Flow<ConnectionResult> {
        Log.e("connection changed", "server created")
        return flow {
            Log.e("connection changed", "is server created")
            var isRunning = true
            serverSocket = ServerSocket(8989)
            while (isRunning) {
                try {
                    clientSocket = serverSocket?.accept()
                    if (clientSocket != null) {
                            activeSockets.add(clientSocket!!)
                        Log.e("connection changed", "not null")
                        dataTransferService = WiFiDataTransferService(clientSocket,context)
                        activeClients.add(dataTransferService!!)
                        emit(ConnectionResult.ReadyForDataTransfer)
                        CoroutineScope(Dispatchers.IO).launch {
                            dataTransferService?.listenForIncomingMessage()
                        }
                    }
                } catch (e: IOException) {
                    isRunning = false
                    e.printStackTrace()
                    Log.e("connection changed", "what created")
                    emit(ConnectionResult.Error("Connection Interrupted"))
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun sendMessage() {
        for (activeClient in activeClients) {
            activeClient.sendMessage()
        }
    }

    override fun getLatestDeviceList(): MutableList<Socket> {
            return activeSockets
    }

    override fun stopRecording() {
        for (activeClient in activeClients) {
            activeClient.stopRecording()
        }
    }

    override fun closeConnection() {
        Log.e("connection changed", "closed")
        manager?.removeGroup(wifiP2pChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                _isConnected.update {
                    false
                }
            }

            override fun onFailure(reason: Int) {
                // Failed to remove the group. Handle the error.
            }
        })
        serverSocket?.close()
        clientSocket?.close()
        serverSocket = null
        clientSocket = null
    }

    override fun onRelease() {
        context.unregisterReceiver(receiver)
        closeConnection()
    }
}