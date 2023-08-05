package com.example.btwalkietalkie.data

sealed interface ConnectionResult{
    object ConnectoionEstablished: ConnectionResult
    data class Error(val msg:String):ConnectionResult
}