package com.relaxmind.app.services

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.relaxmind.app.MainActivity
import com.relaxmind.app.R
import com.relaxmind.app.utils.ReminderManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class CheckInAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarm received for daily check-in")
        val pendingResult = goAsync()
        val reminderType = intent.getStringExtra(ReminderManager.EXTRA_REMINDER_TYPE)
            ?: ReminderManager.TYPE_DAILY_CHECK_IN

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

                    val isEnabled = patientDoc.getBoolean("checkInReminderEnabled") ?: true
                    if (!isEnabled) {
                        Log.d(TAG, "Check-in reminders disabled. Skipping.")
                        return@withTimeout
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
                    } else if (reminderType == ReminderManager.TYPE_STREAK_WARNING) {
                        val streakDoc = db.collection("streaks").document(user.uid).get().await()
                        val currentStreak = streakDoc.getLong("currentStreak") ?: 0L
                        if (currentStreak > 0L) {
                            showStreakWarningNotification(context, currentStreak)
                        } else {
                            Log.d(TAG, "No active streak. Skipping streak warning.")
                        }
                    } else {
                        // Patient hasn't checked in today. Show reminder!
                        showDailyReminderNotification(context)
                    }

                    // Reschedule for next day using the user's preferred time
                    val timeStr = patientDoc.getString("checkInReminderTime") ?: "20:00"
                    ReminderManager.scheduleReminder(context, timeStr)
                }

            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                Log.w(TAG, "Firestore query timed out (likely Doze mode). Showing fallback notification.")
                if (reminderType == ReminderManager.TYPE_STREAK_WARNING) {
                    showStreakWarningNotification(context, null)
                } else {
                    showDailyReminderNotification(context)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in CheckInAlarmReceiver", e)
                if (reminderType == ReminderManager.TYPE_STREAK_WARNING) {
                    showStreakWarningNotification(context, null)
                } else {
                    showDailyReminderNotification(context)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showDailyReminderNotification(context: Context) {
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

    private fun showStreakWarningNotification(context: Context, currentStreak: Long?) {
        NotificationUtils.createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("action", "open_checkin")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            "checkin_streak_warning".hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val streakText = currentStreak?.let { "Tu racha de $it dias esta en riesgo." }
            ?: "Tu racha esta en riesgo."
        val body = "$streakText Completa tu check-in antes de medianoche para mantenerla activa."
        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.racha_apagada)

        val builder = NotificationCompat.Builder(context, NotificationUtils.CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.racha_apagada)
            .setLargeIcon(largeIcon)
            .setContentTitle("Estas por perder tu racha")
            .setContentText("Completa tu check-in antes de medianoche.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify("checkin_streak_warning".hashCode(), builder.build())
            } catch (e: SecurityException) {
                Log.w(TAG, "Missing POST_NOTIFICATIONS permission", e)
            }
        }
    }

    companion object {
        private const val TAG = "CheckInAlarmReceiver"
    }
}
