package com.relaxmind.app.services

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.relaxmind.app.MainActivity
import com.relaxmind.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AppointmentAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val appointmentId = intent.getStringExtra("appointmentId") ?: return
        val title = intent.getStringExtra("title") ?: "Recordatorio de evento"
        val type = intent.getStringExtra("type") ?: "recordatorio"
        val dayOfWeek = intent.getIntExtra("dayOfWeek", -1)

        Log.d(TAG, "Alarm received for appointment: $appointmentId")
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = FirebaseFirestore.getInstance()
                
                val appointmentDoc = db.collection("appointments").document(appointmentId).get().await()

                if (appointmentDoc.getBoolean("completed") == true) {
                    Log.d(TAG, "Skipping completed appointment reminder: $appointmentId")
                    return@launch
                }

                showNotification(context, title, type, appointmentId)

                // Mark as notificationSent
                db.collection("appointments").document(appointmentId).update("notificationSent", true).await()

                // Reschedule if recurring
                val isRecurring = appointmentDoc.getBoolean("recurring") ?: false
                if (isRecurring && dayOfWeek != -1) {
                    val time = appointmentDoc.getString("time") ?: "10:30"
                    val reminderMinutes = appointmentDoc.getLong("reminderTime")?.toInt() ?: 15
                    
                    // Logic to schedule for next week goes here, but it's handled when the user views the schedule too
                    // To keep it simple, we use the AlarmManager to schedule it again next week
                    scheduleNextWeek(context, appointmentId, title, type, dayOfWeek, time, reminderMinutes)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in AppointmentAlarmReceiver", e)
                // Fallback notification
                showNotification(context, title, type, appointmentId)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun scheduleNextWeek(
        context: Context,
        appointmentId: String,
        title: String,
        type: String,
        dayOfWeek: Int,
        time: String,
        reminderMinutes: Int
    ) {
        try {
            val formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_TIME
            val localTime = java.time.LocalTime.parse(time, formatter)
            val targetDate = java.time.LocalDate.now().plusDays(7)
            val localDateTime = java.time.LocalDateTime.of(targetDate, localTime)
            val targetMs = localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            val reminderMs = targetMs - (reminderMinutes * 60 * 1000)
            
            if (reminderMs > System.currentTimeMillis()) {
                val appointment = com.relaxmind.app.data.model.Appointment(
                    id = appointmentId,
                    title = title,
                    type = type,
                    recurringDays = listOf(dayOfWeek),
                    time = time,
                    reminderTime = reminderMinutes
                )
                NotificationUtils.scheduleAppointmentReminder(context, appointment)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling next week", e)
        }
    }

    private fun showNotification(context: Context, title: String, type: String, appointmentId: String) {
        NotificationUtils.createNotificationChannels(context)

        val message = when (type) {
            "cita" -> "Tienes una cita médica pronto: $title"
            "medicacion" -> "Es hora de tomar tu medicación: $title"
            else -> "Recordatorio: $title"
        }

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

        val largeIcon = android.graphics.BitmapFactory.decodeResource(context.resources, R.drawable.recodatorio)
        val builder = NotificationCompat.Builder(context, NotificationUtils.CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.recodatorio)
            .setLargeIcon(largeIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(appointmentId.hashCode(), builder.build())
            } catch (e: SecurityException) {
                Log.w(TAG, "Missing POST_NOTIFICATIONS permission", e)
            }
        }
    }

    companion object {
        private const val TAG = "AppointmentAlarm"
    }
}
