package com.example.btwalkietalkie.screens

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.btwalkietalkie.R
import kotlinx.coroutines.delay
import kotlin.random.Random

@OptIn(ExperimentalAnimationApi::class)
@SuppressLint("MissingPermission")
@Composable
fun ConnectionScreen(
    onStopRecording: () -> Unit,
    onDisconnect: () -> Unit,
    onSendMessage: (Boolean) -> Unit
) {
    var isRecording by remember {
        mutableStateOf(false)
    }
    var isVisible by remember {
        mutableStateOf(false)
    }
    var isSecondVisible by remember {
        mutableStateOf(false)
    }

    var playAnimation by remember {
        mutableStateOf(false)
    }

    var isClickedVisibility by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = playAnimation) {
        while (playAnimation) {
            isVisible = !isVisible
            delay(1100)
        }
        if (!playAnimation) {
            isVisible = false
        }
    }
    LaunchedEffect(key1 = playAnimation) {
        while (playAnimation) {
            delay(100)
            isSecondVisible = !isSecondVisible
            delay(1000)
        }
        if (!playAnimation) {
            isSecondVisible = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.End
    ) {
        IconButton(onClick = { onDisconnect() }) {
            Icon(
                painter = painterResource(id = R.drawable.exit),
                contentDescription = "",
                modifier = Modifier
                    .size(50.dp)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            IconButton(
                onClick = {
                    if (!isRecording) {
                        onSendMessage(isRecording)
                        isClickedVisibility = !isClickedVisibility
                        playAnimation = !playAnimation
                        isRecording = !isRecording
                    } else {
                        isRecording = !isRecording
                        isClickedVisibility = !isClickedVisibility
                        playAnimation = !playAnimation
                        onStopRecording()
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.mic),
                        contentDescription = "",
                        tint = if (isSystemInDarkTheme()) Color.White else Color.Black,
                        modifier = Modifier.size(100.dp)
                    )
                    Column() {
                        AnimatedVisibility(
                            visible = isVisible,
                            enter = scaleIn(
                                tween(1000)
                            ),
                            exit = fadeOut(
                                tween(0)
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(
                                        shape = CircleShape
                                    )
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                    .size(200.dp)
                            )
                        }
                    }

                    Column() {
                        AnimatedVisibility(
                            visible = isSecondVisible,
                            enter = scaleIn(
                                tween(1000)
                            ),
                            exit = fadeOut(
                                tween(0)
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(
                                        shape = CircleShape
                                    )
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                    .size(200.dp)
                            )
                        }
                    }
                    Column() {
                        AnimatedVisibility(
                            visible = isClickedVisibility,
                            enter = scaleIn(
                                tween(1000)
                            ),
                            exit = scaleOut(
                                tween(1000)
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(
                                        shape = CircleShape
                                    )
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                    .size(150.dp)
                            )
                        }
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 30.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "You are now connected \nClick on the mic to start talking",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
        }

    }
}
