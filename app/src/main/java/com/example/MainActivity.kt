package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.screens.MajarahAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MajarahViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MajarahViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.data.network.SupabaseConfig.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MajarahAppScreen(viewModel = viewModel)
            }
        }
    }
}
