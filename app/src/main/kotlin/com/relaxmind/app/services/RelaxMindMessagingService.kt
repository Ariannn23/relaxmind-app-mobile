package com.relaxmind.app.services

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.relaxmind.app.MainActivity
import com.relaxmind.app.R
import com.relaxmind.app.data.remote.FirebaseAuthService
import com.relaxmind.app.data.remote.FirestoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RelaxMindMessagingService : FirebaseMessagingService() {

    private val firestoreRepository = FirestoreRepository()
    private val authService = FirebaseAuthService()
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val user = authService.getCurrentUser() ?: return

        scope.launch {
            val roleResult = firestoreRepository.getRoleById(user.uid)
            val role = roleResult.getOrNull() ?: return@launch
            firestoreRepository.updateFcmToken(user.uid, role, token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data
        val type = data["type"] ?: return
        val alertId = data["alertId"]
        val title = data["title"] ?: message.notification?.title ?: "Nueva notificación"
        val body = data["body"] ?: message.notification?.body ?: ""

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        when (type) {
            "sos" -> {
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("action", "open_sos")
                    putExtra("alertId", alertId)
                }

                val pendingIntent = PendingIntent.getActivity(
                    this,
                    (alertId ?: "sos").hashCode(),
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                val notification = NotificationUtils.buildSosNotification(
                    context = this,
                    title = title,
                    body = body,
                    contentIntent = pendingIntent
                )

                with(NotificationManagerCompat.from(this)) {
                    notify((alertId ?: "sos").hashCode(), notification)
                }
            }
            "low_score", "wellness" -> {
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("action", "open_patient_detail")
                    putExtra("patientId", data["patientId"])
                }

                val pendingIntent = PendingIntent.getActivity(
                    this,
                    title.hashCode(),
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                val builder = NotificationCompat.Builder(this, NotificationUtils.CHANNEL_WELLNESS)
                    .setSmallIcon(R.drawable.icono_plano2)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)

                with(NotificationManagerCompat.from(this)) {
                    notify(title.hashCode(), builder.build())
                }
            }
            "reminder" -> {
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("action", "open_checkin")
                }

                val pendingIntent = PendingIntent.getActivity(
                    this,
                    "reminder".hashCode(),
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                val builder = NotificationCompat.Builder(this, NotificationUtils.CHANNEL_REMINDERS)
                    .setSmallIcon(R.drawable.icono_plano2)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)

                with(NotificationManagerCompat.from(this)) {
                    notify("reminder".hashCode(), builder.build())
                }
            }
        }
    }
}
