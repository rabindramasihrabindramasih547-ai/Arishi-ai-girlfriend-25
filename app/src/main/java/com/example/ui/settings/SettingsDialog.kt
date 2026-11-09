package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.ArishiCardBorder
import com.example.ui.theme.ArishiCyanAccent
import com.example.ui.theme.ArishiLavender
import com.example.ui.theme.ArishiNeonGlow
import com.example.ui.theme.ArishiPink
import com.example.ui.theme.ArishiPurpleDark
import com.example.ui.theme.ArishiSurfaceDark
import com.example.ui.theme.ArishiSurfaceLightDark
import com.example.ui.theme.ArishiTextPrimary
import com.example.ui.theme.ArishiTextSecondary

@Composable
fun SettingsDialog(
    currentSpeed: Float,
    currentPitch: Float,
    currentVolume: Float,
    currentLanguage: String,
    currentTheme: String,
    userEmail: String?,
    onSaveVoiceSettings: (speed: Float, pitch: Float, volume: Float, language: String) -> Unit,
    onTestVoice: (speed: Float, pitch: Float) -> Unit,
    onChangeTheme: (String) -> Unit,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit
) {
    var speed by remember { mutableFloatStateOf(currentSpeed) }
    var pitch by remember { mutableFloatStateOf(currentPitch) }
    var volume by remember { mutableFloatStateOf(currentVolume) }
    var language by remember { mutableStateOf(currentLanguage) }
    var theme by remember { mutableStateOf(currentTheme) }

    val languages = listOf(
        "auto" to "Auto-Detect (Any Language)",
        "hi" to "Hindi (हिंदी)",
        "hinglish" to "Hinglish (Hindi-English)",
        "en" to "English (Indian/US)",
        "or" to "Odia (ଓଡ଼ିଆ)",
        "bn" to "Bengali (বাংলা)",
        "mr" to "Marathi (मराठी)",
        "gu" to "Gujarati (ગુજરાતી)",
        "ta" to "Tamil (தமிழ்)",
        "te" to "Telugu (తెలుగు)",
        "kn" to "Kannada (ಕನ್ನಡ)",
        "ml" to "Malayalam (മലയാളം)",
        "pa" to "Punjabi (ਪੰਜਾਬੀ)",
        "ur" to "Urdu (اردو)"
    )

    val themes = listOf(
        "default" to "Deep Romantic",
        "neon" to "Cyber Neon",
        "cozy" to "Cozy Sunset",
        "starry" to "Midnight Stars"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = ArishiSurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, ArishiCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ArishiPink.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = ArishiNeonGlow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Arishi Voice & Settings",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArishiTextPrimary
                        )
                        Text(
                            text = userEmail ?: "Signed in with Google",
                            fontSize = 12.sp,
                            color = ArishiLavender
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = ArishiTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Voice Pitch Slider
                Text(
                    text = "Cute & Soft Female Voice Pitch: ${String.format("%.2f", pitch)}x",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ArishiTextPrimary
                )
                Slider(
                    value = pitch,
                    onValueChange = { pitch = it },
                    valueRange = 0.8f..1.6f,
                    colors = SliderDefaults.colors(
                        thumbColor = ArishiPink,
                        activeTrackColor = ArishiPink,
                        inactiveTrackColor = ArishiPurpleDark
                    )
                )

                // Speech Rate Slider
                Text(
                    text = "Speech Speed: ${String.format("%.2f", speed)}x",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ArishiTextPrimary
                )
                Slider(
                    value = speed,
                    onValueChange = { speed = it },
                    valueRange = 0.7f..1.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = ArishiPink,
                        activeTrackColor = ArishiPink,
                        inactiveTrackColor = ArishiPurpleDark
                    )
                )

                // Output Volume Slider
                Text(
                    text = "Output Volume: ${(volume * 100).toInt()}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ArishiTextPrimary
                )
                Slider(
                    value = volume,
                    onValueChange = { volume = it },
                    valueRange = 0.0f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = ArishiCyanAccent,
                        activeTrackColor = ArishiCyanAccent,
                        inactiveTrackColor = ArishiPurpleDark
                    )
                )

                // Voice Preview & Reset Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onTestVoice(speed, pitch) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Voice Preview", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            pitch = 1.25f
                            speed = 1.0f
                            volume = 1.0f
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Language Selection
                Text(
                    text = "Conversation Language",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ArishiTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    languages.take(6).forEach { (code, name) ->
                        val isSelected = language == code
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) ArishiPink.copy(alpha = 0.2f) else ArishiSurfaceLightDark,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) ArishiPink else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { language = code }
                        ) {
                            Text(
                                text = name,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else ArishiTextSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Avatar Environment Theme
                Text(
                    text = "3D Avatar Environment",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ArishiTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    themes.forEach { (tid, tname) ->
                        val isSelected = theme == tid
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ArishiPink else ArishiSurfaceLightDark)
                                .clickable {
                                    theme = tid
                                    onChangeTheme(tid)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tname,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else ArishiTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // AI Safety & Identity Disclaimer
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ArishiPurpleDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArishiCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = ArishiCyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Arishi is a 22-year-old fictional Indian AI companion. She is an AI, not a real human.",
                            fontSize = 11.sp,
                            color = ArishiTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save & Sign Out buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onSaveVoiceSettings(speed, pitch, volume, language)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ArishiPink),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onSignOut,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ArishiPink)
                    ) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "Sign Out", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
