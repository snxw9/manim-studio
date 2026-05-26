package com.manimstudio.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.manimstudio.app.engine.RenderResult
import com.manimstudio.app.engine.SetupViewModel
import com.manimstudio.app.ui.setup.SetupScreen

class MainActivity : ComponentActivity() {

    private val viewModel: SetupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val state by viewModel.state.collectAsState()
                    
                    // State to hold our popup data
                    var renderResultDialog by remember { mutableStateOf<RenderResult?>(null) }

                    SetupScreen(
                        state = state,
                        onStartSetup = { viewModel.startInstallation() },
                        onRetry = { viewModel.retrySetup() },
                        onTestRender = {
                            viewModel.testRender { result ->
                                runOnUiThread {
                                    // Trigger the popup instead of a toast
                                    renderResultDialog = result
                                }
                            }
                        },
                        onOpenSettings = { 
                            // You can wire up your settings navigation here later.
                            // For now, this empty block just satisfies the compiler!
                        }
                    )

                    // The actual popup dialog
                    renderResultDialog?.let { result ->
                        AlertDialog(
                            onDismissRequest = { renderResultDialog = null },
                            title = { Text(if (result.success) "Render Successful!" else "Render Failed") },
                            text = {
                                Text(
                                    text = if (result.success) {
                                        "Time: ${result.renderTimeMs}ms\nSaved to:\n${result.videoFile?.absolutePath}"
                                    } else {
                                        result.errorMessage ?: "Unknown error"
                                    },
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    // Makes the text scrollable if the error is huge
                                    modifier = Modifier.verticalScroll(rememberScrollState())
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = { renderResultDialog = null }) {
                                    Text("Close", fontFamily = FontFamily.Monospace)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
