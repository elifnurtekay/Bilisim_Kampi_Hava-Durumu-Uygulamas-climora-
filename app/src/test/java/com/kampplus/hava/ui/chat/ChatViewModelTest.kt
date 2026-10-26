package com.kampplus.hava.ui.chat

import com.kampplus.hava.data.ChatMessage
import com.kampplus.hava.data.ChatRepository
import com.kampplus.hava.testing.MainDispatcherRule
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private class FakeChat : ChatRepository {
        var calls = 0
        var history = emptyList<ChatMessage>()
        var contexts = mutableListOf<String>()
        var error: Exception? = null
        var gate: CompletableDeferred<Unit>? = null
        override suspend fun reply(key: String, model: String, context: String, messages: List<ChatMessage>): String {
            calls++
            history = messages
            contexts.add(context)
            gate?.await()
            error?.let { throw it }
            return "İnce bir ceket seçebilirsin."
        }
    }

    @Test fun `anahtar yokken ag istegi yapilmaz`() = runTest {
        val repo = FakeChat()
        val vm = ChatViewModel(repo, "", "test-model")
        vm.updateContext("Ankara", "15 derece")
        vm.send("Ne giyeyim?")
        advanceUntilIdle()
        assertEquals(0, repo.calls)
        assertFalse(vm.state.value.connected)
    }

    @Test fun `gelistirici anahtari ile ilk oneri otomatik ve tek sefer gelir`() = runTest {
        val repo = FakeChat()
        val vm = ChatViewModel(repo, "test-key", "test-model")
        vm.updateContext("Ankara", "15 derece")
        advanceUntilIdle()
        vm.updateContext("Ankara", "15 derece")
        advanceUntilIdle()
        assertEquals(1, repo.calls)
        assertEquals(listOf("model"), vm.state.value.messages.map { it.role })
        assertEquals("15 derece", repo.contexts.single())
    }

    @Test fun `otomatik oneri sonrasi sohbet gecmisi korunur`() = runTest {
        val repo = FakeChat()
        val vm = ChatViewModel(repo, "test-key", "test-model")
        vm.updateContext("Ankara", "15 derece")
        advanceUntilIdle()
        vm.send("Spor olsun")
        advanceUntilIdle()
        assertEquals(listOf("model", "user"), repo.history.map { it.role })
        assertEquals(3, vm.state.value.messages.size)
    }

    @Test fun `ilk oneri hatasi tekrar denenir gizli hata gosterilmez`() = runTest {
        val repo = FakeChat().apply { error = IOException("private-detail") }
        val vm = ChatViewModel(repo, "test-key", "test-model")
        vm.updateContext("Ankara", "15 derece")
        advanceUntilIdle()
        assertTrue(vm.state.value.messages.isEmpty())
        assertFalse(vm.state.value.error.orEmpty().contains("private-detail"))
        repo.error = null
        vm.greet()
        advanceUntilIdle()
        assertEquals(1, vm.state.value.messages.size)
    }

    @Test fun `sehir degisince eski istek iptal olur yeni oneri gelir`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repo = FakeChat().apply { this.gate = gate }
        val vm = ChatViewModel(repo, "test-key", "test-model")
        vm.updateContext("Ankara", "15 derece")
        advanceUntilIdle()
        vm.updateContext("İzmir", "25 derece")
        gate.complete(Unit)
        advanceUntilIdle()
        assertEquals(1, vm.state.value.messages.size)
        assertEquals("25 derece", repo.contexts.last())
        assertFalse(vm.state.value.busy)
    }
}
