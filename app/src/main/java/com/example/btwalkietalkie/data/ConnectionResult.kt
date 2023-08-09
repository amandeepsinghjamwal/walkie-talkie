package com.example.btwalkietalkie.data

sealed interface ConnectionResult{
    object ConnectionEstablished: ConnectionResult
    data class TransferSucceeded(val audioData:ByteArray):ConnectionResult
    data class Error(val msg:String):ConnectionResult
}