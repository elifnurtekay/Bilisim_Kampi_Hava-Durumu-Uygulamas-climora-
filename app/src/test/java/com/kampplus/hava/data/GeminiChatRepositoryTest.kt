package com.kampplus.hava.data

import com.google.gson.JsonParser
import com.kampplus.hava.testing.createApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiChatRepositoryTest {
    @Test fun `anahtar headerda mesajlar sirali ve hava baglami sistem alaninda`() = runTest {
        val server = MockWebServer().apply { start() }
        try {
            server.enqueue(MockResponse().setBody("""{"candidates":[{"content":{"parts":[{"text":"Ceket al."}]}}]}"""))
            val api: GeminiApi = server.createApi()
            val reply = GeminiChatRepository(
                api
            ).reply("test-only", "test-model", "Ankara 15 derece", listOf(ChatMessage("user", "Ne giyeyim?")))
            assertEquals("Ceket al.", reply)
            val request = server.takeRequest()
            assertEquals("test-only", request.getHeader("x-goog-api-key"))
            assertFalse(request.path.orEmpty().contains("test-only"))
            val body = JsonParser.parseString(request.body.readUtf8()).asJsonObject
            assertTrue(body["systemInstruction"].toString().contains("Ankara 15 derece"))
            assertEquals("user", body["contents"].asJsonArray[0].asJsonObject["role"].asString)
        } finally {
            server.shutdown()
        }
    }

    @Test fun `bos veya engellenen yanit sahte cevap olarak gosterilmez`() = runTest {
        val server = MockWebServer().apply { start() }
        try {
            server.enqueue(MockResponse().setBody("""{"promptFeedback":{"blockReason":"SAFETY"}}"""))
            val api: GeminiApi = server.createApi()
            var failed = false
            try {
                GeminiChatRepository(api).reply("test-only", "test-model", "hava", listOf(ChatMessage("user", "soru")))
            } catch (
                _: IllegalStateException
            ) {
                failed = true
            }
            assertTrue(failed)
        } finally {
            server.shutdown()
        }
    }

    @Test fun `baska sehir araci gercek veriyi modele geri gonderir`() = runTest {
        val server = MockWebServer().apply { start() }
        try {
            server.enqueue(
                MockResponse().setBody(
                    """{"candidates":[{"content":{"role":"model","parts":[{"thoughtSignature":"preserve-me","functionCall":{"name":"get_city_weather","args":{"city":"Paris","country_code":"FR"}}}]}}]}"""
                )
            )
            server.enqueue(MockResponse().setBody("""{"candidates":[{"content":{"parts":[{"text":"Paris 12 derece; ceket al."}]}}]}"""))
            var queried = ""
            val lookup = object : CityWeatherTool {
                override suspend fun lookup(city: String, countryCode: String?, region: String?): String {
                    queried = "$city/$countryCode"
                    return "Paris gerçek tahmin: 12 derece"
                }
            }
            val reply = GeminiChatRepository(
                server.createApi(),
                lookup
            ).reply("test", "test-model", "Ankara", listOf(ChatMessage("user", "Paris'te ne giyeyim?")))
            assertEquals("Paris/FR", queried)
            assertTrue(reply.contains("Paris"))
            server.takeRequest()
            val body = JsonParser.parseString(server.takeRequest().body.readUtf8()).asJsonObject
            assertTrue(body["contents"].toString().contains("Paris gerçek tahmin"))
            assertTrue(body["contents"].toString().contains("preserve-me"))
        } finally {
            server.shutdown()
        }
    }
}
