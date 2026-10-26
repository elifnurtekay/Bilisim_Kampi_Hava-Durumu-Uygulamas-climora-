package com.kampplus.hava.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kampplus.hava.BuildConfig
import com.kampplus.hava.data.ChatMessage
import com.kampplus.hava.data.ChatRepository
import com.kampplus.hava.data.GeminiChatRepository
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class ChatState(
    val connected: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val busy: Boolean = false,
    val error: String? = null
)

class ChatViewModel(
    private val repository: ChatRepository = GeminiChatRepository(),
    private val developerKey: String = BuildConfig.GEMINI_API_KEY,
    private val developerModel: String = BuildConfig.GEMINI_MODEL
) : ViewModel() {
    private val mutableState = MutableStateFlow(ChatState(connected = developerKey.isNotBlank()))
    val state = mutableState.asStateFlow()
    private var apiKey = developerKey.trim()
    private var model = developerModel
    private var weatherContext = ""
    private var city = ""
    private var request: Job? = null

    fun updateContext(newCity: String, context: String) {
        val changed = city != newCity || weatherContext != context
        if (!changed) return
        clear()
        city = newCity
        weatherContext = context
        if (apiKey.isNotBlank()) greet()
    }

    fun clear() {
        request?.cancel()
        mutableState.value = ChatState(connected = apiKey.isNotBlank())
    }

    fun greet() =
        send("Bu konumun bugünkü hava verilerine göre şimdi ne giyebileceğimi kısaca öner. Şehir adını da belirt.", automatic = true)

    fun send(text: String, automatic: Boolean = false) {
        val input = text.trim().take(2000)
        val old = mutableState.value
        if (old.busy || input.isEmpty() || apiKey.isBlank() || weatherContext.isBlank()) return
        val history = (old.messages + ChatMessage("user", input)).takeLast(21)
        mutableState.value = old.copy(messages = if (automatic) old.messages else history, busy = true, error = null)
        request = viewModelScope.launch {
            try {
                val reply = repository.reply(apiKey, model, weatherContext, history)
                mutableState.value =
                    mutableState.value.copy(
                        messages = (if (automatic) old.messages else history) + ChatMessage("model", reply),
                        busy = false
                    )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                val message = when (error) {
                    is HttpException -> when (error.code()) {
                        400, 401, 403 -> "Asistan şu anda kullanılamıyor. Daha sonra yeniden dene."
                        404 -> "Asistanın bağlantısı şu anda kullanılamıyor."
                        429 -> "Kullanım sınırına ulaşıldı. Bir süre sonra yeniden dene."
                        else -> "Gemini şu anda yanıt veremiyor. Yeniden deneyebilirsin."
                    }
                    is IOException -> "Bağlantı kurulamadı veya zaman aşımına uğradı. Yeniden dene."
                    else -> "Yanıt alınamadı. Sorunu farklı şekilde yazmayı deneyebilirsin."
                }
                // Başarısız kullanıcı mesajını geçmişten çıkar; tekrar gönderim çift mesaj oluşturmaz.
                mutableState.value = mutableState.value.copy(messages = old.messages, busy = false, error = message)
            }
        }
    }
}
