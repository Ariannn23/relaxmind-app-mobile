package com.relaxmind.app.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.relaxmind.app.services.CheckInAlarmReceiver
import java.util.Calendar

object ReminderManager {
    const val EXTRA_REMINDER_TYPE = "reminder_type"
    const val TYPE_DAILY_CHECK_IN = "daily_check_in"
    const val TYPE_STREAK_WARNING = "streak_warning"

    private const val DAILY_CHECK_IN_REQUEST_CODE = 1001
    private const val STREAK_WARNING_REQUEST_CODE = 1002
    
    fun scheduleReminder(context: Context, timeStr: String) {
        scheduleDailyCheckInReminder(context, timeStr)
        scheduleStreakWarningReminder(context)
    }

    private fun scheduleDailyCheckInReminder(context: Context, timeStr: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, CheckInAlarmReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_TYPE, TYPE_DAILY_CHECK_IN)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DAILY_CHECK_IN_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Parse time
        val parts = timeStr.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 20
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        // If time has already passed today, schedule for tomorrow
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }

        scheduleAlarm(alarmManager, target.timeInMillis, pendingIntent)
    }

    private fun scheduleStreakWarningReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, CheckInAlarmReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_TYPE, TYPE_STREAK_WARNING)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            STREAK_WARNING_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
        }

        if (target.before(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }

        scheduleAlarm(alarmManager, target.timeInMillis, pendingIntent)
    }

    private fun scheduleAlarm(alarmManager: AlarmManager, triggerAtMillis: Long, pendingIntent: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun cancelReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val dailyIntent = Intent(context, CheckInAlarmReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_TYPE, TYPE_DAILY_CHECK_IN)
        }
        
        val dailyPendingIntent = PendingIntent.getBroadcast(
            context,
            DAILY_CHECK_IN_REQUEST_CODE,
            dailyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val streakIntent = Intent(context, CheckInAlarmReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_TYPE, TYPE_STREAK_WARNING)
        }

        val streakPendingIntent = PendingIntent.getBroadcast(
            context,
            STREAK_WARNING_REQUEST_CODE,
            streakIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(dailyPendingIntent)
        alarmManager.cancel(streakPendingIntent)
    }
}
