package com.manimstudio.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.manimstudio.app.engine.SetupState
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

                    SetupScreen(
                        state = state,
                        onStartSetup = { viewModel.startInstallation() },
                        onRetry = { viewModel.retrySetup() },
                        onTestRender = {
                            viewModel.testRender { result ->
                                runOnUiThread {
                                    if (result.success) {
                                        Toast.makeText(
                                            this,
                                            "Render successful! ${result.renderTimeMs}ms\n${result.videoFile?.absolutePath}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            this,
                                            "Render failed: ${result.errorMessage?.take(100)}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}
