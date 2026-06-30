package com.relaxmind.app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.relaxmind.app.MainActivity
import com.relaxmind.app.R
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class DailyReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user == null) {
            Log.d(TAG, "User not logged in. Skipping reminder.")
            return Result.success()
        }

        try {
            val db = FirebaseFirestore.getInstance()
            
            // Check if patient role (only remind patients)
            val patientDoc = db.collection("patients").document(user.uid).get().await()
            if (!patientDoc.exists()) {
                Log.d(TAG, "Not a patient. Skipping reminder.")
                return Result.success()
            }

            // Check if check-in already done today
            val todayStr = LocalDate.now().toString()
            val checkIns = db.collection("checkIns")
                .whereEqualTo("patientId", user.uid)
                .whereEqualTo("date", todayStr)
                .limit(1)
                .get()
                .await()

            if (!checkIns.isEmpty) {
                Log.d(TAG, "Check-in already done today. Skipping reminder.")
                // Reschedule for next day
                reschedule(user.uid, db)
                return Result.success()
            }

            // Send local notification
            sendNotification()
            
            // Reschedule for next day
            reschedule(user.uid, db)
            
            return Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "Error in DailyReminderWorker", e)
            return Result.retry()
        }
    }

    private suspend fun reschedule(userId: String, db: FirebaseFirestore) {
        try {
            val patientDoc = db.collection("patients").document(userId).get().await()
            if (patientDoc.exists()) {
                val timeStr = patientDoc.getString("checkInReminderTime") ?: "20:00"
                val isEnabled = patientDoc.getBoolean("checkInReminderEnabled") ?: true
                if (isEnabled) {
                    com.relaxmind.app.utils.ReminderManager.scheduleReminder(context, timeStr)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error rescheduling", e)
        }
    }

    private fun sendNotification() {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "patient_reminders_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios de Check-in",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para recordatorios de check-in diario"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("action", "open_checkin")
        }
        
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("¡Es hora de tu check-in!")
            .setContentText("Tómate un momento para registrar cómo te sientes hoy.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(1001, builder.build())
    }

    companion object {
        private const val TAG = "DailyReminderWorker"
    }
}
