const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const logger = require("firebase-functions/logger");
const admin = require("firebase-admin");

admin.initializeApp();

// 1. sendSOSAlert
exports.sendSOSAlert = onDocumentCreated("alerts/{alertId}", async (event) => {
    try {
        const snapshot = event.data;
        if (!snapshot) {
            logger.info("No data associated with the event.");
            return;
        }

        const alertData = snapshot.data();
        const { patientId, caregiverId, type } = alertData;

        if (type !== "sos" && type !== "low_score") {
            return;
        }

        // Obtener fcmToken del cuidador
        const caregiverRef = admin.firestore().collection("caregivers").doc(caregiverId);
        const caregiverSnap = await caregiverRef.get();
        if (!caregiverSnap.exists) {
            logger.error(`Cuidador ${caregiverId} no existe.`);
            return;
        }

        const caregiverData = caregiverSnap.data();
        const fcmToken = caregiverData.fcmToken;
        if (!fcmToken) {
            logger.error(`Cuidador ${caregiverId} no tiene fcmToken.`);
            return;
        }

        // Obtener nombre del paciente
        const patientRef = admin.firestore().collection("patients").doc(patientId);
        const patientSnap = await patientRef.get();
        const patientName = patientSnap.exists ? patientSnap.data().name : "Paciente";

        let message = null;

        if (type === "sos") {
            message = {
                token: fcmToken,
                notification: {
                    title: `🆘 ALERTA SOS — ${patientName}`,
                    body: `${patientName} necesita ayuda inmediata. Toca para ver su ubicación.`
                },
                data: {
                    alertId: event.params.alertId,
                    type: "sos",
                    screen: "SOSAlert"
                },
                android: {
                    priority: "high",
                    notification: {
                        channelId: "sos"
                    }
                }
            };
        } else if (type === "low_score") {
            const score = alertData.score || "bajo";
            message = {
                token: fcmToken,
                notification: {
                    title: `⚠️ Bienestar bajo — ${patientName}`,
                    body: `${patientName} registró bienestar muy bajo hoy (${score}/100). Revisa cómo está.`
                },
                data: {
                    alertId: event.params.alertId,
                    type: "low_score",
                    screen: "PatientDetail"
                },
                android: {
                    notification: {
                        channelId: "wellness_alerts"
                    }
                }
            };
        }

        if (message) {
            await admin.messaging().send(message);
            logger.info(`Notificación de alerta ${type} enviada al cuidador ${caregiverId}.`);
        }
    } catch (error) {
        logger.error("Error en sendSOSAlert:", error);
    }
});

// 2. checkDailyCheckIns
exports.checkDailyCheckIns = onSchedule({
    schedule: "59 23 * * *",
    timeZone: "America/Lima"
}, async (event) => {
    try {
        const db = admin.firestore();
        // Obtener todos los pacientes donde caregiverId !== null
        const patientsSnap = await db.collection("patients")
            .where("caregiverId", "!=", null)
            .get();

        const today = new Date().toISOString().split('T')[0];
        const now = Date.now();

        for (const doc of patientsSnap.docs) {
            const patient = doc.data();
            if (patient.isDeleted === true) continue;

            const patientId = doc.id;
            const caregiverId = patient.caregiverId;

            // Buscar si hizo checkIn en diario
            const checkInsSnap = await db.collection("diaryEntries")
                .where("patientId", "==", patientId)
                .where("date", "==", today)
                .limit(1)
                .get();

            if (checkInsSnap.empty) {
                // Verificar que no se haya generado ya una alerta hoy
                const streakRef = db.collection("streaks").doc(patientId);
                const streakSnap = await streakRef.get();
                
                let lastNoCheckinAlertDate = null;
                if (streakSnap.exists) {
                    lastNoCheckinAlertDate = streakSnap.data().lastNoCheckinAlertDate;
                }

                if (lastNoCheckinAlertDate !== today) {
                    // Crear alerta
                    await db.collection("alerts").add({
                        patientId: patientId,
                        caregiverId: caregiverId,
                        type: "no_checkin",
                        timestamp: now,
                        resolved: false
                    });

                    // Actualizar lastNoCheckinAlertDate
                    await streakRef.set({ lastNoCheckinAlertDate: today }, { merge: true });
                    
                    logger.info(`Alerta no_checkin generada para el paciente ${patientId}`);
                }
            }
        }
    } catch (error) {
        logger.error("Error en checkDailyCheckIns:", error);
    }
});

// 3. sendCheckInReminder
exports.sendCheckInReminder = onSchedule({
    schedule: "0 20 * * *",
    timeZone: "America/Lima"
}, async (event) => {
    try {
        const db = admin.firestore();
        // Obtener pacientes con checkInReminderEnabled = true
        const patientsSnap = await db.collection("patients")
            .where("checkInReminderEnabled", "==", true)
            .get();

        const today = new Date().toISOString().split('T')[0];

        for (const doc of patientsSnap.docs) {
            const patient = doc.data();
            if (patient.isDeleted === true) continue;
            
            const patientId = doc.id;
            const fcmToken = patient.fcmToken;

            if (!fcmToken) continue;

            // Verificar si ya hizo check-in hoy
            const checkInsSnap = await db.collection("diaryEntries")
                .where("patientId", "==", patientId)
                .where("date", "==", today)
                .limit(1)
                .get();

            if (checkInsSnap.empty) {
                const message = {
                    token: fcmToken,
                    notification: {
                        title: "📋 ¿Cómo estuvo tu día?",
                        body: "No olvides registrar tu check-in de hoy y mantener tu racha."
                    },
                    android: {
                        notification: {
                            channelId: "reminders"
                        }
                    }
                };

                await admin.messaging().send(message);
                logger.info(`Recordatorio de check-in enviado a ${patientId}`);
            }
        }
    } catch (error) {
        logger.error("Error en sendCheckInReminder:", error);
    }
});
