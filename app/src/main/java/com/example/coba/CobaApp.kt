package com.example.coba

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.example.coba.ui.navigation.AppNavGraph

@Composable
fun CobaApp() {
    Surface {
        AppNavGraph()
    }
}
