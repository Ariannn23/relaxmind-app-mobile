package com.relaxmind.app.data.remote

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.relaxmind.app.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val LUMI_SYSTEM_PROMPT = """
Eres Lumi, un asistente de bienestar emocional empático y cálido dentro de la app RelaxMind.
Tu rol es acompañar al paciente, escuchar sus emociones sin juzgar, sugerir técnicas simples
de relajación y respiración, y motivarlo a mantener sus hábitos saludables.
Nunca reemplazas a un profesional de salud mental. Si el paciente expresa pensamientos de 
autolesión o crisis grave, debes sugerirle contactar a su cuidador o a una línea de crisis.
Responde siempre en español, con un tono cálido, conciso y esperanzador.
Mantén las respuestas entre 2 y 4 párrafos cortos.
""".trimIndent()

class GeminiApiService {
    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig { 
            temperature = 0.8f
            maxOutputTokens = 512 
        }
    )

    fun sendMessage(
        history: List<Content>,
        userMessage: String
    ): Flow<String> {
        val chat = model.startChat(history = listOf(content("user") { text(LUMI_SYSTEM_PROMPT) }) + history)
        return chat.sendMessageStream(userMessage).map { it.text ?: "" }
    }
}
