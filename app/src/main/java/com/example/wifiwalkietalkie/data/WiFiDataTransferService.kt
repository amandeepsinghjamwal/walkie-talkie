package com.example.wifiwalkietalkie.data

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.Exception
import java.net.Socket

class WiFiDataTransferService(private val socket:Socket?) {


    private var recordingJob: Job? = null
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    var audioRecord : AudioRecord? = null

    private val playChannelConfig = AudioFormat.CHANNEL_OUT_MONO
    private val audioBufferSize = AudioTrack.getMinBufferSize(sampleRate, playChannelConfig, audioFormat)


    private val audioTrack = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build())
        .setAudioFormat(
            AudioFormat.Builder()
            .setEncoding(audioFormat)
            .setSampleRate(sampleRate)
            .setChannelMask(playChannelConfig)
            .build())
        .setBufferSizeInBytes(audioBufferSize)
        .setTransferMode(AudioTrack.MODE_STREAM)
        .build()

    suspend fun listenForIncomingMessage(){
        Log.e("connection changed","reached at listenner")
        if(!socket!!.isConnected){
            return
        }
        val buffer = ByteArray(400000)
        audioTrack.play()
        var isListening = true
        withContext(Dispatchers.IO) {
                while (isListening) {
                    try {
                        val byteCount = socket.inputStream?.read(buffer) ?: -1
                        if (byteCount > 0) {
                            audioTrack.write(buffer, 0, byteCount)
                        }
                    }catch (e:Exception){
                        e.printStackTrace()
                        Log.e("connection changed", e.message.toString())
                        isListening=false
                    }
                }
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun sendMessage(){
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )
        return withContext(Dispatchers.IO) {
            try {
                audioRecord?.startRecording()
                recordingJob=CoroutineScope(Dispatchers.IO).launch {
                    val buffer = ByteArray(1024 * 1024)
                    while (true) {
                        try {
                            val readBytes = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                            withContext(Dispatchers.IO) {
                                if (readBytes > 0) {
                                    socket?.outputStream?.write(buffer, 0, readBytes)
                                }
                            }
                        }catch (e: IOException){
                            Log.e("socket","Closed")
                        }
                    }

                }

            } catch (e: IOException) {
                return@withContext
            }
        }
    }

    fun stopRecording(){
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord=null
        recordingJob?.cancel()
    }
}