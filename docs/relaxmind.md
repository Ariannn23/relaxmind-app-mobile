# Documentación Técnica Completa — RelaxMind
> Versión actualizada · Grupo 4, 2026 · Curso: Desarrollo de Aplicaciones Móviles · Prof. Blancas Núñez, Mitchell Paula

---

## 1. Introducción

RelaxMind es una aplicación móvil enfocada en la salud mental y el bienestar emocional. Cuenta con dos roles principales: **paciente** y **cuidador**. La app integra un sistema de check-in diario con puntuación de bienestar, ejercicios de meditación y respiración, agenda personal, un asistente de IA llamado **Lumi**, vinculación entre paciente y cuidador, y un sistema de alertas SOS en tiempo real.

---

## 2. Tecnologías y Versiones

| Tecnología | Versión / Detalle |
|---|---|
| **Lenguaje** | Kotlin |
| **UI Framework** | Jetpack Compose (estable, 1.6+) |
| **Versión mínima de Android** | API 28 (Android 9.0) |
| **Versión objetivo** | API 34 (Android 14) |
| **Base de datos en la nube** | Firebase Firestore (NoSQL, tiempo real) |
| **Autenticación** | Firebase Authentication |
| **Notificaciones push** | Firebase Cloud Messaging (FCM) |
| **Mapas y ubicación** | Google Maps API + Location Services |
| **IA conversacional (Lumi)** | Gemini API (Google AI SDK para Android) |
| **Animaciones** | Lottie + Compose Animations |
| **Fuentes** | Outfit (títulos), Urbanist (textos), Montserrat (botones) |
| **Modo oscuro** | Configurable en ajustes, disponible tras iniciar sesión |
| **Almacenamiento de archivos** | Firebase Storage (fotos del diario y avatares personalizados) |
| **Caché local** | No aplica — todo se gestiona vía Firestore |

> **¿Por qué API 28 como mínimo?**
> Cubre el ~90% de dispositivos Android activos. Garantiza compatibilidad estable con Jetpack Compose, biometría (huella/face unlock), Firebase FCM, Google Maps API y Gemini API sin necesidad de workarounds.

---

## 3. Paleta de Colores

| Rol | Color principal | Código HEX |
|---|---|---|
| Paciente | Verde | `#0F6E56` |
| Cuidador | Índigo | `#4338A8` |
| SOS / Alerta crítica | Coral | `#E8582A` |

---

## 4. Estructura de Carpetas

```
app/
├── build.gradle
├── settings.gradle
│
├── ui/
│   ├── components/          # Componentes reutilizables (botones, inputs, cards)
│   │   ├── Button.kt
│   │   ├── InputField.kt
│   │   └── Card.kt
│   ├── themes/              # Colores, tipografías, estilos
│   │   ├── ColorPalette.kt
│   │   ├── Typography.kt
│   │   └── Theme.kt
│   └── animations/          # Animaciones Lottie y Compose
│       ├── LottieAnimations.kt
│       └── ComposeAnimations.kt
│
├── features/
│   ├── auth/                # Login, registro, recuperación de contraseña
│   │   ├── LoginScreen.kt
│   │   ├── RegisterScreen.kt
│   │   ├── ForgotPasswordScreen.kt
│   │   └── AuthViewModel.kt
│   ├── patient/             # Vistas del paciente
│   │   ├── DashboardPatientScreen.kt
│   │   ├── MeditateScreen.kt
│   │   ├── ProgressScreen.kt
│   │   ├── ScheduleScreen.kt
│   │   ├── LumiChatScreen.kt
│   │   ├── SettingsPatientScreen.kt
│   │   └── PatientViewModel.kt
│   ├── caregiver/           # Vistas del cuidador
│   │   ├── DashboardCaregiverScreen.kt
│   │   ├── PatientsListScreen.kt
│   │   ├── PatientDetailScreen.kt
│   │   ├── SOSAlertScreen.kt
│   │   ├── AlertsHistoryScreen.kt
│   │   ├── SettingsCaregiverScreen.kt
│   │   └── CaregiverViewModel.kt
│   └── common/              # Vistas compartidas entre roles
│       ├── WelcomeScreen.kt
│       ├── OnboardingScreen.kt
│       └── CheckInScreen.kt
│
├── data/
│   ├── remote/              # Conexión con Firestore y APIs externas
│   │   ├── FirebaseAuthService.kt
│   │   ├── FirestoreRepository.kt
│   │   ├── GeminiApiService.kt
│   │   └── MapsService.kt
│   └── model/               # Modelos de datos
│       ├── Patient.kt
│       ├── Caregiver.kt
│       ├── CheckIn.kt
│       ├── Appointment.kt
│       ├── Alert.kt
│       └── LumiChatSession.kt
│
└── utils/
    ├── ValidationUtils.kt   # Validaciones de formularios
    ├── DateUtils.kt         # Manejo de fechas
    ├── NotificationUtils.kt # Notificaciones push
    └── PermissionUtils.kt   # Manejo de permisos
```

---

## 5. Flujo General de la Aplicación

### 5.1 Pantalla de Bienvenida (Onboarding)
- Se muestra solo al abrir la app por primera vez.
- Contiene logo y nombre de la app.
- Tres slides de introducción con imágenes y texto explicativo.
- Botón **"Omitir"** en la parte superior para saltar el onboarding.

### 5.2 Inicio de Sesión / Registro

**Registro:**
- Campos: nombre, apellidos, fecha de nacimiento, correo, contraseña, confirmación de contraseña.
- Selector de tipo de cuenta con cambio de color: verde (paciente) / azul índigo (cuidador).
- Check de términos y condiciones con link visible.
- Validación de correo con código de 6 dígitos (expira en 120 segundos, reenvío hasta 5 veces).
- Configuración inicial opcional: elegir avatar, activar notificaciones.

**Inicio de sesión:**
- Campos: correo y contraseña.
- Opción "Mantener sesión iniciada".
- Inicio con biometría (configurable desde ajustes).
- Recuperación de contraseña mediante código enviado al correo.

---

## 6. Test Inicial (Paciente)

- Es opcional; se puede saltar con botón **"Omitir"**.
- Si se omite, se asigna puntuación inicial de **0** solo para ese día.
- Preguntas: 7–8 preguntas sobre bienestar general.
- Incluye: estado emocional reciente, energía, estrés.
- **No incluye pregunta de sueño** (esta se reserva únicamente para el check-in diario).

---

## 7. Check-in Diario (Paciente)

El check-in diario recoge el estado actual del paciente. Incluye las siguientes preguntas clave:

- **Estado emocional**: selección de tarjeta entre 5 opciones.
- **Sueño**: selección de tarjeta entre 5 opciones (Pésimo / Mal / Regular / Bien / Excelente).
- **Energía**: barra deslizante de 1 a 10.
- **Estrés**: barra deslizante de 1 a 10.
- **Frecuencia de hábitos**: preguntas con escala Nunca → Siempre.
- **Preguntas Sí/No**: deslizar derecha (Sí) / izquierda (No).
- **Notas adicionales**: texto libre opcional.

---

## 8. Sistema de Puntuación de Bienestar

La puntuación final siempre se muestra en una **escala de 0 a 100 puntos** (redondeada al entero más cercano). Se aplica el mismo método para el test inicial, el check-in diario y el historial de progreso.

### 8.1 Estructura de Bloques

| Bloque | Peso |
|---|---|
| Estado emocional reciente | 30% |
| Hábitos y percepción personal (sueño, energía, estrés) | 40% |
| Escala de frecuencia | 20% |
| Preguntas Sí/No | 10% |

### 8.2 Bloque 1 — Estado emocional reciente (30%)

Pregunta: *"¿Cómo te has sentido durante los últimos días?"*

| Respuesta | Valor |
|---|---|
| Muy mal | 1 |
| Mal | 2 |
| Bien | 3 |
| Muy bien | 4 |
| Excelente | 5 |

Si hay varios días registrados, se calcula el promedio. El resultado se divide entre 5 y se multiplica por 0.30.

**Ejemplo:**
Días: 3, 4, 5, 2, 4 → Promedio: 3.6 → Aporte: 3.6 × 0.30 = **1.08**

### 8.3 Bloque 2 — Hábitos y percepción personal (40%)

Incluye barras deslizables de 1 a 10 para: **Sueño, Energía, Estrés**.

Conversión a escala 1–5: `valor ÷ 2`

**Ejemplo:**
- Sueño: 8 ÷ 2 = 4
- Energía: 6 ÷ 2 = 3
- Estrés: 7 ÷ 2 = 3.5
- Promedio: (4 + 3 + 3.5) / 3 = 3.5
- Aporte: 3.5 × 0.40 = **1.40**

### 8.4 Bloque 3 — Escala de frecuencia (20%)

| Respuesta | Valor |
|---|---|
| Nunca | 1 |
| Casi nunca | 2 |
| A veces | 3 |
| Casi siempre | 4 |
| Siempre | 5 |

**Ejemplo:**
Respuestas: 2, 3, 4, 1, 5 → Promedio: 3 → Aporte: 3 × 0.20 = **0.60**

### 8.5 Bloque 4 — Preguntas Sí/No (10%)

Interacción tipo deslizar: derecha = Sí (1) / izquierda = No (0).

**Ejemplo:** Sí → 1 × 0.10 = **0.10**

### 8.6 Cálculo Final

```
Suma de aportes = 1.08 + 1.40 + 0.60 + 0.10 = 3.18
Puntuación final = (3.18 ÷ 5) × 100 = 63.6 → Redondeado: 64 / 100
```

### 8.7 Interpretación del Resultado

| Puntaje | Estado |
|---|---|
| 0 – 20 | Muy bajo |
| 21 – 40 | Bajo |
| 41 – 60 | Moderado |
| 61 – 80 | Bueno |
| 81 – 100 | Excelente |

### 8.8 Datos Guardados por Registro

Cada registro almacena: fecha, puntaje obtenido, categoría del estado, respuestas individuales y notas adicionales del paciente.

---

## 9. Lumi — Asistente de IA

Lumi es el asistente de inteligencia artificial de RelaxMind, diseñado para acompañar emocionalmente al paciente.

### Características
- **Motor**: Gemini API (Google AI SDK para Android).
- **Prompt inicial del sistema**: fijo e invisible para el usuario; define la personalidad y rol de Lumi como asistente de salud mental.
- **Persistencia de conversación**: el historial de cada sesión se guarda en Firestore. Al reabrir el chat, Lumi recuerda todo lo que se habló anteriormente.
- **Nuevo chat**: el paciente puede iniciar una nueva conversación en cualquier momento. La sesión anterior queda archivada y accesible en el historial.
- **Funciones de Lumi**:
  - Escuchar y acompañar al paciente en sus emociones.
  - Sugerir pasos simples para calmar la ansiedad.
  - Dar consejos de bienestar personalizados según el contexto de la conversación.
  - Recomendar ejercicios de respiración o meditación disponibles en la app.

### Modelo de datos en Firestore

Ver estructura completa en **Sección 17.13 `lumiSessions`**.

---

## 10. Flujo del Paciente

### 10.1 Dashboard
- Puntuación del check-in diario.
- Card **"Meta de Hoy"**: ejercicio de meditación sugerido con botón "Ir a meditar" y check de completado.
- Card **"Próximo recordatorio"**: siguiente evento agendado (vacío si no hay ninguno).
- Acceso rápido a: Meditar, Progreso, Agenda, Lumi, Ajustes.
- Botón **SOS** (mantener presionado 2 segundos para activar).
- Card de vinculación con cuidador.

### 10.2 Meditar
- Ejercicios de respiración con animaciones guiadas (Lottie).
- Guías visuales paso a paso.

### 10.3 Progreso
- **Gráfico mensual de bienestar**: cada día del mes se representa con un círculo coloreado según el puntaje del check-in de ese día. Si no hubo check-in, el círculo queda en gris neutro.

| Puntaje | Color | HEX |
|---|---|---|
| Sin check-in | Gris | `#CBD5E0` |
| 0 – 20 | Rojo | `#E53E3E` |
| 21 – 40 | Naranja | `#ED8936` |
| 41 – 60 | Amarillo | `#ECC94B` |
| 61 – 80 | Verde claro | `#68D391` |
| 81 – 100 | Verde oscuro | `#0F6E56` |

- **Racha activa**: días consecutivos con check-in completado. Se rompe si no se registra check-in antes de las 23:59 del día.
- **Logros desbloqueados**: iconos específicos por tipo de logro (ver tabla de logros abajo).
- **Historial de check-ins**: lista cronológica con puntaje y categoría por registro.

#### 10.3.1 Catálogo de Logros

| ID | Título | Condición | Ícono sugerido (clay plasticine) |
|---|---|---|---|
| `first_checkin` | Primer paso | Primer check-in completado | Brote verde |
| `streak_3` | 3 días seguidos | Racha de 3 días | Llama pequeña |
| `streak_7` | Una semana fuerte | Racha de 7 días | Llama mediana |
| `streak_14` | Dos semanas imparable | Racha de 14 días | Llama grande |
| `streak_30` | Mes completo | Racha de 30 días | Trofeo dorado |
| `first_meditation` | Primer respiro | Primera meditación completada | Figura meditando |
| `meditations_10` | Mente en calma | 10 meditaciones completadas | Nube tranquila |
| `first_diary` | Mi historia | Primera entrada de diario | Diario con estrella |
| `diary_7` | Una semana de notas | 7 entradas de diario | Diario brillante |
| `score_80` | Bienestar alto | Check-in con 80+ puntos | Estrella dorada |
| `score_100` | Día perfecto | Check-in con 100 puntos | Gema azul |
| `lumi_first` | Hola Lumi | Primera conversación con Lumi | Burbuja de chat verde |

### 10.4 Agenda
- Calendario semanal y mensual.
- Crear entradas con categoría: cita médica, medicación o recordatorio.
- Agregar notas y fotos del día (entradas de diario independientes).
- Visualización tipo collage en el calendario mensual.
- Cada evento envía una **notificación push 15 minutos antes** de la hora programada.

### 10.5 Lumi (Chat IA)
- Interfaz de chat con historial persistente.
- Botón **"Nuevo chat"** para iniciar una conversación limpia.
- Historial de sesiones anteriores accesible.

### 10.6 Ajustes del Paciente
- Editar perfil: nombre, apellidos, fecha de nacimiento, sexo, condición.
- Modo oscuro.
- Idioma.
- Notificaciones.
- Biometría.
- Términos y condiciones.
- Cerrar sesión.
- **Datos personales**:
  - Opción **"Desvincular cuidador"** (ver Sección 12).
  - Opción **"Borrar cuenta"** (ver Sección 13).

---

## 11. Flujo del Cuidador

### 11.1 Dashboard
- Resumen de los check-ins más recientes de todos sus pacientes vinculados.
- Alertas activas destacadas (SOS sin resolver y check-ins bajos del día).
- Acceso rápido a la lista de pacientes.

### 11.2 Lista de Pacientes
- Lista de todos los pacientes vinculados con buscador.
- Indicador de estado de bienestar por paciente (color según último puntaje).
- Al tocar un paciente se accede a su perfil detallado.

### 11.3 Perfil del Paciente (vista del cuidador)
- Nombre, avatar y datos básicos del paciente.
- **Gráfico mensual de bienestar** (misma visualización por colores que ve el paciente).
- Historial de check-ins: puntaje y categoría por día.
- Opción de **llamar al paciente** directamente desde esta pantalla.
- Acceso al historial de alertas SOS de ese paciente específico.

### 11.4 Historial de Alertas
Muestra las últimas **10 alertas** de todos los pacientes vinculados al cuidador, ordenadas por fecha descendente. Incluye filtro por fecha y por paciente.

Tipos de alerta que aparecen en el historial:

| Tipo | Condición de disparo | Notificación push |
|---|---|---|
| **SOS** | Paciente activa el botón SOS | Sí — con ubicación en tiempo real |
| **Check-in bajo** | Puntaje del check-in es 20 o menos | Sí |
| **Sin check-in** | El paciente no registró check-in antes de las 23:59 | No |

Cada alerta en el historial muestra: tipo, nombre del paciente, fecha/hora y estado (resuelta / pendiente).

### 11.5 Ajustes del Cuidador
- Mismas opciones que el paciente (perfil, modo oscuro, idioma, notificaciones, biometría, términos).
- No incluye vinculación con cuidador.
- Opción de eliminar cuenta.

---

## 12. Vinculación y Desvinculación Paciente–Cuidador

### Regla de relación
- Un paciente puede tener **un solo cuidador** a la vez.
- Un cuidador puede tener **varios pacientes**.

### Vinculación
- El **paciente** genera el código de vinculación (QR o código de 6 dígitos) desde su dashboard.
- El código tiene una validez de **10 minutos**. Pasado ese tiempo, el paciente debe generar uno nuevo.
- El cuidador escanea el QR o ingresa el código manualmente para vincularse.
- El control total de la vinculación es del paciente.
- **Si el paciente ya tiene un cuidador vinculado**, al intentar escanear su código un segundo cuidador verá el mensaje: *"Este paciente ya está vinculado a un cuidador. El paciente debe desvincularse primero."* No se realiza ninguna acción.

### Desvinculación
1. El paciente va a **Ajustes > Datos personales**.
2. Aparece la opción **"Desvincular cuidador"** (encima del botón "Borrar cuenta").
3. Se muestra una ventana de confirmación: *"¿Deseas desvincularte de [nombre del cuidador]?"*
4. El paciente ingresa su **contraseña** para confirmar.
5. Tras confirmar, la desvinculación es inmediata.

---

## 13. Flujo SOS

### Activación por el paciente
1. El paciente **mantiene presionado el botón SOS durante 2 segundos**.
2. Aparece una ventana SOS con:
   - Botón grande **"Llamar a cuidador"** → abre la app de teléfono con el número del cuidador ya cargado.
3. Simultáneamente se envía al cuidador:
   - **Notificación push** con ubicación en tiempo real del paciente y mensaje de emergencia.
   - **Alerta in-app** si el cuidador tiene la app abierta.

### Respuesta del cuidador
Al tocar la notificación push, el cuidador accede a la **pantalla SOS del cuidador**, que incluye:
- Nombre e información básica del paciente.
- Botón **"Llamar al paciente"** → abre la app de teléfono con el número del paciente.
- Botón **"Ver ubicación"** → expande el minimapa con la ubicación en tiempo real.
- **Minimapa integrado** (Google Maps) con la posición actual del paciente.
- Opción de **trazar ruta** desde la ubicación del cuidador hacia el paciente.

---

## 14. Flujo de Eliminación de Cuenta

Aplica tanto para pacientes como para cuidadores.

1. Ir a **Ajustes > Datos personales > Borrar cuenta**.
2. Ventana de confirmación: *"¿Estás seguro de que deseas eliminar tu cuenta?"*
3. Seleccionar **motivo** de eliminación (lista de opciones + "Otro" con campo de texto libre).
4. Ingresar **contraseña** para confirmar.
5. La cuenta entra en un período de gracia de **7 días** durante los cuales puede ser **reactivada**.
6. Si no se reactiva en 7 días, los datos se eliminan permanentemente en los siguientes **30 días**.

---

## 15. Sistema de Notificaciones Push

Todas las notificaciones usan **Firebase Cloud Messaging (FCM)**. El token del dispositivo se guarda en Firestore al iniciar sesión y se actualiza si cambia.

### 15.1 Notificaciones al Paciente

| Evento | Hora / Disparador | Mensaje |
|---|---|---|
| Recordatorio de check-in diario | Todos los días a las **8:00 PM** | "No olvides registrar tu check-in de hoy y mantener tu racha." |
| Recordatorio de agenda | **15 minutos antes** del evento | "En 15 minutos: [título del evento]" |

### 15.2 Notificaciones al Cuidador

| Evento | Disparador | Mensaje |
|---|---|---|
| Alerta SOS | Paciente activa el botón SOS | "EMERGENCIA: [nombre del paciente] necesita ayuda. Ubicación adjunta." |
| Check-in con puntaje muy bajo | Check-in con puntaje 20 o menos | "[nombre del paciente] registró un bienestar muy bajo hoy (X/100). Revisa su estado." |

> Las alertas de tipo "Sin check-in" (paciente que no hizo check-in antes de las 23:59) **NO generan notificación push** al cuidador. Solo aparecen en el historial de alertas dentro de la app.

---

## 16. Permisos de la App

| Permiso | Cuándo se solicita |
|---|---|
| **Ubicación** | Al acceder a la pantalla de mapas o al activar SOS |
| **Cámara** | Al escanear un código QR o al subir/cambiar avatar |
| **Notificaciones** | Durante la configuración inicial tras registrarse |
| **Biometría** | Al activarla desde ajustes (opcional) |
| **Almacenamiento / Galería** | Al adjuntar fotos en entradas del diario. API 28–32: `READ_EXTERNAL_STORAGE`; API 33+: `READ_MEDIA_IMAGES` |

---

## 17. Base de Datos — Estructura en Firestore

> Todas las colecciones viven en Firebase Firestore. No se usa caché local.

---

### 17.1 `patients`
Almacena los datos de cada usuario con rol paciente.

```
patients/
  └── {patientId}             ← mismo UID que Firebase Auth
        ├── role: "patient"
        ├── name
        ├── lastName
        ├── email
        ├── emailVerified: bool
        ├── phone
        ├── birthDate
        ├── sex
        ├── condition          ← condición de salud declarada (texto libre)
        ├── avatarUrl
        ├── fcmToken           ← token del dispositivo para notificaciones push
        ├── caregiverId        ← null si no tiene cuidador vinculado
        ├── linkedCaregiverAt  ← fecha de vinculación con el cuidador
        ├── darkMode: bool
        ├── language           ← "es" | "en"
        ├── biometricEnabled: bool
        ├── keepSessionActive: bool
        ├── notificationsEnabled: bool
        ├── checkInReminderEnabled: bool  ← si desea recibir el push diario de check-in a las 8PM
        ├── onboardingCompleted: bool
        ├── isDeleted: bool
        ├── deletedAt          ← fecha en que solicitó la eliminación (null si activo)
        ├── deletionReason     ← motivo seleccionado al eliminar cuenta
        └── createdAt
```

---

### 17.2 `caregivers`
Almacena los datos de cada usuario con rol cuidador.

```
caregivers/
  └── {caregiverId}           ← mismo UID que Firebase Auth
        ├── role: "caregiver"
        ├── name
        ├── lastName
        ├── email
        ├── emailVerified: bool
        ├── phone
        ├── birthDate
        ├── avatarUrl
        ├── fcmToken           ← token del dispositivo para notificaciones push SOS
        ├── darkMode: bool
        ├── language           ← "es" | "en"
        ├── biometricEnabled: bool
        ├── keepSessionActive: bool
        ├── notificationsEnabled: bool
        ├── onboardingCompleted: bool
        ├── isDeleted: bool
        ├── deletedAt
        ├── deletionReason
        └── createdAt
```

---

### 17.3 `bindingCodes`
Códigos temporales generados por el paciente para vincularse con un cuidador. Se eliminan automáticamente al usarse o al expirar.

```
bindingCodes/
  └── {codeId}
        ├── patientId
        ├── code               ← string de 6 dígitos
        ├── createdAt
        └── expiresAt          ← TTL de 10 minutos desde la creación
```

---

### 17.4 `checkIns`
Registros del test inicial y del check-in diario del paciente.

```
checkIns/
  └── {checkInId}
        ├── patientId
        ├── type               ← "initial_test" | "daily_checkin"
        ├── date               ← fecha del registro (YYYY-MM-DD)
        ├── score              ← puntuación final 0–100
        ├── category           ← "Muy bajo" | "Bajo" | "Moderado" | "Bueno" | "Excelente"
        ├── emotionalState     ← valor 1–5 (Muy mal → Excelente)
        ├── sleep              ← valor 1–5 (Pésimo → Excelente) — solo check-in diario
        ├── energy             ← valor 1–10
        ├── stress             ← valor 1–10
        ├── frequencyAnswers   ← [ { questionId, value (1–5) } ]
        ├── binaryAnswers      ← [ { questionId, value (0 | 1) } ]
        ├── notes              ← texto libre opcional
        └── createdAt
```

> **Regla de negocio**: solo puede existir un documento `checkIn` por `patientId` y `date`. La app debe verificar antes de crear un nuevo registro que no exista ya uno con la misma fecha para ese paciente.

---

### 17.5 `appointments`
Citas médicas, tomas de medicación y recordatorios del paciente.

```
appointments/
  └── {appointmentId}
        ├── patientId
        ├── title
        ├── type               ← "cita" | "medicacion" | "recordatorio"
        ├── category           ← etiqueta personalizada (ej: "psicólogo", "neurología")
        ├── date               ← fecha (YYYY-MM-DD)
        ├── time               ← hora (HH:mm)
        ├── reminderTime       ← minutos antes del evento para enviar notificación push
        ├── completed: bool    ← si el paciente marcó que lo cumplió
        ├── notificationSent: bool  ← true si ya se envió el push de 15 min antes (evita duplicados)
        ├── notes
        └── createdAt
```

---

### 17.6 `diaryEntries`
Entradas del diario personal del paciente. Distintas a las citas; son reflexiones del día con notas y fotos que forman el collage del calendario mensual.

```
diaryEntries/
  └── {entryId}
        ├── patientId
        ├── date               ← fecha de la entrada (YYYY-MM-DD)
        ├── category           ← "estrés" | "familia" | "trabajo" | "logro" | "otro"
        ├── emotionalTag       ← etiqueta emocional rápida del día (ej: "ansioso", "tranquilo")
        ├── notes              ← texto libre
        ├── photoUrls          ← [ url1, url2, ... ] (imágenes subidas a Firebase Storage)
        └── createdAt
```

---

### 17.7 `meditationExercises`
Catálogo de ejercicios de meditación y respiración disponibles en la app. Se define una vez y no cambia por usuario.

```
meditationExercises/
  └── {exerciseId}
        ├── title
        ├── description
        ├── type               ← "respiracion" | "mindfulness" | "relajacion"
        ├── durationMinutes
        ├── lottieAnimationUrl ← animación asociada al ejercicio
        └── order              ← para ordenar en la pantalla de Meditar
```

---

### 17.8 `completedMeditations`
Registro de qué ejercicios completó cada paciente. Soporte para la card "Meta de Hoy" y los logros.

```
completedMeditations/
  └── {completionId}
        ├── patientId
        ├── exerciseId
        ├── completedAt
        └── isGoalOfTheDay: bool   ← si fue la meta sugerida del día
```

---

### 17.9 `streaks`
Racha activa de cada paciente (días consecutivos con check-in completado).

```
streaks/
  └── {patientId}              ← un documento por paciente
        ├── currentStreak      ← número de días consecutivos activos
        ├── longestStreak      ← mejor racha histórica
        ├── lastCheckInDate       ← fecha del último check-in (para calcular si sigue la racha)
        ├── lastNoCheckinAlertDate ← fecha en que se generó la última alerta 'no_checkin' (evita duplicados)
        └── updatedAt
```

---

### 17.10 `dailyGoals`
Meta de meditación asignada a cada paciente por día. Garantiza que el dashboard siempre muestre el mismo ejercicio sugerido aunque el paciente abra y cierre la app varias veces.

```
dailyGoals/
  └── {goalId}
        ├── patientId
        ├── date               ← fecha de la meta (YYYY-MM-DD)
        ├── exerciseId         ← referencia al ejercicio asignado de meditationExercises
        └── completed: bool    ← true cuando el paciente toca "check" en el dashboard
```

---

### 17.11 `achievements`
Logros desbloqueados por cada paciente. Cada logro tiene su ícono en estilo clay plasticine (coherente con los assets visuales de la app).

```
achievements/
  └── {achievementId}
        ├── patientId
        ├── achievementKey     ← ID del logro (ej: "streak_7", "score_100", "lumi_first")
        ├── type               ← "racha" | "checkin" | "meditacion" | "diario" | "especial"
        ├── title              ← nombre del logro (ej: "Una semana fuerte")
        ├── description        ← descripción breve
        ├── iconUrl            ← URL del asset clay generado
        ├── unlockedAt
        └── streakCount        ← solo aplica si type = "racha"
```

Logros definidos:

| achievementKey | Título | Condición | Ícono |
|---|---|---|---|
| `first_checkin` | Primer paso | Primer check-in completado | Brote verde |
| `streak_3` | 3 días seguidos | Racha de 3 días | Llama pequeña |
| `streak_7` | Una semana fuerte | Racha de 7 días | Llama mediana |
| `streak_14` | Dos semanas imparable | Racha de 14 días | Llama grande |
| `streak_30` | Mes completo | Racha de 30 días | Trofeo dorado |
| `first_meditation` | Primer respiro | Primera meditación completada | Figura meditando |
| `meditations_10` | Mente en calma | 10 meditaciones completadas | Nube tranquila |
| `first_diary` | Mi historia | Primera entrada de diario | Diario con estrella |
| `diary_7` | Una semana de notas | 7 entradas de diario | Diario brillante |
| `score_80` | Bienestar alto | Check-in con 80 puntos o más | Estrella dorada |
| `score_100` | Día perfecto | Check-in con 100 puntos | Gema azul |
| `lumi_first` | Hola Lumi | Primera conversación con Lumi | Burbuja de chat verde |

---

### 17.12 `alerts`
Alertas generadas por eventos críticos del paciente. Aparecen en el historial del cuidador.

```
alerts/
  └── {alertId}
        ├── patientId
        ├── caregiverId
        ├── type               ← "sos" | "low_score" | "no_checkin"
        ├── timestamp          ← momento exacto en que se generó la alerta
        ├── score              ← puntaje del check-in (solo para type "low_score")
        ├── location           ← { lat, lng } (solo para type "sos")
        ├── resolved: bool
        ├── resolvedAt         ← fecha en que el cuidador marcó la alerta como resuelta
        └── seenByCaregiverAt  ← fecha en que el cuidador abrió la alerta
```

Reglas de disparo:

| type | Condición | Push al cuidador |
|---|---|---|
| `sos` | Paciente activa el botón SOS | Sí |
| `low_score` | Puntaje del check-in es 20 o menos | Sí |
| `no_checkin` | No se registró check-in antes de las 23:59 | No |

---

### 17.13 `lumiSessions`
Sesiones de conversación entre el paciente y Lumi.

```
lumiSessions/
  └── {sessionId}
        ├── patientId
        ├── title              ← generado automáticamente (ej: fecha o primeras palabras)
        ├── createdAt
        ├── isActive: bool     ← true = sesión actual abierta / false = archivada
        └── messages/          ← subcolección
              └── {messageId}
                    ├── role       ← "user" | "model"
                    ├── content
                    └── timestamp
```

---

## 18. Consideraciones Finales

- El modo oscuro solo se activa tras iniciar sesión.
- No se usa caché local (Room Database). Toda la información se gestiona directamente con Firestore.
- Las notificaciones push (FCM) se usan para alertas SOS y recordatorios de agenda.
- La app es compatible desde **Android 9.0 (API 28)** para garantizar estabilidad en todas sus funciones.
- El aviso de privacidad y uso de datos es visible desde Ajustes en ambos roles.
- Se recomienda un tutorial opcional después del onboarding para nuevos usuarios.