package com.example.kotlin

import AppNavigation
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.kotlin.ui.theme.KotlinTheme

class MainActivity : ComponentActivity() {
    private val TAG: String = this::class.simpleName ?: "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KotlinTheme {
                AppNavigation()
            }
        }
    }
}