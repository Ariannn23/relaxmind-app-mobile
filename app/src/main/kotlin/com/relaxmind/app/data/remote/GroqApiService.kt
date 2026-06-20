package com.relaxmind.app.data.remote

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.relaxmind.app.BuildConfig
import com.relaxmind.app.data.model.LumiMessage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit

val LUMI_SYSTEM_PROMPT = """
Eres Lumi, un asistente de bienestar emocional empático y cálido dentro de la app RelaxMind.
Tu rol es acompañar al paciente, escuchar sus emociones sin juzgar, sugerir técnicas simples
de relajación y respiración, y motivarlo a mantener sus hábitos saludables.
Nunca reemplazas a un profesional de salud mental. Si el paciente expresa pensamientos de 
autolesión o crisis grave, debes sugerirle contactar a su cuidador o a una línea de crisis.
Responde siempre en español, con un tono cálido, conciso y esperanzador.
Mantén las respuestas entre 2 y 4 párrafos cortos.
""".trimIndent()

class GroqApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val factory = EventSources.createFactory(client)

    fun sendMessageStream(
        history: List<LumiMessage>,
        userMessage: String
    ): Flow<String> = callbackFlow {
        
        val messagesArray = com.google.gson.JsonArray()
        
        val systemMsg = JsonObject()
        systemMsg.addProperty("role", "system")
        systemMsg.addProperty("content", LUMI_SYSTEM_PROMPT)
        messagesArray.add(systemMsg)

        history.forEach { msg ->
            val m = JsonObject()
            // Map "model" from Firestore/Gemini to "assistant" for Groq
            m.addProperty("role", if (msg.role == "model") "assistant" else msg.role)
            m.addProperty("content", msg.text)
            messagesArray.add(m)
        }

        val jsonBody = JsonObject()
        jsonBody.addProperty("model", "llama-3.3-70b-versatile")
        jsonBody.add("messages", messagesArray)
        jsonBody.addProperty("stream", true)
        jsonBody.addProperty("temperature", 0.8)
        jsonBody.addProperty("max_tokens", 512)

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        
        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .header("Authorization", "Bearer ${BuildConfig.GROQ_API_KEY}")
            .post(requestBody)
            .build()

        var isClosed = false

        val eventSourceListener = object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                if (data == "[DONE]") {
                    if (!isClosed) {
                        isClosed = true
                        close()
                    }
                    return
                }
                try {
                    val responseObj = gson.fromJson(data, JsonObject::class.java)
                    val choices = responseObj.getAsJsonArray("choices")
                    if (choices.size() > 0) {
                        val delta = choices.get(0).asJsonObject.getAsJsonObject("delta")
                        if (delta.has("content")) {
                            val chunk = delta.get("content").asString
                            trySend(chunk)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("GroqApiService", "Error parsing SSE chunk: " + e.message)
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                val errorBody = response?.body?.string()
                val errorMsg = response?.let { "HTTP ${it.code}: $errorBody" } ?: t?.message ?: "Unknown SSE failure"
                Log.e("GroqApiService", "SSE Connection failure: $errorMsg", t)
                if (!isClosed) {
                    isClosed = true
                    close(Exception(errorMsg, t))
                }
            }

            override fun onClosed(eventSource: EventSource) {
                if (!isClosed) {
                    isClosed = true
                    close()
                }
            }
        }

        val eventSource = factory.newEventSource(request, eventSourceListener)

        awaitClose {
            eventSource.cancel()
        }
    }
}
