package com.example.wifiwalkietalkie.data

sealed interface ConnectionResult {
    object ConnectionEstablished:ConnectionResult
    object ReadyForDataTransfer:ConnectionResult
    data class Error(val errorMsg:String):ConnectionResult
}