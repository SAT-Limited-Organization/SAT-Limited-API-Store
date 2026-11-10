package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.catalog.SatApiCatalog
import com.example.ui.theme.SatCodeBg
import com.example.ui.theme.SatDarkBackground
import com.example.ui.theme.SatDarkSurface
import com.example.ui.theme.SatDarkSurfaceVariant
import com.example.ui.theme.SatPrimary
import com.example.ui.theme.SatSecondary
import com.example.ui.theme.SatSuccess
import com.example.ui.theme.SatTertiary
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaygroundScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val endpoint by viewModel.playgroundEndpoint.collectAsState()
    val method by viewModel.playgroundMethod.collectAsState()
    val body by viewModel.playgroundBody.collectAsState()
    val isExecuting by viewModel.isExecutingRequest.collectAsState()
    val result by viewModel.executionResult.collectAsState()
    val selectedStoreApi by viewModel.selectedStoreApi.collectAsState()
    val selectedCustomApi by viewModel.selectedCustomApi.collectAsState()
    val customApis by viewModel.customApis.collectAsState()
    val apiKeys by viewModel.apiKeys.collectAsState()

    var showExportDialog by remember { mutableStateOf(false) }
    var apiSelectorExpanded by remember { mutableStateOf(false) }

    val activeKey = apiKeys.firstOrNull { it.isActive }?.apiKeyHash ?: "sat_live_guest_demo_token"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SatDarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "API PLAYGROUND & TESTER",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SatSecondary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = selectedCustomApi?.name ?: selectedStoreApi?.title ?: "Select Endpoint",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Button(
                onClick = { showExportDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = SatDarkSurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    tint = SatSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export Code", fontSize = 12.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // API Switcher dropdown
        ExposedDropdownMenuBox(
            expanded = apiSelectorExpanded,
            onExpandedChange = { apiSelectorExpanded = !apiSelectorExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedCustomApi?.let { "Custom: ${it.name}" } ?: selectedStoreApi?.title ?: "Select API",
                onValueChange = {},
                readOnly = true,
                label = { Text("Active Target API") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = apiSelectorExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SatDarkSurface,
                    unfocusedContainerColor = SatDarkSurface,
                    focusedBorderColor = SatSecondary,
                    unfocusedBorderColor = SatDarkSurfaceVariant,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            ExposedDropdownMenu(
                expanded = apiSelectorExpanded,
                onDismissRequest = { apiSelectorExpanded = false },
                modifier = Modifier.background(SatDarkSurface)
            ) {
                Text(
                    text = "STORE APIS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SatSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
                SatApiCatalog.storeApis.forEach { storeApi ->
                    DropdownMenuItem(
                        text = { Text(storeApi.title, color = Color.White) },
                        onClick = {
                            viewModel.selectStoreApiForPlayground(storeApi)
                            apiSelectorExpanded = false
                        }
                    )
                }

                if (customApis.isNotEmpty()) {
                    Text(
                        text = "YOUR CUSTOM APIS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SatTertiary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                    customApis.forEach { cApi ->
                        DropdownMenuItem(
                            text = { Text(cApi.name, color = Color.White) },
                            onClick = {
                                viewModel.selectCustomApiForPlayground(cApi)
                                apiSelectorExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Endpoint and Method Bar
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SatDarkSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Method pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (method) {
                            "GET" -> Color(0xFF2E7D32)
                            "POST" -> Color(0xFF1565C0)
                            "PUT" -> Color(0xFFE65100)
                            "DELETE" -> Color(0xFFC62828)
                            else -> SatPrimary
                        }
                    ) {
                        Text(
                            text = method,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    OutlinedTextField(
                        value = endpoint,
                        onValueChange = { viewModel.updatePlaygroundParams(it, method, body) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("playground_endpoint_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SatCodeBg,
                            unfocusedContainerColor = SatCodeBg,
                            focusedBorderColor = SatSecondary,
                            unfocusedBorderColor = Color(0xFF2E2652),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Headers & auth bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Header: x-sat-api-key: ${activeKey.take(12)}...",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFAAA5CC)
                    )

                    Button(
                        onClick = { viewModel.executePlaygroundRequest() },
                        enabled = !isExecuting,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SatPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("run_api_button")
                    ) {
                        if (isExecuting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Running...", fontSize = 12.sp)
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Send Request", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Request Body editor (if POST / PUT or AI)
        if (method == "POST" || method == "PUT" || selectedStoreApi?.category == "ai" || selectedStoreApi?.category == "whatsapp") {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "REQUEST BODY (JSON)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9E99C2)
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = body,
                onValueChange = { viewModel.updatePlaygroundParams(endpoint, method, it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .testTag("playground_body_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SatCodeBg,
                    unfocusedContainerColor = SatCodeBg,
                    focusedBorderColor = SatSecondary,
                    unfocusedBorderColor = SatDarkSurfaceVariant,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Response section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RESPONSE INSPECTOR",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SatSecondary,
                letterSpacing = 1.sp
            )

            if (result != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (result!!.statusCode == 200) SatSuccess.copy(alpha = 0.2f) else Color(0x33FF5252)
                    ) {
                        Text(
                            text = "${result!!.statusCode} OK",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (result!!.statusCode == 200) SatSuccess else Color(0xFFFF5252),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Latency Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SatDarkSurfaceVariant
                    ) {
                        Text(
                            text = "${result!!.latencyMs} ms",
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = SatTertiary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SatCodeBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Payload (JSON)",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )

                    if (result?.responseBody?.isNotBlank() == true) {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("API Response", result!!.responseBody)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied response to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Response",
                                tint = SatSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val displayText = result?.responseBody ?: """// Click 'Send Request' to execute this API endpoint against SAT Limited store."""

                Text(
                    text = displayText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = if (result != null) Color(0xFF80D8FF) else Color.Gray,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Code Export Dialog (Node.js/Baileys, cURL, Python, Kotlin)
    if (showExportDialog) {
        ExportCodeDialog(
            endpoint = endpoint,
            method = method,
            body = body,
            apiKey = activeKey,
            onDismiss = { showExportDialog = false }
        )
    }
}

@Composable
fun ExportCodeDialog(
    endpoint: String,
    method: String,
    body: String,
    apiKey: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedLanguageIndex by remember { mutableIntStateOf(0) }
    val languages = listOf("Node.js (Baileys)", "cURL", "Python", "Kotlin")

    val baseUrl = "https://api.satlimited.org"
    val fullUrl = "$baseUrl$endpoint"

    val codeSnippets = remember(endpoint, method, body, apiKey) {
        listOf(
            // 0: Node.js with Baileys
            """
            // SAT Limited API Store WhatsApp Bot Client
            // Integrates with @whiskeysockets/baileys
            const { default: makeWASocket, useMultiFileAuthState } = require('@whiskeysockets/baileys');

            async function callSatApi() {
              const response = await fetch('$fullUrl', {
                method: '$method',
                headers: {
                  'Content-Type': 'application/json',
                  'x-sat-api-key': '$apiKey'
                },
                ${if (method == "POST" || method == "PUT") "body: JSON.stringify(${body.ifBlank { "{}" }})" else ""}
              });
              const data = await response.json();
              console.log('SAT Store Response:', data);
              return data;
            }
            """.trimIndent(),

            // 1: cURL
            """
            curl -X $method '$fullUrl' \
              -H 'Content-Type: application/json' \
              -H 'x-sat-api-key: $apiKey' \
              ${if (method == "POST" || method == "PUT") "-d '${body.replace("'", "\\'")}'" else ""}
            """.trimIndent(),

            // 2: Python requests
            """
            import requests

            url = "$fullUrl"
            headers = {
                "Content-Type": "application/json",
                "x-sat-api-key": "$apiKey"
            }
            ${if (method == "POST" || method == "PUT") "payload = $body\nresponse = requests.$method.lower()(url, headers=headers, json=payload)" else "response = requests.get(url, headers=headers)"}

            print(response.status_code)
            print(response.json())
            """.trimIndent(),

            // 3: Kotlin (OkHttp)
            """
            val client = OkHttpClient()
            ${if (method == "POST" || method == "PUT") "val mediaType = \"application/json\".toMediaType()\nval body = \"${body.replace("\"", "\\\"")}\".toRequestBody(mediaType)" else ""}
            val request = Request.Builder()
                .url("$fullUrl")
                .header("x-sat-api-key", "$apiKey")
                ${if (method == "POST" || method == "PUT") ".$method.lowercase()(body)" else ".get()"}
                .build()

            client.newCall(request).execute().use { response ->
                println(response.body?.string())
            }
            """.trimIndent()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Export Client Code", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                ScrollableTabRow(
                    selectedTabIndex = selectedLanguageIndex,
                    containerColor = SatDarkSurface,
                    contentColor = SatSecondary,
                    edgePadding = 0.dp
                ) {
                    languages.forEachIndexed { index, lang ->
                        Tab(
                            selected = selectedLanguageIndex == index,
                            onClick = { selectedLanguageIndex = index },
                            text = { Text(lang, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SatCodeBg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = codeSnippets[selectedLanguageIndex],
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = SatSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("SAT API Code", codeSnippets[selectedLanguageIndex])
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied snippet to clipboard", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SatPrimary)
            ) {
                Text("Copy Snippet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color.Gray)
            }
        },
        containerColor = SatDarkSurface,
        shape = RoundedCornerShape(18.dp)
    )
}
