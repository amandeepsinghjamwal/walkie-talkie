package com.example.wifiwalkietalkie.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wifiwalkietalkie.controller.WifiWalkieTalkieController
import com.example.wifiwalkietalkie.data.AppUiState
import com.example.wifiwalkietalkie.data.ConnectionResult
import com.example.wifiwalkietalkie.data.WifiDirectDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.InetAddress
import javax.inject.Inject

@HiltViewModel
class WifiViewModel
@Inject constructor(
    private val wifiDirectController: WifiWalkieTalkieController
) : ViewModel() {
    private val _state = MutableStateFlow(AppUiState())
    private var deviceConnectionJob: Job? = null
    var state = combine(
        wifiDirectController.scannedDevices,
        _state
    ) { scannedDevice, state ->
        state.copy(
            scannedDevices = scannedDevice
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

    fun startScan() {
        wifiDirectController.startSearch()
    }

    init {

        wifiDirectController.isGroupOwner.onEach {isGroupOwner->
            _state.update {
                it.copy(
                    isGroupOwner=isGroupOwner
                )
            }
        }.launchIn(viewModelScope)

        wifiDirectController.groupOwnerAddress.onEach { groupAddress->
            _state.update {
                it.copy(
                    groupAddress=groupAddress
                )
            }
        }.launchIn(viewModelScope)

        wifiDirectController.isConnected.onEach { isConnected ->
            _state.update {
                it.copy(
                    isConnected = isConnected
                )
            }
        }.launchIn(viewModelScope)
        wifiDirectController.isWifiEnabled.onEach { isEnabled->
            _state.update {
                it.copy(
                    isWifiEnabled = isEnabled
                )
            }
        }.launchIn(viewModelScope)
    }

    fun connectToDevice(device: WifiDirectDevice) {
        _state.update {
            it.copy(
                isConnecting = true
            )
        }
        deviceConnectionJob = wifiDirectController.connectToDevice(device).listen()
    }

    fun stopRecording(){
        wifiDirectController.stopRecording()
    }

    fun sendMessage(){
        viewModelScope.launch {
            wifiDirectController.sendMessage()
        }
    }

    fun disconnectFromDevice(){
            deviceConnectionJob?.cancel()
            wifiDirectController.closeConnection()
        _state.update {
            it.copy(
                isTransferStarted = false,
                isConnecting = false,
                isConnected = false,
                isGroupOwner = false
            )
        }
    }

    fun startServer(){
        _state.update {
            it.copy(
                isConnecting = true
            )
        }
        deviceConnectionJob = wifiDirectController.startServer().listen()
    }

    fun connectToTheServer(address: InetAddress?){
        _state.update {
            it.copy(
                isConnecting = true
            )
        }
        deviceConnectionJob = wifiDirectController.connectToServer(address).listen()
    }

    private fun Flow<ConnectionResult>.listen(): Job {
        return onEach { result ->
            when (result) {
                ConnectionResult.ConnectionEstablished -> {
                    _state.update {
                        it.copy(
                            isTransferStarted = false,
                            isConnected = true,
                            isConnecting = false,
                            errorMessage = null
                        )
                    }
                }

                is ConnectionResult.ReadyForDataTransfer -> {
                    _state.update {
                        it.copy(
                            isTransferStarted = true,
                            isConnected = false,
                            isConnecting = false,
                            errorMessage = null
                        )
                    }
                }

                is ConnectionResult.Error -> {
                    _state.update {
                        it.copy(
                            isTransferStarted = false,
                            isConnected = false,
                            isConnecting = false,
                            errorMessage = result.errorMsg
                        )
                    }
                }
            }
        }.catch { throwable ->
            Log.e("connection changed",throwable.message.toString())
            wifiDirectController.closeConnection()
            _state.update {
                it.copy(
                    isConnected = false,
                    isConnecting = false,
                )
            }
        }.launchIn(viewModelScope)
    }

}