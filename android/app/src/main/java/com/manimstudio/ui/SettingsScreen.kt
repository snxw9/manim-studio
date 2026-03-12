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
    var openaiKey by remember { mutableStateOf(ApiKeyManager.getOpenAIKey(context)) }
    
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
            // Tiered Info
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tiered Usage", fontWeight = FontWeight.Bold)
                    Text(
                        "• No Key: 10 free animations per day.\n" +
                        "• With Key: Unlimited animations.",
                        fontSize = 14.sp
                    )
                }
            }

            // AI Configuration
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "API Keys (Optional)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Groq
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Groq (Recommended)", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(
                            "Get free key →",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://console.groq.com/keys")))
                            }
                        )
                    }
                    OutlinedTextField(
                        value = groqKey,
                        onValueChange = { groqKey = it },
                        placeholder = { Text("gsk_...") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showGroqKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showGroqKey = !showGroqKey }) {
                                Icon(if (showGroqKey) Icons.Default.VisibilityOff else Icons.Default.Visibility, "")
                            }
                        }
                    )
                }

                // Gemini
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gemini", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(
                            "Get key →",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com")))
                            }
                        )
                    }
                    OutlinedTextField(
                        value = geminiKey,
                        onValueChange = { geminiKey = it },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation()
                    )
                }

                // OpenAI
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("OpenAI", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(
                            "Get key →",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://platform.openai.com/api-keys")))
                            }
                        )
                    }
                    OutlinedTextField(
                        value = openaiKey,
                        onValueChange = { openaiKey = it },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation()
                    )
                }

                Button(
                    onClick = {
                        ApiKeyManager.setGroqKey(context, groqKey)
                        ApiKeyManager.setGeminiKey(context, geminiKey)
                        ApiKeyManager.setOpenAIKey(context, openaiKey)
                        testStatus = "✅ Keys saved successfully!"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save All Keys")
                }

                if (testStatus != null) {
                    Text(
                        testStatus!!,
                        fontSize = 14.sp,
                        color = if (testStatus!!.contains("✅")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

// Extension function for clickable Text
@Composable
fun Modifier.clickable(onClick: () -> Unit): Modifier = this.then(
    androidx.compose.foundation.clickable(onClick = onClick)
)
