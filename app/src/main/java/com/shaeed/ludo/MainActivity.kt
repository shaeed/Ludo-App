package com.shaeed.ludo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.shaeed.ludo.audio.SoundManagerHolder
import com.shaeed.ludo.data.UserPreferences
import com.shaeed.ludo.model.TokenStyleHolder
import com.shaeed.ludo.navigation.LudoNavGraph
import com.shaeed.ludo.ui.theme.LudoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = UserPreferences(this)
        SoundManagerHolder.instance.setEnabled(prefs.soundEnabled)
        TokenStyleHolder.current = prefs.tokenStyle
        enableEdgeToEdge()
        setContent {
            LudoTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LudoNavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
