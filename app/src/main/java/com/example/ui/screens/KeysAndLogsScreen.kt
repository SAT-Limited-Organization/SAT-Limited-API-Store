package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApiKey
import com.example.data.model.ApiLog
import com.example.ui.theme.SatCodeBg
import com.example.ui.theme.SatDarkBackground
import com.example.ui.theme.SatDarkSurface
import com.example.ui.theme.SatDarkSurfaceVariant
import com.example.ui.theme.SatPrimary
import com.example.ui.theme.SatSecondary
import com.example.ui.theme.SatSuccess
import com.example.ui.theme.SatTertiary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun KeysAndLogsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val apiKeys by viewModel.apiKeys.collectAsState()
    val apiLogs by viewModel.apiLogs.collectAsState()

    var showNewKeyDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SatDarkBackground)
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quota & Tier Stats Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SatDarkSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SAT LIMITED CLOUD TIER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SatSecondary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${userProfile?.tier?.uppercase() ?: "DEVELOPER"} PLAN",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SatSuccess.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SatSuccess, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("DB Synced", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SatSuccess)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val used = apiLogs.size.toLong() + (userProfile?.totalRequests ?: 0L)
                    val limit = userProfile?.quotaLimit ?: 10000L
                    val progress = (used.toFloat() / limit.toFloat()).coerceIn(0f, 1f)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Requests: $used / $limit",
                            fontSize = 12.sp,
                            color = Color(0xFFC7C2E5)
                        )
                        Text(
                            text = "${(progress * 100).toInt()}% utilized",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SatTertiary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = SatSecondary,
                        trackColor = SatDarkSurfaceVariant
                    )
                }
            }
        }

        // API Keys Header & List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "API KEYS & ACCESS TOKENS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SatSecondary,
                    letterSpacing = 1.sp
                )

                Button(
                    onClick = { showNewKeyDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SatPrimary),
                    modifier = Modifier.testTag("generate_key_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Key", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (apiKeys.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SatDarkSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No custom API keys generated yet. Click 'New Key' to create a token for accessing Examination, Textbook, Movie Box, YouTube, or WhatsApp bot endpoints.",
                            fontSize = 12.sp,
                            color = Color(0xFFC7C2E5),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(apiKeys, key = { it.id }) { key ->
                ApiKeyCard(
                    apiKey = key,
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("API Key", key.apiKeyHash))
                        Toast.makeText(context, "API Key copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    onDelete = { viewModel.deleteApiKey(key.id) }
                )
            }
        }

        // Live Audit & Execution Logs Header
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LIVE AUDIT & TELEMETRY LOGS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SatSecondary,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "${apiLogs.size} events",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        if (apiLogs.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SatDarkSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No requests recorded yet. Execute queries in the Playground to see live telemetry.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(apiLogs, key = { it.id }) { log ->
                ApiLogCard(log = log)
            }
        }
    }

    if (showNewKeyDialog) {
        GenerateKeyDialog(
            onConfirm = { name ->
                viewModel.generateNewApiKey(name)
                showNewKeyDialog = false
            },
            onDismiss = { showNewKeyDialog = false }
        )
    }
}

@Composable
fun ApiKeyCard(
    apiKey: ApiKey,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SatDarkSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = apiKey.keyName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SatCodeBg
                ) {
                    Text(
                        text = apiKey.apiKeyMasked,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SatSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Key", tint = SatSecondary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Revoke Key", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun ApiLogCard(log: ApiLog) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SatDarkSurfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when (log.method) {
                        "GET" -> Color(0xFF2E7D32)
                        "POST" -> Color(0xFF1565C0)
                        else -> Color(0xFFC62828)
                    }
                ) {
                    Text(
                        text = log.method,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = log.apiName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = log.endpoint,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFB0ACCB)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (log.statusCode == 200L) SatSuccess.copy(alpha = 0.2f) else Color(0x33FF5252)
                ) {
                    Text(
                        text = "${log.statusCode}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (log.statusCode == 200L) SatSuccess else Color(0xFFFF5252),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${log.latencyMs}ms",
                    fontSize = 11.sp,
                    color = SatTertiary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun GenerateKeyDialog(
    onConfirm: (name: String) -> Unit,
    onDismiss: () -> Unit
) {
    var keyName by remember { mutableStateOf("Production Bot Key") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generate New API Key", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column {
                Text(
                    text = "Name your key for identification (e.g. 'Baileys WhatsApp Bot', 'Exam Papers Mobile Client'):",
                    fontSize = 12.sp,
                    color = Color(0xFFC7C2E5)
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = keyName,
                    onValueChange = { keyName = it },
                    label = { Text("Key Label") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (keyName.isNotBlank()) onConfirm(keyName) },
                colors = ButtonDefaults.buttonColors(containerColor = SatPrimary)
            ) {
                Text("Generate Token")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = SatDarkSurface,
        shape = RoundedCornerShape(18.dp)
    )
}
