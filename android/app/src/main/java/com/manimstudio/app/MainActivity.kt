package com.manimstudio.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ManimStudioApp()
        }
    }
}

@Composable
fun ManimStudioApp() {
    // Navigation and app structure will go here
    Text("Manim Studio — Android app coming soon")
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    ManimStudioApp()
}
