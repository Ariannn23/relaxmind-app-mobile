package com.relaxmind.app.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingWorkPolicy
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class AppointmentReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val appointmentId = inputData.getString("appointmentId") ?: return Result.failure()
        val title = inputData.getString("title") ?: "Recordatorio de evento"
        val type = inputData.getString("type") ?: "recordatorio"
        val dayOfWeek = inputData.getInt("dayOfWeek", -1)

        val message = when (type) {
            "cita" -> "Tienes una cita médica pronto: $title"
            "medicacion" -> "Es hora de tomar tu medicación: $title"
            else -> "Recordatorio: $title"
        }

        showNotification(title, message)

        // Mark as notificationSent in Firestore (only for non-recurring or general tracking)
        runCatching {
            FirebaseFirestore.getInstance()
                .collection("appointments")
                .document(appointmentId)
                .update("notificationSent", true)
                .await()
        }

        // Reschedule if recurring
        runCatching {
            val db = FirebaseFirestore.getInstance()
            val appointmentDoc = db.collection("appointments")
                .document(appointmentId)
                .get()
                .await()
            
            val isRecurring = appointmentDoc.getBoolean("recurring") ?: false
            if (isRecurring && dayOfWeek != -1) {
                val time = appointmentDoc.getString("time") ?: "10:30"
                val reminderMinutes = appointmentDoc.getLong("reminderTime")?.toInt() ?: 15
                
                // Calculate next week's delay
                val formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_TIME
                val localTime = java.time.LocalTime.parse(time, formatter)
                val targetDate = java.time.LocalDate.now().plusDays(7)
                val localDateTime = java.time.LocalDateTime.of(targetDate, localTime)
                val targetMs = localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                val reminderMs = targetMs - (reminderMinutes * 60 * 1000)
                val delayMs = reminderMs - System.currentTimeMillis()
                
                if (delayMs > 0) {
                    val data = workDataOf(
                        "appointmentId" to appointmentId,
                        "title" to title,
                        "type" to type,
                        "dayOfWeek" to dayOfWeek
                    )
                    
                    val request = OneTimeWorkRequestBuilder<AppointmentReminderWorker>()
                        .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                        .setInputData(data)
                        .addTag("appointment_$appointmentId")
                        .addTag("appointment_${appointmentId}_$dayOfWeek")
                        .build()
                    
                    WorkManager.getInstance(context).enqueueUniqueWork(
                        "appointment_${appointmentId}_$dayOfWeek",
                        ExistingWorkPolicy.REPLACE,
                        request
                    )
                }
            }
        }

        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "appointments_reminder_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
 
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios de Agenda",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones para citas, medicaciones y recordatorios."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // System standard alarm icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
