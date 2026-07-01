package com.stremiox.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.stremiox.android.ui.StremioXApp

/// Phone / tablet entry point. Uses the five-tab bottom-nav [StremioXApp] shell.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { StremioXApp() }
    }
}
