package com.example.wifiwalkietalkie.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusEventModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.wifiwalkietalkie.R

@Composable
fun EnableWifiScreen(
    enableWifi: () -> Unit,
) {
    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.wifi_disabled),
            contentDescription = "",
            modifier = Modifier.fillMaxWidth(.7f).padding(bottom = 10.dp))
        Text(text = "Please enable Wi-Fi to continue", modifier = Modifier.padding(bottom = 20.dp))
        TextButton(onClick =  enableWifi ) {
            Text(text = "Enable Wi-Fi")
        }
    }
}