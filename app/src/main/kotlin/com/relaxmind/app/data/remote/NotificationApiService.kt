package com.relaxmind.app.data.remote

import android.util.Log
import com.google.gson.JsonObject
import com.relaxmind.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class NotificationApiService(
    private val baseUrl: String = BuildConfig.NOTIFICATIONS_BASE_URL
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun sendSosAlert(
        patientId: String,
        caregiverId: String,
        alertId: String,
        patientName: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = JsonObject().apply {
                addProperty("patientId", patientId)
                addProperty("caregiverId", caregiverId)
                addProperty("alertId", alertId)
                addProperty("patientName", patientName)
            }

            val request = Request.Builder()
                .url("${baseUrl.trimEnd('/')}/api/send-sos-alert")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful || body.contains("\"error\"")) {
                    error("HTTP ${response.code}: $body")
                }
                Log.d(TAG, "SOS push sent successfully for alert $alertId")
            }
            Unit
        }
    }

    private companion object {
        const val TAG = "NotificationApiService"
    }
}
