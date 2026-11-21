package com.vizion.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.json.JSONArray

private val Ink = Color(0xFF090A0F)
private val Panel = Color(0xFF171922)
private val Accent = Color(0xFFE50914)
private val Muted = Color(0xFF9CA3AF)
private val http = OkHttpClient()

data class MediaHit(
    val id: Int,
    val title: String,
    val overview: String,
    val poster: String?,
    val kind: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { VizionApp() }
    }
}

@Composable
private fun VizionApp() {
    var apiKey by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<MediaHit>>(emptyList()) }
    var selected by remember { mutableStateOf<MediaHit?>(null) }
    var streamUrl by remember { mutableStateOf("") }
    var playingUrl by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Recherche des films, séries et animes.") }

    MaterialTheme(colorScheme = darkColorScheme(
        background = Ink, surface = Panel, primary = Accent,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Surface(Modifier.fillMaxSize(), color = Ink) {
            Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("VIZ", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
                    Text("ION", color = Accent, fontSize = 30.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.weight(1f))
                    Text("BETA 0.1", color = Muted, fontSize = 11.sp)
                }

                if (playingUrl != null) {
                    VideoPlayer(url = playingUrl!!, onClose = { playingUrl = null })
                } else if (selected != null) {
                    val item = selected!!
                    Text("‹  RETOUR", color = Muted, modifier = Modifier.clickable { selected = null; streamUrl = "" }.padding(vertical = 8.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        AsyncImage(model = item.poster, contentDescription = item.title, modifier = Modifier.width(112.dp).height(164.dp))
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(item.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text(if (item.kind == "movie") "FILM" else "SÉRIE", color = Accent, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
                            Text(item.overview.ifBlank { "Aucun résumé disponible." }, color = Muted, fontSize = 12.sp, maxLines = 7, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                    Text("Lire une vidéo", fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 24.dp, bottom = 10.dp))
                    Text("Colle une URL vidéo à laquelle tu as accès (MP4, M3U8 ou DASH).", color = Muted, fontSize = 12.sp)
                    UrlInput(streamUrl, { streamUrl = it }, "https://…", Modifier.padding(top = 12.dp))
                    Button(
                        onClick = { if (streamUrl.startsWith("https://") || streamUrl.startsWith("http://")) playingUrl = streamUrl else message = "Entre une URL vidéo valide." },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent)
                    ) { Text("LANCER LA LECTURE") }
                    Text(message, color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp))
                } else {
                    Text("Tout votre univers.", fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    Text("Films • Séries • Anime", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp, bottom = 18.dp))
                    Text("CLÉ API TMDB", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    UrlInput(apiKey, { apiKey = it }, "Ta clé API TMDB", Modifier.padding(top = 6.dp))
                    Text("RECHERCHER", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
                    UrlInput(query, { query = it }, "Un film, une série, un anime…", Modifier.padding(top = 6.dp))
                    Button(
                        onClick = {
                            if (apiKey.isBlank() || query.isBlank()) message = "Ajoute ta clé TMDB et un titre."
                            else loading = true
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent)
                    ) { Text(if (loading) "RECHERCHE…" else "RECHERCHER") }
                    LaunchedEffect(loading) {
                        if (loading) {
                            val found = searchTmdb(apiKey.trim(), query.trim())
                            results = found
                            message = if (found.isEmpty()) "Aucun résultat. Vérifie la clé ou le titre." else "${found.size} résultat(s)"
                            loading = false
                        }
                    }
                    Text(message, color = Muted, fontSize = 12.sp, modifier = Modifier.padding(vertical = 12.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(results) { hit ->
                            Row(
                                Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(12.dp))
                                    .clickable { selected = hit }.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(model = hit.poster, contentDescription = hit.title, modifier = Modifier.width(64.dp).height(92.dp))
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(hit.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(if (hit.kind == "movie") "FILM" else "SÉRIE", color = Accent, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                                    Text(hit.overview, color = Muted, fontSize = 11.sp, maxLines = 3, modifier = Modifier.padding(top = 4.dp))
                                }
                                Text("›", color = Accent, fontSize = 24.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UrlInput(value: String, onValue: (String) -> Unit, hint: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().background(Panel, RoundedCornerShape(10.dp)).padding(14.dp)) {
        Box {
            if (value.isEmpty()) Text(hint, color = Muted, fontSize = 13.sp)
            BasicTextField(value, onValue, singleLine = true, textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 13.sp))
        }
    }
}

@Composable
private fun VideoPlayer(url: String, onClose: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val player = remember(url) {
        ExoPlayer.Builder(context).build().apply { setMediaItem(MediaItem.fromUri(url)); prepare(); playWhenReady = true }
    }
    DisposableEffect(player) { onDispose { player.release() } }
    Column {
        Text("‹  FERMER LE LECTEUR", color = Muted, modifier = Modifier.clickable(onClick = onClose).padding(vertical = 12.dp))
        AndroidView(factory = { ctx -> PlayerView(ctx).apply { this.player = player } }, modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f))
        Text("VIZION PLAYER", color = Accent, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
    }
}

private suspend fun searchTmdb(apiKey: String, query: String): List<MediaHit> = withContext(Dispatchers.IO) {
    val url = "https://api.themoviedb.org/3/search/multi?api_key=${java.net.URLEncoder.encode(apiKey, "UTF-8")}&query=${java.net.URLEncoder.encode(query, "UTF-8")}&include_adult=false"
    runCatching {
        http.newCall(Request.Builder().url(url).build()).execute().use { response ->
            if (!response.isSuccessful) return@use emptyList()
            val root = JSONObject(response.body?.string().orEmpty())
            val items = root.optJSONArray("results") ?: JSONArray()
            buildList {
                for (i in 0 until items.length()) {
                    val item = items.optJSONObject(i) ?: continue
                    val type = item.optString("media_type")
                    if (type != "movie" && type != "tv") continue
                    val id = item.optInt("id")
                    val title = item.optString(if (type == "movie") "title" else "name")
                    if (title.isBlank()) continue
                    val posterPath = item.optString("poster_path").takeIf { it.isNotBlank() && it != "null" }
                    add(MediaHit(id, title, item.optString("overview"), posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }, type))
                }
            }
        }
    }.getOrDefault(emptyList())
}
