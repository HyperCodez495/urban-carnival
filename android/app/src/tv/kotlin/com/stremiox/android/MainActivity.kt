package com.stremiox.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

/// Android TV entry point. Uses the D-pad-optimised [TvApp] shell with a side navigation rail
/// and focus-ring-enabled 10-foot poster cards, instead of the phone bottom-nav shell.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TvApp() }
    }
}
