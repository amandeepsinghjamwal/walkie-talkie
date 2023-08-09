package com.example.btwalkietalkie

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.btwalkietalkie.controller.BluetoothController
import com.example.btwalkietalkie.data.BluetoothUiState
import com.example.btwalkietalkie.data.BtDevices
import com.example.btwalkietalkie.data.ConnectionResult
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
import javax.inject.Inject

@HiltViewModel
class BtViewModel
    @Inject constructor(private val bluetoothController: BluetoothController) :
    ViewModel() {
    private val _state = MutableStateFlow(BluetoothUiState())
    private var deviceConnectionJob :Job? = null
    val state = combine(
        bluetoothController.pairedDevices,
        bluetoothController.scannedDevices,
        _state
    ){pairedDevices,scannedDevices,state->
        state.copy(
            pairedDevices=pairedDevices,
            scannedDevices = scannedDevices,
            msg = if (state.isConnected) state.msg else byteArrayOf()
        )

    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),_state.value)


    init {

        bluetoothController.isConnected.onEach {isConnected->
            _state.update { it.copy(isConnected = isConnected) }
        }.launchIn(viewModelScope)

        bluetoothController.error.onEach { error->
            _state.update {
                it.copy(
                    errorMessage = error
                )
            }
        }.launchIn(viewModelScope)
    }
    fun startScan(){
        bluetoothController.startDiscovery()
    }
    fun stopScan(){
        bluetoothController.stopDiscovery()
    }

    fun connectToDevice(devices: BtDevices){
        _state.update {
            it.copy(
                isConnecting = true
            )
        }
        deviceConnectionJob=bluetoothController.connectToDevice(devices)
            .listen()
    }


    fun disconnectFromDevice(){
        Log.e("hereee","cancelled")
        deviceConnectionJob?.cancel()
        bluetoothController.closeConnection()
        _state.update {
            it.copy(
                isConnecting =false,
                isConnected = false,
                errorMessage = "Connection interrupted"
            )
        }
    }


    fun waitForIncomingConnection(){
        _state.update {
            it.copy(isConnecting = true)
        }
        deviceConnectionJob= bluetoothController.startBtServer().listen()
    }
    private fun Flow<ConnectionResult>.listen(): Job {
        return onEach { result ->
            when(result) {
                ConnectionResult.ConnectionEstablished -> {
                    _state.update { it.copy(
                        isConnected = true,
                        isConnecting = false,
                        errorMessage = null
                    ) }
                }
                is ConnectionResult.TransferSucceeded->{
                    _state.update {
                        it.copy(
                            msg = result.audioData
                        )
                    }
                }
                is ConnectionResult.Error -> {
                    _state.update { it.copy(
                        isConnected = false,
                        isConnecting = false,
                        errorMessage = result.msg
                    ) }
                }
            }
        }
            .catch { throwable ->
                bluetoothController.closeConnection()
                _state.update { it.copy(
                    isConnected = false,
                    isConnecting = false,
                ) }
            }
            .launchIn(viewModelScope)
    }

    fun sendMessage(isRecording:Boolean){
        viewModelScope.launch {
            bluetoothController.trySendMessage(isRecording)
        }
    }

    fun stopRecording(){
        bluetoothController.stopRecording()
    }
    override fun onCleared() {
        super.onCleared()
        bluetoothController.release()
    }
}