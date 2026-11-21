package id.winimeku.wibinime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import id.winimeku.wibinime.network.Episode
import id.winimeku.wibinime.screens.DetailScreen
import id.winimeku.wibinime.screens.HomeScreen
import id.winimeku.wibinime.screens.LoginScreen
import id.winimeku.wibinime.screens.PlayerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                    
                    var isLoggedIn by remember { mutableStateOf(false) }
                    var currentScreen by remember { mutableStateOf("home") }
                    var selectedAnimeId by remember { mutableStateOf("") }
                    var selectedEpisode by remember { mutableStateOf<Episode?>(null) }

                    when {
                        !isLoggedIn -> {
                            LoginScreen(onLoginSuccess = { isLoggedIn = true })
                        }
                        currentScreen == "home" -> {
                            HomeScreen(onAnimeClick = { id ->
                                selectedAnimeId = id
                                currentScreen = "detail"
                            })
                        }
                        currentScreen == "detail" -> {
                            DetailScreen(
                                animeId = selectedAnimeId,
                                onBack = { currentScreen = "home" },
                                onPlayEpisode = { episode ->
                                    selectedEpisode = episode
                                    currentScreen = "player"
                                }
                            )
                        }
                        currentScreen == "player" -> {
                            if (selectedEpisode != null) {
                                PlayerScreen(
                                    episode = selectedEpisode!!,
                                    onBack = { currentScreen = "detail" }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
