package com.relaxmind.app.services

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.relaxmind.app.MainActivity
import com.relaxmind.app.R
import com.relaxmind.app.data.remote.FirebaseAuthService
import com.relaxmind.app.data.remote.FirestoreRepository
import java.time.LocalDate

class CheckInReminderWorker(
    private val ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val authService = FirebaseAuthService()
        val user = authService.getCurrentUser() ?: return Result.success()
        
        val firestoreRepository = FirestoreRepository()
        
        val roleResult = firestoreRepository.getRoleById(user.uid)
        if (roleResult.getOrNull() != "patient") {
            return Result.success() // Only for patients
        }

        // Verify if check-in was already done today
        val today = LocalDate.now().toString()
        val checkInResult = firestoreRepository.getTodayCheckIn(user.uid, today)
        
        if (checkInResult.isSuccess && checkInResult.getOrNull() == null) {
            // Patient hasn't checked in today. Show reminder!
            showNotification()
        }

        return Result.success()
    }

    private fun showNotification() {
        if (ActivityCompat.checkSelfPermission(
                ctx,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("action", "open_checkin")
        }

        val pendingIntent = PendingIntent.getActivity(
            ctx,
            "checkin".hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(ctx, NotificationUtils.CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.icono_plano2)
            .setContentTitle("¿Cómo te sientes hoy?")
            .setContentText("No olvides completar tu evaluación diaria (Check-in).")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(ctx)) {
            notify("checkin".hashCode(), builder.build())
        }
    }
}
