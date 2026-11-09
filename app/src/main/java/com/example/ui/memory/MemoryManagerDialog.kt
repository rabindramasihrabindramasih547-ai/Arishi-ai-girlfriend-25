package com.example.ui.memory

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.MemoryItem
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
fun MemoryManagerDialog(
    memories: List<MemoryItem>,
    onAddMemory: (category: String, key: String, value: String) -> Unit,
    onDeleteMemory: (String) -> Unit,
    onToggleApproval: (String, Boolean) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showConfirmClear by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = ArishiSurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, ArishiCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("memory_manager_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
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
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = ArishiNeonGlow,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Arishi's Memory",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArishiTextPrimary
                        )
                        Text(
                            text = "${memories.count { it.isApproved }} active memories saved",
                            fontSize = 12.sp,
                            color = ArishiLavender
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ArishiTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ArishiPink),
                        modifier = Modifier.testTag("add_memory_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Teach Arishi", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    if (memories.isNotEmpty()) {
                        TextButton(
                            onClick = { showConfirmClear = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = ArishiPink)
                        ) {
                            Text("Clear All", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Memory list
                if (memories.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = null,
                                tint = ArishiLavender.copy(alpha = 0.4f),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Arishi has no saved memories yet.",
                                color = ArishiTextSecondary,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Chat with her or tap 'Teach Arishi' to add one!",
                                color = ArishiLavender,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        items(memories, key = { it.id }) { item ->
                            MemoryCard(
                                item = item,
                                onToggle = { onToggleApproval(item.id, !item.isApproved) },
                                onDelete = { onDeleteMemory(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMemoryDialog(
            onAdd = { cat, k, v ->
                onAddMemory(cat, k, v)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    if (showConfirmClear) {
        AlertDialog(
            onDismissRequest = { showConfirmClear = false },
            title = { Text("Clear all memories?", color = ArishiTextPrimary) },
            text = { Text("Arishi will forget all saved facts and personal preferences.", color = ArishiTextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAll()
                        showConfirmClear = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArishiPink)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClear = false }) {
                    Text("Cancel", color = ArishiTextSecondary)
                }
            },
            containerColor = ArishiSurfaceDark
        )
    }
}

@Composable
private fun MemoryCard(
    item: MemoryItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = ArishiSurfaceLightDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, ArishiCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.category.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArishiCyanAccent
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.key,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ArishiTextPrimary
                    )
                }
                Text(
                    text = item.value,
                    fontSize = 13.sp,
                    color = ArishiLavender,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Switch(
                checked = item.isApproved,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ArishiPink,
                    uncheckedThumbColor = ArishiTextSecondary,
                    uncheckedTrackColor = ArishiPurpleDark
                ),
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Memory",
                    tint = ArishiPink.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AddMemoryDialog(
    onAdd: (category: String, key: String, value: String) -> Unit,
    onDismiss: () -> Unit
) {
    var category by remember { mutableStateOf("preference") }
    var keyText by remember { mutableStateOf("") }
    var valueText by remember { mutableStateOf("") }

    val categories = listOf("preference", "fact", "activity", "date", "topic")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Teach Arishi a Memory", color = ArishiTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Category:", fontSize = 12.sp, color = ArishiLavender)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = cat == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ArishiPink else ArishiSurfaceLightDark)
                                .clickable { category = cat }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else ArishiTextSecondary
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = keyText,
                    onValueChange = { keyText = it },
                    label = { Text("Memory topic (e.g. Favorite Food)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ArishiTextPrimary,
                        unfocusedTextColor = ArishiTextPrimary,
                        focusedBorderColor = ArishiPink,
                        unfocusedBorderColor = ArishiCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = valueText,
                    onValueChange = { valueText = it },
                    label = { Text("What to remember (e.g. Butter Chicken)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ArishiTextPrimary,
                        unfocusedTextColor = ArishiTextPrimary,
                        focusedBorderColor = ArishiPink,
                        unfocusedBorderColor = ArishiCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (keyText.isNotBlank() && valueText.isNotBlank()) {
                        onAdd(category, keyText.trim(), valueText.trim())
                    }
                },
                enabled = keyText.isNotBlank() && valueText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ArishiPink)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ArishiTextSecondary)
            }
        },
        containerColor = ArishiSurfaceDark
    )
}
