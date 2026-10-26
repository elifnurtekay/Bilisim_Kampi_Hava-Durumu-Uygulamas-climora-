package com.kampplus.hava.ui.chat

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import com.kampplus.hava.appContainer
import com.kampplus.hava.data.weatherChatContext
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.clothingAdvice

private fun Context.activity(): ComponentActivity = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.activity()
    else -> error("Activity bulunamadı")
}

@Composable
fun WeatherChat(city: String, forecast: Forecast) {
    val container = LocalContext.current.appContainer
    val vm: ChatViewModel = viewModel(
        viewModelStoreOwner = LocalContext.current.activity(),
        factory = androidx.lifecycle.viewmodel.viewModelFactory { initializer { ChatViewModel(container.chat) } }
    )
    val state by vm.state.collectAsStateWithLifecycle()
    var draft by remember(city) { mutableStateOf("") }
    val listState = rememberLazyListState()
    LaunchedEffect(city, forecast) { vm.updateContext(city, weatherChatContext(city, forecast)) }
    LaunchedEffect(state.messages.size, state.busy) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem((state.messages.size - 1).coerceAtLeast(0))
        if (state.messages.lastOrNull()?.role == "model" && !state.busy && state.error == null) draft = ""
    }
    Column(Modifier.fillMaxWidth().fillMaxHeight(.92f).imePadding().padding(horizontal = 20.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Ne giysem?", style = MaterialTheme.typography.headlineSmall)
                Text("$city • Hava arkadaşın", style = MaterialTheme.typography.bodySmall)
            }
        }
        run {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.messages.isEmpty()) {
                    item {
                        Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "Yerel hava önerisi",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(forecast.clothingAdvice().joinToString("\n"), style = MaterialTheme.typography.bodyMedium)
                                if (!state.connected) {
                                    Text(
                                        "Canlı sohbet şu anda hazır değil. Hava verilerine göre hazırlanan önerin yukarıda.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                if (state.connected) {
                                    Text(
                                        "Nereye gideceğini ve tarzını yaz; Gemini hava durumuna göre yanıtlasın.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
                items(state.messages) { message ->
                    val user = message.role == "user"
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (user) Arrangement.End else Arrangement.Start) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (user) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.fillMaxWidth(.9f)
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text(
                                    if (user) "Sen" else "Gemini",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(message.text)
                            }
                        }
                    }
                }
                if (state.busy) item { Text("Hava arkadaşın düşünüyor…", style = MaterialTheme.typography.bodySmall) }
            }
        }
        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            if (state.messages.isEmpty()) TextButton(onClick = { vm.greet() }) { Text("İlk öneriyi yeniden dene") }
        }
        if (state.connected) Text("Sohbet ve hava özeti Google Gemini ile işlenir.", style = MaterialTheme.typography.labelSmall)
        if (state.connected) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("Akşam yürüyüşe çıkacağım", "Yarın okula ne giyeyim?", "Daha spor bir kombin öner")) { suggestion ->
                    SuggestionChip(onClick = { draft = suggestion }, label = { Text(suggestion) }, enabled = !state.busy)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(draft, {
                    draft = it.take(2000)
                }, placeholder = {
                    Text("Şehir, plan veya tarzını yaz…")
                }, modifier = Modifier.weight(1f), maxLines = 3, enabled = !state.busy)
                TextButton(onClick = { vm.send(draft) }, enabled = draft.isNotBlank() && !state.busy) { Text("Gönder") }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = {
                    vm.clear()
                    vm.greet()
                    draft = ""
                }) { Text("Sohbeti temizle") }
                Text("Gemini yanılabilir.", Modifier.padding(top = 16.dp), style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}
