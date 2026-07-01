package com.relaxmind.app.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingWorkPolicy
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import android.app.PendingIntent
import android.content.Intent
import com.relaxmind.app.MainActivity
import com.relaxmind.app.services.NotificationUtils

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

        val appointmentDoc = runCatching {
            FirebaseFirestore.getInstance()
                .collection("appointments")
                .document(appointmentId)
                .get()
                .await()
        }.getOrNull()

        if (appointmentDoc?.getBoolean("completed") == true) {
            Log.d(TAG, "Skipping completed appointment reminder: $appointmentId")
            return Result.success()
        }

        showNotification(title, message, appointmentId)

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
            val recurringAppointmentDoc = appointmentDoc ?: db.collection("appointments")
                .document(appointmentId)
                .get()
                .await()
            
            val isRecurring = recurringAppointmentDoc.getBoolean("recurring") ?: false
            if (isRecurring && dayOfWeek != -1) {
                val time = recurringAppointmentDoc.getString("time") ?: "10:30"
                val reminderMinutes = recurringAppointmentDoc.getLong("reminderTime")?.toInt() ?: 15
                
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

    private fun showNotification(title: String, message: String, appointmentId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "POST_NOTIFICATIONS permission is missing. Reminder skipped for $appointmentId")
            return
        }

        NotificationUtils.createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("action", "open_appointment")
            putExtra("appointmentId", appointmentId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            appointmentId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val largeIcon = android.graphics.BitmapFactory.decodeResource(context.resources, com.relaxmind.app.R.drawable.recodatorio)
        val notification = NotificationCompat.Builder(context, NotificationUtils.CHANNEL_REMINDERS)
            .setSmallIcon(com.relaxmind.app.R.drawable.recodatorio)
            .setLargeIcon(largeIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(appointmentId.hashCode(), notification)
        Log.d(TAG, "Appointment reminder notification shown: $appointmentId")
    }

    private companion object {
        const val TAG = "AppointmentReminder"
    }
}
