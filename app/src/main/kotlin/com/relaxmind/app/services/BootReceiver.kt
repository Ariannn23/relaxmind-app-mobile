package com.relaxmind.app.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.relaxmind.app.data.model.Appointment
import com.relaxmind.app.utils.ReminderManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.d("BootReceiver", "Device rebooted, rescheduling alarms...")
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val auth = FirebaseAuth.getInstance()
                    val user = auth.currentUser
                    if (user != null) {
                        val db = FirebaseFirestore.getInstance()
                        
                        // Reschedule Check-In Alarm
                        val patientDoc = db.collection("patients").document(user.uid).get().await()
                        if (patientDoc.exists()) {
                            val isEnabled = patientDoc.getBoolean("checkInReminderEnabled") ?: true
                            if (isEnabled) {
                                val timeStr = patientDoc.getString("checkInReminderTime") ?: "20:00"
                                ReminderManager.scheduleReminder(context, timeStr)
                            }
                        }

                        // Reschedule Appointments
                        val appointments = db.collection("appointments")
                            .whereEqualTo("patientId", user.uid)
                            .whereEqualTo("completed", false)
                            .get()
                            .await()

                        for (doc in appointments.documents) {
                            val appointment = doc.toObject(Appointment::class.java)?.copy(id = doc.id)
                            if (appointment != null) {
                                NotificationUtils.scheduleAppointmentReminder(context, appointment)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error rescheduling alarms", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
