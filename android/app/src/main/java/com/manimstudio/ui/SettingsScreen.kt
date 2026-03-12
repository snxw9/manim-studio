package com.manimstudio.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manimstudio.ai.ApiKeyManager
import com.manimstudio.ai.GroqClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var groqKey by remember { mutableStateOf(ApiKeyManager.getGroqKey(context)) }
    var geminiKey by remember { mutableStateOf(ApiKeyManager.getGeminiKey(context)) }
    var showGroqKey by remember { mutableStateOf(false) }
    var testStatus by remember { mutableStateOf<String?>(null) }
    var isTesting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // AI Configuration
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "AI Configuration",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    "Manim Studio uses Groq as the primary provider for lightning-fast animation generation. It's free and requires no credit card.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = groqKey,
                    onValueChange = { groqKey = it },
                    label = { Text("Groq API Key") },
                    placeholder = { Text("gsk_...") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showGroqKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showGroqKey = !showGroqKey }) {
                            Icon(
                                if (showGroqKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle visibility"
                            )
                        }
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://console.groq.com/keys"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors()
                    ) {
                        Text("Get Free Key")
                    }
                    
                    Button(
                        onClick = {
                            ApiKeyManager.setGroqKey(context, groqKey)
                            testStatus = "Key saved!"
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Key")
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            isTesting = true
                            testStatus = "Testing connection..."
                            val success = GroqClient.testKey(context)
                            testStatus = if (success) "✅ Connection successful!" else "❌ Test failed. Check your key."
                            isTesting = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isTesting && groqKey.isNotBlank(),
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    if (isTesting) CircularProgressIndicator(size = 20.dp)
                    else Text("Test Connection")
                }

                if (testStatus != null) {
                    Text(
                        testStatus!!,
                        fontSize = 14.sp,
                        color = if (testStatus!!.contains("✅")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Divider()

            // Optional Gemini Fallback
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Gemini Fallback (Optional)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = geminiKey,
                    onValueChange = { geminiKey = it },
                    label = { Text("Gemini API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )
                
                Button(
                    onClick = {
                        ApiKeyManager.setGeminiKey(context, geminiKey)
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save Gemini Key")
                }
            }
        }
    }
}
