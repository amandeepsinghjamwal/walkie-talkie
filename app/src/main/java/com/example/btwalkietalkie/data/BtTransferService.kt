package com.example.btwalkietalkie.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothSocket
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket


class BtTransferService(private val socket: BluetoothSocket) {
    private var recordingJob: Job? = null
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    var audioRecord: AudioRecord? = null

    var listenMessage = mutableStateOf(false)
    private val playChannelConfig = AudioFormat.CHANNEL_OUT_MONO
    private val audioBufferSize =
        AudioTrack.getMinBufferSize(sampleRate, playChannelConfig, audioFormat)


    private val audioTrack = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(audioFormat)
                .setSampleRate(sampleRate)
                .setChannelMask(playChannelConfig)
                .build()
        )
        .setBufferSizeInBytes(audioBufferSize)
        .setTransferMode(AudioTrack.MODE_STREAM)
        .build()

    fun listenForIncomingMessages(): Flow<ByteArray> {
        listenMessage.value = true
        val byteArrayOutputStream = ByteArrayOutputStream()
        var count: Int = -1
        return flow {
            if (!socket.isConnected) {
                return@flow
            }
            val buffer = ByteArray(1024 * 1024)
            audioTrack.play()
            while (listenMessage.value) {
                try {
                    val byteCount = socket.inputStream?.read(buffer) ?: -1
                    if (byteCount > 0) {
                        audioTrack.write(buffer, 0, byteCount)
                    }
                    count = byteCount
                    byteArrayOutputStream.write(buffer, 0, byteCount)
                } catch (e: Exception) {
                    Log.e("something", e.message.toString())
                    listenMessage.value = false
                }
                if (buffer.size ==
                    count
                ) {
                    emit(
                        byteArrayOutputStream.toByteArray()
                    )
                    byteArrayOutputStream.reset()
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    fun stopRecording() {
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        recordingJob?.cancel()
    }

    @SuppressLint("MissingPermission")
    suspend fun sendMessage(recording: Boolean = true) {
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
                recordingJob = CoroutineScope(Dispatchers.IO).launch {
                    val buffer = ByteArray(1024 * 1024)
                    while (true) {
                        try {
                            val readBytes = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                            withContext(Dispatchers.IO) {
                                if (readBytes > 0) {
                                    socket.outputStream.write(buffer, 0, readBytes)
                                }
                            }
                        } catch (e: IOException) {
                            Log.e("socket", "Closed")
                        }
                    }

                }

            } catch (e: IOException) {
                return@withContext
            }
        }
    }
}