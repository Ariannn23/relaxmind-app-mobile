package com.relaxmind.app.services

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.relaxmind.app.MainActivity
import com.relaxmind.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class CheckInAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarm received for daily check-in")
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val auth = FirebaseAuth.getInstance()
                val user = auth.currentUser
                if (user == null) {
                    Log.d(TAG, "User not logged in. Skipping reminder.")
                    return@launch
                }

                // Use a strict 5-second timeout. In Doze mode, Firestore .await() will hang forever 
                // because network is restricted. If it hangs, we catch the timeout and show the notification anyway.
                kotlinx.coroutines.withTimeout(5000L) {
                    val db = FirebaseFirestore.getInstance()
                    
                    // Check if patient role (only remind patients)
                    val patientDoc = db.collection("patients").document(user.uid).get().await()
                    if (!patientDoc.exists()) {
                        Log.d(TAG, "Not a patient. Skipping reminder.")
                        return@withTimeout
                    }

                    // Check if check-in already done today
                    val todayStr = LocalDate.now().toString()
                    val checkIns = db.collection("diaryEntries")
                        .whereEqualTo("patientId", user.uid)
                        .whereEqualTo("date", todayStr)
                        .limit(1)
                        .get()
                        .await()

                    if (!checkIns.isEmpty) {
                        Log.d(TAG, "Check-in already done today. Skipping reminder.")
                    } else {
                        // Patient hasn't checked in today. Show reminder!
                        showNotification(context)
                    }

                    // Reschedule for next day using the user's preferred time
                    val timeStr = patientDoc.getString("checkInReminderTime") ?: "20:00"
                    val isEnabled = patientDoc.getBoolean("checkInReminderEnabled") ?: true
                    if (isEnabled) {
                        com.relaxmind.app.utils.ReminderManager.scheduleReminder(context, timeStr)
                    }
                }

            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                Log.w(TAG, "Firestore query timed out (likely Doze mode). Showing fallback notification.")
                showNotification(context)
            } catch (e: Exception) {
                Log.e(TAG, "Error in CheckInAlarmReceiver", e)
                showNotification(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context) {
        NotificationUtils.createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("action", "open_checkin")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            "checkin".hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val bitmap = android.graphics.BitmapFactory.decodeResource(context.resources, R.drawable.checkin_completado)

        val builder = NotificationCompat.Builder(context, NotificationUtils.CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.icono_plano2)
            .setContentTitle("📋 ¿Cómo te sientes hoy?")
            .setContentText("Tómate un momento para registrar tu estado de ánimo.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setLargeIcon(bitmap)
            .setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
                    .bigLargeIcon(null as android.graphics.Bitmap?)
            )

        with(NotificationManagerCompat.from(context)) {
            try {
                notify("checkin".hashCode(), builder.build())
            } catch (e: SecurityException) {
                Log.w(TAG, "Missing POST_NOTIFICATIONS permission", e)
            }
        }
    }

    companion object {
        private const val TAG = "CheckInAlarmReceiver"
    }
}
