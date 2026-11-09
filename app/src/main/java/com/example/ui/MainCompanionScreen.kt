package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.VoiceState
import com.example.ui.avatar.ArishiAvatarView
import com.example.ui.chat.ChatHistorySheet
import com.example.ui.memory.MemoryManagerDialog
import com.example.ui.modes.ModeSelectorSheet
import com.example.ui.settings.SettingsDialog
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
fun MainCompanionScreen(
    viewModel: MainViewModel,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current

    val userProfile by viewModel.userProfile.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val audioAmplitude by viewModel.audioAmplitude.collectAsState()
    val avatarEmotion by viewModel.avatarEmotion.collectAsState()
    val avatarTheme by viewModel.avatarTheme.collectAsState()
    val isVoiceActive by viewModel.isVoiceActive.collectAsState()
    val isFullScreen by viewModel.isFullScreenAvatar.collectAsState()
    val memories by viewModel.memories.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val greeting by viewModel.suggestedGreeting.collectAsState()

    var showModeSheet by remember { mutableStateOf(false) }
    var showMemoryDialog by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    var isTextBarExpanded by remember { mutableStateOf(false) }

    // Microphone Permission Launcher
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleVoiceMode()
        }
    }

    fun handleMicClick() {
        val hasMic = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasMic) {
            viewModel.toggleVoiceMode()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Pulsing animation for microphone glow
    val infiniteTransition = rememberInfiniteTransition(label = "mic_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ArishiPurpleDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // --- 1. Center 3D Interactive Avatar Canvas ---
        ArishiAvatarView(
            voiceState = voiceState,
            audioAmplitude = audioAmplitude,
            emotion = avatarEmotion,
            theme = avatarTheme,
            modifier = Modifier.fillMaxSize()
        )

        // --- 2. Top Bar Navigation ---
        if (!isFullScreen) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Logo & Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showModeSheet = true }
                ) {
                    Text(
                        text = "ARISHI",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ArishiTextPrimary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = ArishiPink,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // Mode Badge Pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ArishiSurfaceDark.copy(alpha = 0.85f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArishiCardBorder),
                        modifier = Modifier.testTag("mode_badge_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = currentMode.emoji, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentMode.title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ArishiLavender
                            )
                        }
                    }
                }

                // Action Icons (Memory, History, Settings)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Memory Button
                    IconButton(
                        onClick = { showMemoryDialog = true },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("memory_button")
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Arishi Memory",
                                tint = ArishiNeonGlow,
                                modifier = Modifier.size(22.dp)
                            )
                            if (memories.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ArishiPink)
                                )
                            }
                        }
                    }

                    // Conversation History Button
                    IconButton(
                        onClick = { showHistorySheet = true },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("chat_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = "Conversation History",
                            tint = ArishiLavender,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Settings Button
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = ArishiTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Full Screen Toggle Button (Always visible on avatar edge)
        IconButton(
            onClick = { viewModel.toggleFullScreenAvatar() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = if (isFullScreen) 16.dp else 56.dp, end = 16.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(ArishiSurfaceDark.copy(alpha = 0.7f))
                .border(1.dp, ArishiCardBorder, CircleShape)
                .testTag("fullscreen_toggle_button")
        ) {
            Icon(
                imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                contentDescription = "Toggle Fullscreen",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        // --- 3. Subtitles / Speaking Banner Overlay ---
        val lastArishiMsg = messages.lastOrNull { it.sender == "arishi" }
        AnimatedVisibility(
            visible = (voiceState == VoiceState.SPEAKING || voiceState == VoiceState.THINKING) && lastArishiMsg != null && !isFullScreen,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 180.dp, start = 20.dp, end = 20.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = ArishiSurfaceDark.copy(alpha = 0.88f),
                border = androidx.compose.foundation.BorderStroke(1.dp, ArishiPink.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ArishiPink.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = ArishiPink,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (voiceState == VoiceState.THINKING) "Arishi is thinking..." else (lastArishiMsg?.text ?: ""),
                        color = ArishiTextPrimary,
                        fontSize = 13.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- 4. Bottom Controls Panel ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isFullScreen) {
                // Suggested Quick Conversation Prompts
                val prompts = listOf(
                    "Hey jaan! ❤️",
                    "Aaj tumhara din kaisa raha?",
                    "Tell me a sweet story ✨",
                    "I had a stressful day...",
                    "Tell me a cute joke 😊"
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    prompts.forEach { prompt ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ArishiSurfaceDark.copy(alpha = 0.85f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ArishiCardBorder),
                            modifier = Modifier
                                .clickable {
                                    viewModel.sendTextMessage(prompt)
                                }
                                .testTag("quick_prompt_${prompt.take(6)}")
                        ) {
                            Text(
                                text = prompt,
                                fontSize = 12.sp,
                                color = ArishiLavender,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Text Input Bar (Expandable)
            AnimatedVisibility(visible = isTextBarExpanded && !isFullScreen) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Message Arishi...", color = ArishiTextSecondary.copy(alpha = 0.6f), fontSize = 14.sp) },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ArishiTextPrimary,
                            unfocusedTextColor = ArishiTextPrimary,
                            focusedBorderColor = ArishiPink,
                            unfocusedBorderColor = ArishiCardBorder,
                            focusedContainerColor = ArishiSurfaceDark.copy(alpha = 0.9f),
                            unfocusedContainerColor = ArishiSurfaceDark.copy(alpha = 0.9f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_text_input")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                viewModel.sendTextMessage(textInput)
                                textInput = ""
                            }
                        },
                        enabled = textInput.isNotBlank(),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = ArishiPink,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("send_message_button")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Action Dock: [Text Toggle] --- [Large Glowing Mic] --- [Stop / Voice Mode]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Text Chat Keyboard Toggle
                IconButton(
                    onClick = { isTextBarExpanded = !isTextBarExpanded },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ArishiSurfaceDark.copy(alpha = 0.9f))
                        .border(1.dp, ArishiCardBorder, CircleShape)
                        .testTag("toggle_keyboard_button")
                ) {
                    Icon(
                        imageVector = if (isTextBarExpanded) Icons.Default.Close else Icons.Default.ChatBubble,
                        contentDescription = "Toggle Keyboard",
                        tint = ArishiLavender,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Central Large Glowing Microphone Button
                Box(contentAlignment = Alignment.Center) {
                    // Pulsing animated outer ring when listening or speaking
                    if (isVoiceActive || voiceState == VoiceState.LISTENING || voiceState == VoiceState.SPEAKING) {
                        Box(
                            modifier = Modifier
                                .size(78.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            (if (voiceState == VoiceState.SPEAKING) ArishiPink else ArishiCyanAccent).copy(alpha = 0.45f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }

                    // Main Mic Button
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = if (isVoiceActive) {
                                        listOf(ArishiPink, ArishiNeonGlow)
                                    } else {
                                        listOf(Color(0xFF581C87), Color(0xFF2E1065))
                                    }
                                )
                            )
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    listOf(ArishiPink, ArishiCyanAccent)
                                ),
                                shape = CircleShape
                            )
                            .clickable { handleMicClick() }
                            .testTag("main_mic_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isVoiceActive) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = "Microphone",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Stop / Interruption Button
                IconButton(
                    onClick = { viewModel.interruptSpeech() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ArishiSurfaceDark.copy(alpha = 0.9f))
                        .border(1.dp, ArishiCardBorder, CircleShape)
                        .testTag("interrupt_speech_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop Speech",
                        tint = if (voiceState == VoiceState.SPEAKING) ArishiPink else ArishiTextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Voice status subtitle label
            Text(
                text = when (voiceState) {
                    VoiceState.LISTENING -> "Listening to you... (Tap mic to pause)"
                    VoiceState.THINKING -> "Arishi is thinking..."
                    VoiceState.SPEAKING -> "Speaking ❤️ (Tap stop to interrupt)"
                    VoiceState.CONNECTING -> "Connecting to Arishi..."
                    else -> if (isVoiceActive) "Voice mode active" else "Tap microphone to talk to Arishi"
                },
                fontSize = 12.sp,
                color = when (voiceState) {
                    VoiceState.LISTENING -> ArishiCyanAccent
                    VoiceState.SPEAKING -> ArishiPink
                    else -> ArishiTextSecondary
                },
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    // Modal Sheets and Dialogs
    if (showModeSheet) {
        ModeSelectorSheet(
            currentMode = currentMode,
            onModeSelected = { mode -> viewModel.setMode(mode) },
            onDismiss = { showModeSheet = false }
        )
    }

    if (showMemoryDialog) {
        MemoryManagerDialog(
            memories = memories,
            onAddMemory = { cat, k, v -> viewModel.addMemory(cat, k, v) },
            onDeleteMemory = { id -> viewModel.deleteMemory(id) },
            onToggleApproval = { id, app -> viewModel.toggleMemoryApproval(id, app) },
            onClearAll = { viewModel.clearAllMemories() },
            onDismiss = { showMemoryDialog = false }
        )
    }

    if (showHistorySheet) {
        ChatHistorySheet(
            messages = messages,
            onDeleteMessage = { id -> viewModel.deleteChatMessage(id) },
            onClearConversation = { viewModel.clearConversation() },
            onDismiss = { showHistorySheet = false }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            currentSpeed = viewModel.voiceManager.speechRate,
            currentPitch = viewModel.voiceManager.voicePitch,
            currentVolume = viewModel.voiceManager.voiceVolume,
            currentLanguage = userProfile?.preferredLanguage ?: "auto",
            currentTheme = avatarTheme,
            userEmail = userProfile?.displayName,
            onSaveVoiceSettings = { spd, ptch, vol, lng ->
                viewModel.saveVoiceSettings(spd, ptch, vol, lng)
            },
            onTestVoice = { spd, ptch -> viewModel.testVoice(spd, ptch) },
            onChangeTheme = { tid -> viewModel.setTheme(tid) },
            onSignOut = onSignOut,
            onDismiss = { showSettingsDialog = false }
        )
    }
}
