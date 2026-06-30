package com.relaxmind.app.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.relaxmind.app.R
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.relaxmind.app.data.model.Appointment
import com.relaxmind.app.utils.AppointmentReminderWorker
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

object NotificationUtils {
    const val CHANNEL_SOS = "sos"
    const val CHANNEL_WELLNESS = "wellness_alerts"
    const val CHANNEL_REMINDERS = "reminders"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // SOS Channel
            val sosChannel = NotificationChannel(
                CHANNEL_SOS,
                "Alertas SOS",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Emergencias críticas de pacientes vinculados"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 900, 400, 900, 400, 900, 400, 900)
                enableLights(true)
                lightColor = Color.parseColor("#E8582A")
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setBypassDnd(true)
                }

                val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
                setSound(alarmSound, audioAttributes)
            }

            // Wellness Channel
            val wellnessChannel = NotificationChannel(
                CHANNEL_WELLNESS,
                "Alertas de Bienestar",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas de progreso y estado del paciente"
                enableVibration(true)
            }

            // Reminders Channel
            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Recordatorios",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Recordatorios de agenda y check-in"
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(
                listOf(sosChannel, wellnessChannel, remindersChannel)
            )
        }
    }

    fun buildSosNotification(
        context: Context,
        title: String,
        body: String,
        contentIntent: PendingIntent
    ): Notification {
        val alertTitle = title.ifBlank { "🆘 ALERTA SOS" }
        val alertBody = body.ifBlank { "Emergencia activa. Toca para responder." }
        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.sos)

        return NotificationCompat.Builder(context, CHANNEL_SOS)
            .setSmallIcon(R.drawable.sos)
            .setLargeIcon(largeIcon)
            .setContentTitle(alertTitle)
            .setContentText(alertBody)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(alertBody)
                    .setBigContentTitle(alertTitle)
                    .setSummaryText("Emergencia activa")
            )
            .setColor(context.getColor(R.color.notification_sos_accent))
            .setColorized(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentIntent)
            .setFullScreenIntent(contentIntent, true)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            .setDefaults(0)
            .build()
    }

    fun buildWellnessNotification(
        context: Context,
        title: String,
        body: String,
        contentIntent: PendingIntent
    ): Notification {
        val alertTitle = title.ifBlank { "Alerta de Bienestar" }
        val alertBody = body.ifBlank { "El bienestar de tu paciente ha bajado. Te sugerimos contactarlo." }
        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.checkin_bajo)

        return NotificationCompat.Builder(context, CHANNEL_WELLNESS)
            .setSmallIcon(R.drawable.checkin_bajo)
            .setLargeIcon(largeIcon)
            .setContentTitle(alertTitle)
            .setContentText(alertBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(alertBody).setBigContentTitle(alertTitle))
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()
    }

    fun buildCheckInCompletedNotification(
        context: Context,
        title: String,
        body: String,
        contentIntent: PendingIntent
    ): Notification {
        val alertTitle = title.ifBlank { "Check-in Completado" }
        val alertBody = body.ifBlank { "Tu paciente ha completado su check-in diario. Revisa su estado." }
        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.checkin_completado)

        return NotificationCompat.Builder(context, CHANNEL_WELLNESS)
            .setSmallIcon(R.drawable.checkin_completado)
            .setLargeIcon(largeIcon)
            .setContentTitle(alertTitle)
            .setContentText(alertBody)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()
    }

    fun scheduleAppointmentReminder(context: Context, appointment: Appointment) {
        try {
            val date = LocalDate.parse(appointment.date)
            val time = LocalTime.parse(appointment.time)
            val appointmentTime = LocalDateTime.of(date, time)
            val triggerTime = appointmentTime
                .minusMinutes(appointment.reminderTime.toLong())
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            val now = System.currentTimeMillis()
            if (triggerTime <= now) return

            val data = Data.Builder()
                .putString("appointmentId", appointment.id)
                .putString("title", appointment.title)
                .build()

            val request = OneTimeWorkRequestBuilder<AppointmentReminderWorker>()
                .setInitialDelay(triggerTime - now, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("appointment_${appointment.id}")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "appointment_${appointment.id}",
                ExistingWorkPolicy.REPLACE,
                request
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelAppointmentReminder(context: Context, appointmentId: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag("appointment_$appointmentId")
    }

    fun scheduleDailyCheckInReminder(context: Context) {
        val now = LocalDateTime.now()
        var next8PM = now.withHour(20).withMinute(0).withSecond(0).withNano(0)
        
        if (now.isAfter(next8PM) || now.isEqual(next8PM)) {
            next8PM = next8PM.plusDays(1)
        }

        val initialDelay = next8PM.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - System.currentTimeMillis()

        val request = PeriodicWorkRequestBuilder<CheckInReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("checkin_reminder")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "checkin_reminder",
            androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
