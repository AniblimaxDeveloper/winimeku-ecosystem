package id.winimeku.wibinime.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import id.winimeku.wibinime.network.Anime
import id.winimeku.wibinime.network.ApiClient
import id.winimeku.wibinime.network.Episode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(animeId: String, onPlayEpisode: (Episode) -> Unit, onBack: () -> Unit) {
    var anime by remember { mutableStateOf<Anime?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(animeId) {
        isLoading = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.instance.getAnimeDetail(animeId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        anime = response.body()
                    } else {
                        errorMsg = "Gagal memuat detail"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { errorMsg = "Koneksi Error: ${e.message}" }
            } finally {
                withContext(Dispatchers.Main) { isLoading = false }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(anime?.title ?: "Detail Anime", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", color = Color.White, fontSize = 24.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black, titleContentColor = Color.White)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
            } else if (errorMsg != null) {
                Text(errorMsg!!, color = Color.Red, modifier = Modifier.align(Alignment.Center))
            } else if (anime != null) {
                LazyColumn {
                    item {
                        AsyncImage(
                            model = anime!!.coverImage,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(250.dp)
                        )
                    }
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(anime!!.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Text("Status: ${anime!!.status} | Tahun: ${anime!!.releaseYear}", color = Color.Gray, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Sinopsis", color = Color.White, fontWeight = FontWeight.Bold)
                            Text(anime!!.synopsis ?: "-", color = Color.LightGray, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Daftar Episode", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                    items(anime!!.episodes ?: emptyList()) { episode ->
                        // Desain Kustom Pengganti ListTile (Bebas Error & Lebih Premium)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPlayEpisode(episode) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E1E1E))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.Red,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Episode ${episode.episodeNumber}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(episode.title ?: "Tanpa Judul", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
