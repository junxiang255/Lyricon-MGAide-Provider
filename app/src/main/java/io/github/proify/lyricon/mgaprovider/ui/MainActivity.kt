package io.github.proify.lyricon.mgaprovider.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import io.github.proify.lyricon.mgaprovider.ModulePrefs

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        ModulePrefs.isEnabled(this)

        setContent {
            MgTheme {
                var page by remember { mutableStateOf(0) }
                var enabled by remember { mutableStateOf(ModulePrefs.isEnabled(this)) }

                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MgColors.Background),
                ) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.statusBars),
                    ) {
                        Box(Modifier.weight(1f)) {
                            if (page == 0) {
                                HomeScreen(enabled) { value ->
                                    enabled = value
                                    ModulePrefs.setEnabled(this@MainActivity, value)
                                }
                            } else {
                                AboutScreen { url -> openUrl(url) }
                            }
                        }
                    }

                    MgBottomBar(
                        page = page,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .windowInsetsPadding(WindowInsets.navigationBars),
                    ) { page = it }
                }
            }
        }
    }

    private fun openUrl(url: String) {
        runCatching {
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
