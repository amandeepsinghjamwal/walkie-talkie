package com.example.walkietalkie

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.example.walkietalkie.ui.theme.WalkieTalkieTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class MainActivity : ComponentActivity() {
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sampleRate = 44100 // Your desired sample rate
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val playChannelConfig = AudioFormat.CHANNEL_OUT_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        var audioTrack: AudioTrack? = null
        var audioRecord : AudioRecord? = null
        var audioByteArray:ByteArray? = null
        var audioData = ByteArrayOutputStream()
        setContent {
            WalkieTalkieTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var isRecording by remember {
                        mutableStateOf(false)
                    }
                    var isPlaying by remember {
                        mutableStateOf(false)
                    }
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(onClick = {
                            if (!isRecording) {
                                audioRecord = AudioRecord(
                                    MediaRecorder.AudioSource.MIC,
                                    sampleRate,
                                    channelConfig,
                                    audioFormat,
                                    bufferSize
                                )
                                audioRecord?.startRecording()
                                CoroutineScope(Dispatchers.IO).launch {
                                    val buffer = ByteArray(bufferSize)
                                    var readBytes: Int
                                    while (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                                        readBytes = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                                        audioData.write(buffer, 0, readBytes)
                                    }
                                }
                                isRecording = true
                            }
                            else {
                                try {
                                    audioRecord?.stop()
                                    audioRecord?.release()
                                    audioByteArray= audioData.toByteArray()
                                    audioData=ByteArrayOutputStream()
                                    Log.e("byteArrayData",audioByteArray.toString())
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                isRecording=false
                            }
                        }) {
                            Text(text =if(!isRecording) "Record" else "stop")
                        }

                        Button(onClick = {
                            isPlaying = if (!isPlaying){
                                val audioBufferSize = AudioTrack.getMinBufferSize(sampleRate, playChannelConfig, audioFormat)
                                audioTrack = AudioTrack.Builder()
                                    .setAudioAttributes(AudioAttributes.Builder()
                                        .setUsage(AudioAttributes.USAGE_MEDIA)
                                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                        .build())
                                    .setAudioFormat(AudioFormat.Builder()
                                        .setEncoding(audioFormat)
                                        .setSampleRate(sampleRate)
                                        .setChannelMask(playChannelConfig)
                                        .build())
                                    .setBufferSizeInBytes(audioBufferSize)
                                    .setTransferMode(AudioTrack.MODE_STREAM)
                                    .build()

                                audioTrack?.play()
                                CoroutineScope(Dispatchers.IO).launch {
                                    audioTrack?.write(audioByteArray!!,0,audioByteArray!!.size)
                                }
                                true
                            } else {
                                audioTrack?.stop()
                                audioTrack?.release()
                                false
                            }
                        }) {
                            Text(text = if(!isPlaying) "Play" else "stop")
                        }
                    }


                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WalkieTalkieTheme {
        Greeting("Android")
    }
}
