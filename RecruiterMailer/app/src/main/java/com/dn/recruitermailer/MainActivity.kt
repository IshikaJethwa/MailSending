package com.dn.recruitermailer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dn.recruitermailer.data.Profile
import com.dn.recruitermailer.data.ProfileStore
import com.dn.recruitermailer.ui.SendScreen
import com.dn.recruitermailer.ui.SetupScreen
import com.dn.recruitermailer.ui.theme.MailerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val store = ProfileStore(applicationContext)
        setContent { MailerTheme { App(store) } }
    }
}

@Composable
private fun App(store: ProfileStore) {
    var profile by remember { mutableStateOf<Profile?>(store.load()) }
    var editing by remember { mutableStateOf(profile == null) }

    val current = profile
    if (editing || current == null) {
        SetupScreen(
            store = store,
            existing = current,
            onSaved = { profile = it; editing = false },
            onBack = if (current != null) ({ editing = false }) else null
        )
    } else {
        SendScreen(store = store, profile = current, onEdit = { editing = true })
    }
}
