# Plan de Desarrollo — RelaxMind
> Grupo 4, 2026 · Curso: Desarrollo de Aplicaciones Móviles · Prof. Blancas Núñez, Mitchell Paula  
> Duración total estimada: **12 sprints de 1 semana cada uno**

---

## Principio general de desarrollo

Antes de escribir una sola pantalla, se define la base. El orden correcto es:

```
Configuración del proyecto
       ↓
Sistema de diseño (colores, tipografía, componentes base)
       ↓
Arquitectura y modelos de datos
       ↓
Autenticación (sin esto nada funciona)
       ↓
Flujos del paciente (núcleo de la app)
       ↓
Flujos del cuidador (depende del paciente)
       ↓
Features avanzadas (Lumi, SOS, notificaciones)
       ↓
Pulido visual, animaciones y pruebas finales
```

Cada sprint tiene: objetivo claro, pantallas/archivos a crear, qué conectar con Firestore, y criterio de éxito medible.

---

## Sprint 0 — Configuración del Proyecto y Sistema de Diseño
> **Duración:** 1 semana  
> **Objetivo:** Tener el proyecto configurado, todas las dependencias instaladas y el sistema visual definido antes de tocar cualquier pantalla.

### 0.1 Configuración inicial del proyecto

- Crear proyecto Android en Android Studio con Kotlin + Jetpack Compose
- Configurar `minSdk = 28`, `targetSdk = 34`
- Crear la estructura de carpetas definida en la documentación:
  ```
  ui/ → components/, themes/, animations/
  features/ → auth/, patient/, caregiver/, common/
  data/ → remote/, model/
  utils/
  ```
- Conectar Firebase al proyecto:
  - Descargar `google-services.json` y colocarlo en `/app`
  - Habilitar: **Authentication**, **Firestore**, **Storage**, **FCM**
- Agregar dependencias en `build.gradle`:
  ```kotlin
  // Jetpack Compose
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.navigation:navigation-compose")

  // Firebase
  implementation("com.google.firebase:firebase-auth-ktx")
  implementation("com.google.firebase:firebase-firestore-ktx")
  implementation("com.google.firebase:firebase-storage-ktx")
  implementation("com.google.firebase:firebase-messaging-ktx")

  // Google Maps
  implementation("com.google.maps.android:maps-compose")
  implementation("com.google.android.gms:play-services-location")

  // Gemini AI
  implementation("com.google.ai.client.generativeai:generativeai")

  // Lottie
  implementation("com.airbnb.android:lottie-compose")

  // Coil (carga de imágenes)
  implementation("io.coil-kt:coil-compose")
  ```
- Configurar Google Maps API Key en `AndroidManifest.xml`
- Configurar permisos en `AndroidManifest.xml`:
  ```xml
  <uses-permission android:name="android.permission.INTERNET"/>
  <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
  <uses-permission android:name="android.permission.CAMERA"/>
  <uses-permission android:name="android.permission.USE_BIOMETRIC"/>
  <uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
  <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
      android:maxSdkVersion="32"/>
  <uses-permission android:name="android.permission.READ_MEDIA_IMAGES"/>
  ```

### 0.2 Sistema de diseño

Crear en `ui/themes/`:

**`ColorPalette.kt`** — definir todos los colores de la app:
```kotlin
// Paciente
val PatientGreen = Color(0xFF0F6E56)
val PatientGreenLight = Color(0xFF68D391)

// Cuidador
val CaregiverIndigo = Color(0xFF4338A8)

// SOS
val SOSCoral = Color(0xFFE8582A)

// Gráfico de progreso
val ScoreRed = Color(0xFFE53E3E)
val ScoreOrange = Color(0xFFED8936)
val ScoreYellow = Color(0xFFECC94B)
val ScoreGreenLight = Color(0xFF68D391)
val ScoreGreenDark = Color(0xFF0F6E56)
val ScoreGray = Color(0xFFCBD5E0)

// Neutros
val BackgroundLight = Color(0xFFF7F7F7)
val BackgroundDark = Color(0xFF1A1A2E)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF252536)
```

**`Typography.kt`** — configurar las tres fuentes:
```kotlin
// Outfit → títulos y headers
// Urbanist → textos, descripciones, párrafos
// Montserrat → botones y etiquetas
```

**`Theme.kt`** — MaterialTheme con modo claro y oscuro usando los colores anteriores

### 0.3 Componentes base reutilizables

Crear en `ui/components/` los componentes que se van a usar en toda la app:

- **`RelaxButton.kt`** — botón primario con colores según rol (verde paciente / índigo cuidador), con variante outline y variante destructiva (coral SOS)
- **`RelaxInputField.kt`** — campo de texto con label flotante, soporte de error, ícono opcional
- **`RelaxCard.kt`** — card con sombra suave, borde redondeado, soporte de contenido genérico
- **`RelaxTopBar.kt`** — barra superior con título, botón de retroceso opcional, ícono de acción opcional
- **`RelaxBottomNav.kt`** — barra de navegación inferior (se usará en dashboards)
- **`LoadingIndicator.kt`** — spinner centralizado para estados de carga

### 0.4 Navegación principal

Crear `AppNavGraph.kt` en la raíz de `features/`:
- Definir todas las rutas de la app como sealed class o enum
- Configurar `NavController` con Compose Navigation
- Definir dos grafos separados: `AuthGraph` y `MainGraph`
- El `MainGraph` debe ramificarse según el rol: `PatientGraph` y `CaregiverGraph`

### ✅ Criterio de éxito del Sprint 0
- El proyecto compila sin errores
- Firebase está conectado (verificar en Firebase Console que el proyecto aparece)
- Los componentes base se pueden ver en una pantalla de prueba con los colores y tipografías correctos
- La navegación entre rutas vacías funciona sin crash

---

## Sprint 1 — Onboarding y Autenticación
> **Duración:** 1 semana  
> **Objetivo:** El usuario puede registrarse, verificar su correo, iniciar sesión y recuperar contraseña. Todo conectado con Firebase Auth y Firestore.

### Pantallas a crear

**`WelcomeScreen.kt`** (en `features/common/`)
- 3 slides de introducción con imagen + título + descripción
- Indicador de posición (dots)
- Botón "Omitir" arriba a la derecha
- Botón "Siguiente" / "Comenzar" al final
- Navega a `LoginScreen` al terminar o al omitir
- Guardar en `SharedPreferences` o en `patients.onboardingCompleted` que ya se vio

**`RegisterScreen.kt`** (en `features/auth/`)
- Campos: nombre, apellidos, fecha de nacimiento (DatePicker), correo, contraseña, confirmar contraseña
- Selector de rol con cambio de color animado: verde = paciente / índigo = cuidador
- Checkbox de términos y condiciones con link
- Validaciones en tiempo real con mensajes de error claros
- Al registrar: crear usuario en Firebase Auth → crear documento en `patients/` o `caregivers/` en Firestore
- Navegar a `EmailVerificationScreen`

**`EmailVerificationScreen.kt`** (en `features/auth/`)
- Mostrar campo para ingresar código de 6 dígitos
- Temporizador visible de 120 segundos
- Botón "Reenviar código" (habilitado solo cuando el temporizador llega a 0, máximo 5 veces)
- Al verificar correctamente: navegar a `AvatarSetupScreen`

**`AvatarSetupScreen.kt`** (en `features/auth/`)
- Grid de avatares predefinidos en estilo clay para elegir
- Botón "Omitir" para usar avatar por defecto
- Guardar `avatarUrl` en el documento del usuario en Firestore
- Navegar a `NotificationPermissionScreen`

**`NotificationPermissionScreen.kt`** (en `features/auth/`)
- Pantalla simple explicando por qué se necesitan notificaciones
- Botón "Permitir" → solicitar permiso `POST_NOTIFICATIONS`
- Botón "Ahora no" → continuar sin permiso
- Guardar `notificationsEnabled` en Firestore
- Navegar al dashboard correspondiente según rol

**`LoginScreen.kt`** (en `features/auth/`)
- Campos: correo y contraseña
- Toggle "Mantener sesión iniciada" → guardar `keepSessionActive` en Firestore
- Botón "¿Olvidaste tu contraseña?" → navegar a `ForgotPasswordScreen`
- Iniciar sesión con Firebase Auth
- Consultar rol del usuario en Firestore → redirigir a dashboard correcto
- Soporte de biometría si `biometricEnabled = true` en Firestore

**`ForgotPasswordScreen.kt`** (en `features/auth/`)
- Campo de correo electrónico
- Enviar código de recuperación con Firebase Auth (`sendPasswordResetEmail`)
- Confirmar al usuario que revise su correo

### Modelos de datos a crear

En `data/model/`:
- **`Patient.kt`** — data class con todos los campos de `patients/` en Firestore
- **`Caregiver.kt`** — data class con todos los campos de `caregivers/`

En `data/remote/`:
- **`FirebaseAuthService.kt`** — funciones: `register()`, `login()`, `logout()`, `sendVerificationCode()`, `resetPassword()`
- **`FirestoreRepository.kt`** — funciones: `createPatient()`, `createCaregiver()`, `getPatientById()`, `getCaregiverById()`, `updatePatient()`, `updateCaregiver()`

En `features/auth/`:
- **`AuthViewModel.kt`** — maneja estados de UI, llama a los servicios, expone StateFlow para cada pantalla

### ✅ Criterio de éxito del Sprint 1
- Un usuario nuevo puede registrarse como paciente o cuidador
- El correo de verificación llega y el código funciona
- El usuario puede iniciar sesión y se le redirige al dashboard correcto según su rol
- Los datos del usuario aparecen en Firestore Console
- La recuperación de contraseña envía el correo correctamente

---

## Sprint 2 — Dashboard del Paciente + Check-in Diario
> **Duración:** 1 semana  
> **Objetivo:** El paciente ve su dashboard principal y puede completar su check-in diario. El puntaje se calcula y se guarda en Firestore.

### Pantallas a crear

**`DashboardPatientScreen.kt`** (en `features/patient/`)
- Saludo con nombre del paciente y avatar
- Card de puntuación del día (puntaje + color según categoría + texto del estado)
- Card "Meta de Hoy": nombre del ejercicio + botón "Ir a meditar" + check de completado (consulta `dailyGoals/`)
- Card "Próximo recordatorio": título y hora del siguiente `appointment` del día
- Card de vinculación con cuidador: si no tiene → botón "Vincular cuidador"; si tiene → nombre del cuidador + avatar
- Botón SOS flotante en esquina inferior (diseño coral `#E8582A`)
- Bottom navigation: Dashboard / Meditar / Progreso / Agenda / Lumi

**`CheckInScreen.kt`** (en `features/common/`)
- Pantalla de check-in reutilizable para test inicial y check-in diario
- Paso 1 — Estado emocional: 5 tarjetas con emoji + texto (Muy mal / Mal / Bien / Muy bien / Excelente)
- Paso 2 — Sueño (solo check-in diario): 5 tarjetas (Pésimo / Mal / Regular / Bien / Excelente)
- Paso 3 — Energía: slider de 1 a 10 con valor visible
- Paso 4 — Estrés: slider de 1 a 10 con valor visible
- Paso 5 — Frecuencia de hábitos: tarjetas de opción Nunca → Siempre por cada pregunta
- Paso 6 — Preguntas Sí/No: cards deslizables (swipe derecha = Sí, izquierda = No)
- Paso 7 — Notas adicionales: campo de texto libre opcional
- Barra de progreso visible en la parte superior
- Botón "Omitir" (solo para test inicial)
- Al completar: calcular puntaje con el algoritmo de la Sección 8, guardar en `checkIns/`, actualizar `streaks/`, verificar logros

### Lógica de negocio a implementar

En `utils/`:
- **`WellnessScoreCalculator.kt`** — implementar el algoritmo completo de puntuación:
  - Bloque 1: promedio emocional × 0.30
  - Bloque 2: (sueño/2 + energía/2 + estrés/2) / 3 × 0.40
  - Bloque 3: promedio frecuencias × 0.20
  - Bloque 4: promedio sí/no × 0.10
  - Suma total / 5 × 100 → redondear al entero más cercano
- **`StreakManager.kt`** — al guardar un check-in: comparar `lastCheckInDate` con hoy, incrementar o resetear `currentStreak`, actualizar `longestStreak` si corresponde
- **`AchievementChecker.kt`** — al guardar un check-in: verificar si se desbloqueó algún logro y crearlo en `achievements/`

En `data/model/`:
- **`CheckIn.kt`** — data class completa

En `data/remote/` (agregar a `FirestoreRepository.kt`):
- `createCheckIn()`, `getTodayCheckIn()`, `updateStreak()`, `unlockAchievement()`

### ✅ Criterio de éxito del Sprint 2
- El dashboard carga los datos del paciente desde Firestore
- El paciente puede completar el check-in diario paso a paso
- El puntaje se calcula correctamente y se guarda en Firestore
- No se puede crear un segundo check-in el mismo día
- La racha se actualiza correctamente en `streaks/`
- El dashboard muestra el puntaje del día inmediatamente después del check-in

---

## Sprint 3 — Test Inicial + Ajustes del Paciente
> **Duración:** 1 semana  
> **Objetivo:** El paciente pasa por el test inicial al registrarse y puede editar su perfil y preferencias desde ajustes.

### Pantallas a crear

**`InitialTestScreen.kt`** (en `features/patient/`)
- Reutiliza `CheckInScreen` con `type = "initial_test"` (sin pregunta de sueño)
- Botón "Omitir" visible en todo momento → asignar score = 0 si se salta
- Al completar: guardar en `checkIns/` con `type = "initial_test"`, navegar al dashboard

**`SettingsPatientScreen.kt`** (en `features/patient/`)
- Sección "Mi perfil": nombre, apellidos, fecha de nacimiento, sexo, condición — editable en línea
- Sección "Apariencia": toggle de modo oscuro (actualiza `darkMode` en Firestore y aplica el tema inmediatamente)
- Sección "Idioma": selector ES / EN (actualiza `language` en Firestore)
- Sección "Notificaciones":
  - Toggle general de notificaciones (`notificationsEnabled`)
  - Toggle específico de recordatorio de check-in diario (`checkInReminderEnabled`)
- Sección "Seguridad":
  - Toggle de biometría (`biometricEnabled`)
  - Botón "Cerrar sesión" con confirmación
- Sección "Información": link a Términos y Condiciones, versión de la app
- Sección "Datos personales":
  - Botón "Desvincular cuidador" (visible solo si `caregiverId != null`) → flujo de confirmación + contraseña
  - Botón "Borrar cuenta" (coral) → flujo completo de eliminación

**`EditProfileScreen.kt`** (en `features/patient/`)
- Editar avatar (elegir de lista predefinida o subir foto propia a Firebase Storage)
- Editar campos del perfil
- Guardar cambios en Firestore

### Lógica a implementar

- Aplicar `darkMode` en tiempo real al cambiar el toggle (actualizar el `MaterialTheme` dinámicamente)
- Flujo de desvinculación: confirmar → pedir contraseña → verificar con Firebase Auth → limpiar `caregiverId` y `linkedCaregiverAt` en Firestore
- Flujo de eliminación: confirmar → elegir motivo → pedir contraseña → marcar `isDeleted = true`, `deletedAt = now()` en Firestore
- Subida de fotos de avatar a Firebase Storage con `put()` y obtener URL de descarga

### ✅ Criterio de éxito del Sprint 3
- El test inicial se muestra correctamente al nuevo paciente y puede omitirse
- Los ajustes se sincronizan con Firestore al guardar
- El modo oscuro cambia el tema en tiempo real
- La desvinculación y eliminación de cuenta funcionan con sus flujos de confirmación

---

## Sprint 4 — Progreso y Logros del Paciente
> **Duración:** 1 semana  
> **Objetivo:** El paciente ve su historial de bienestar en un gráfico mensual con colores, su racha activa y sus logros desbloqueados.

### Pantallas a crear

**`ProgressScreen.kt`** (en `features/patient/`)

Sección 1 — Racha:
- Número grande del streak actual con ícono de llama
- Texto "Mejor racha: X días"
- Animación Lottie de celebración al ver una racha nueva

Sección 2 — Gráfico mensual:
- Grid de círculos (uno por día del mes)
- Cada círculo coloreado según el puntaje del check-in de ese día
- Leyenda de colores con rangos de puntaje
- Selector de mes (anterior / siguiente con flechas)
- Cargar check-ins del mes seleccionado desde Firestore

Sección 3 — Logros:
- Grid de tarjetas de logros
- Logros desbloqueados: ícono a color + título + fecha de desbloqueo
- Logros bloqueados: ícono en gris + título + condición para desbloquear (texto gris)

Sección 4 — Historial:
- Lista cronológica de check-ins: fecha, puntaje, categoría, chip de color
- Scroll infinito o paginación

### Lógica a implementar

- Query a Firestore: obtener todos los `checkIns` del paciente filtrados por mes y año
- Mapear puntajes a colores con la función de la paleta
- Query a `achievements/` filtrada por `patientId` para mostrar logros desbloqueados
- Comparar lista de logros desbloqueados vs catálogo completo para mostrar los bloqueados

### ✅ Criterio de éxito del Sprint 4
- El gráfico mensual se pinta correctamente con los colores según puntaje
- Los días sin check-in se muestran en gris
- Se puede cambiar de mes y los datos se actualizan
- Los logros desbloqueados aparecen correctamente
- Los logros bloqueados muestran su condición

---

## Sprint 5 — Módulo de Meditación
> **Duración:** 1 semana  
> **Objetivo:** El paciente accede a los ejercicios de respiración y meditación, los completa con animaciones y la "Meta de Hoy" del dashboard funciona correctamente.

### Pantallas a crear

**`MeditateScreen.kt`** (en `features/patient/`)
- Lista de ejercicios cargada desde `meditationExercises/` en Firestore
- Cards por ejercicio: nombre, tipo (respiración / mindfulness / relajación), duración en minutos
- Chip de "Meta de hoy" en el ejercicio asignado del día (consulta `dailyGoals/`)
- Al tocar un ejercicio → navegar a `MeditationDetailScreen`

**`MeditationDetailScreen.kt`** (en `features/patient/`)
- Nombre y descripción del ejercicio
- Animación Lottie centralizada (la animación varía según tipo de ejercicio)
- Guía paso a paso sincronizada con la animación (texto que cambia en cada fase)
- Temporizador visible con duración del ejercicio
- Barra de progreso del ejercicio
- Botón "Completar" al terminar → registrar en `completedMeditations/`, marcar `dailyGoals.completed = true` si era la meta del día, verificar logros de meditación

### Lógica a implementar

En `data/remote/`:
- `getMeditationExercises()` → cargar catálogo desde Firestore
- `getTodayGoal(patientId)` → obtener la meta del día desde `dailyGoals/`
- `createDailyGoalIfNotExists(patientId)` → al abrir el dashboard, si no existe meta del día, asignar un ejercicio aleatorio y crearlo en `dailyGoals/`
- `completeMeditation(patientId, exerciseId, isGoalOfTheDay)` → registrar en `completedMeditations/`

### Seed de datos en Firestore

Cargar manualmente (o con script) los ejercicios iniciales en `meditationExercises/`:
- Respiración 4-7-8
- Respiración de caja (box breathing)
- Escaneo corporal (body scan)
- Meditación de gratitud
- Respiración diafragmática

### ✅ Criterio de éxito del Sprint 5
- Los ejercicios se cargan desde Firestore
- La animación Lottie se reproduce durante el ejercicio
- Al completar un ejercicio, se registra en Firestore
- La card "Meta de Hoy" del dashboard se marca como completada
- Si se gana un logro de meditación, se desbloquea correctamente

---

## Sprint 6 — Agenda del Paciente
> **Duración:** 1 semana  
> **Objetivo:** El paciente puede crear, ver y gestionar citas, medicaciones y recordatorios. El diario de fotos y notas funciona con el collage mensual.

### Pantallas a crear

**`ScheduleScreen.kt`** (en `features/patient/`)

Vista de calendario:
- Selector de vista: semanal o mensual
- Vista semanal: lista de eventos del día seleccionado
- Vista mensual: collage con miniaturas de fotos de `diaryEntries/` en los días que tienen entradas
- Días con eventos de `appointments/` marcados con punto de color según tipo

**`CreateAppointmentScreen.kt`** (en `features/patient/`)
- Campo de título
- Selector de tipo: cita médica / medicación / recordatorio
- Campo de categoría (psicólogo, neurología, etc.)
- DatePicker para fecha
- TimePicker para hora
- Campo de notas opcional
- Botón guardar → crear documento en `appointments/`, programar notificación push local para 15 minutos antes

**`AppointmentDetailScreen.kt`** (en `features/patient/`)
- Ver detalle de un evento
- Botón "Marcar como completado" → actualizar `completed = true` en Firestore
- Botón "Eliminar" con confirmación

**`DiaryEntryScreen.kt`** (en `features/patient/`)
- Selector de categoría (estrés / familia / trabajo / logro / otro)
- Selector de etiqueta emocional rápida (ansioso / tranquilo / feliz / triste / etc.)
- Campo de notas de texto libre
- Botón para agregar fotos → solicitar permiso de galería → subir a Firebase Storage → guardar URLs en `diaryEntries.photoUrls`
- Guardar → crear documento en `diaryEntries/`, verificar logro `first_diary` y `diary_7`

### Lógica a implementar

- Notificaciones locales programadas con `AlarmManager` o `WorkManager` para el recordatorio de 15 minutos antes del evento
- Marcar `notificationSent = true` en el documento de la cita tras enviar la notificación
- Subida de fotos a Firebase Storage con compresión previa (reducir tamaño para no consumir cuota)
- Carga del collage mensual: query `diaryEntries/` por `patientId` y mes, mostrar miniaturas

### ✅ Criterio de éxito del Sprint 6
- El paciente puede crear un evento y aparece en el calendario
- La notificación push de recordatorio llega 15 minutos antes del evento
- El paciente puede crear entradas de diario con fotos
- Las fotos se suben a Firebase Storage y se muestran en el collage
- El logro de primera entrada de diario se desbloquea al crear la primera

---

## Sprint 7 — Vinculación y Dashboard del Cuidador
> **Duración:** 1 semana  
> **Objetivo:** El flujo de vinculación QR/código funciona. El cuidador ve su dashboard con el estado de sus pacientes.

### Pantallas a crear

**`LinkCaregiverScreen.kt`** (en `features/patient/`)
- Botón "Generar QR" → crear documento en `bindingCodes/` con código de 6 dígitos aleatorio y TTL de 10 minutos
- Mostrar QR generado con la librería `zxing-android-embedded` o similar
- Mostrar también el código de 6 dígitos en texto para quienes no pueden escanear
- Temporizador de 10 minutos visible
- Botón "Generar nuevo código" cuando expira

**`ScanQRScreen.kt`** (en `features/caregiver/`)
- Solicitar permiso de cámara
- Visor de cámara para escanear QR
- Campo alternativo para ingresar código de 6 dígitos manualmente
- Al escanear/ingresar código:
  - Consultar `bindingCodes/` para validar que existe y no expiró
  - Verificar que `patients.caregiverId == null` → si ya tiene cuidador, mostrar mensaje de error
  - Si es válido: actualizar `patients.caregiverId = caregiverId`, `patients.linkedCaregiverAt = now()` → eliminar el código de `bindingCodes/`

**`DashboardCaregiverScreen.kt`** (en `features/caregiver/`)
- Header con nombre y avatar del cuidador
- Sección "Alertas activas": lista de alertas `resolved = false` de sus pacientes, ordenadas por fecha
- Sección "Mis pacientes": lista horizontal scrolleable con nombre, avatar y chip de estado de bienestar (color según último puntaje)
- Botón "Vincularme con paciente" → navegar a `ScanQRScreen`
- Bottom navigation: Dashboard / Pacientes / Alertas / Ajustes

### Lógica a implementar

- Generación de código de 6 dígitos aleatorio único (verificar que no exista en Firestore antes de crear)
- Query en tiempo real (Firestore `addSnapshotListener`) para alertas activas del cuidador
- Al cargar el dashboard: query `patients/` donde `caregiverId == currentCaregiverId`

### ✅ Criterio de éxito del Sprint 7
- El paciente genera un QR y el cuidador puede escanearlo
- El código de 6 dígitos también funciona como alternativa
- El intento de vincular con un paciente ya vinculado muestra el mensaje de error correcto
- El código expira correctamente a los 10 minutos
- El dashboard del cuidador muestra sus pacientes con el estado de bienestar actualizado

---

## Sprint 8 — Vistas del Cuidador: Pacientes y Alertas
> **Duración:** 1 semana  
> **Objetivo:** El cuidador puede ver el perfil detallado de cada paciente con su gráfico de bienestar, y gestionar el historial de alertas.

### Pantallas a crear

**`PatientsListScreen.kt`** (en `features/caregiver/`)
- Lista completa de pacientes vinculados con buscador
- Cada ítem: avatar, nombre, último puntaje con chip de color, indicador de check-in hoy (sí/no)
- Al tocar → navegar a `PatientDetailScreen`

**`PatientDetailScreen.kt`** (en `features/caregiver/`)
- Avatar, nombre, condición de salud del paciente
- Gráfico mensual de bienestar (mismo componente del Sprint 4, reutilizado)
- Selector de mes
- Historial de check-ins: fecha, puntaje, categoría
- Botón "Llamar" → abrir app de teléfono con el número del paciente (`tel:` intent)
- Sección de alertas SOS pasadas de ese paciente específico

**`AlertsHistoryScreen.kt`** (en `features/caregiver/`)
- Lista de las últimas 10 alertas de todos sus pacientes
- Filtro por tipo: SOS / Check-in bajo / Sin check-in
- Filtro por paciente (si tiene varios)
- Filtro por fecha (DateRangePicker)
- Cada alerta muestra: ícono de tipo, nombre del paciente, fecha/hora, estado (resuelta/pendiente)
- Botón "Marcar como resuelta" en alertas pendientes → actualizar `resolved = true`, `resolvedAt = now()`

**`SettingsCaregiverScreen.kt`** (en `features/caregiver/`)
- Mismas secciones que `SettingsPatientScreen` pero sin vinculación
- Incluye flujo de eliminación de cuenta

### Lógica a implementar

- Reutilizar el componente del gráfico mensual del Sprint 4 con el `patientId` del paciente visto
- Query `alerts/` filtrada por `caregiverId` y paginada a 10 resultados
- `tel:` intent para llamar directamente desde la pantalla de perfil del paciente

### ✅ Criterio de éxito del Sprint 8
- El cuidador puede ver el gráfico mensual de cualquiera de sus pacientes
- El botón "Llamar" abre la app de teléfono con el número correcto
- Las alertas se muestran con sus filtros funcionando
- Marcar una alerta como resuelta la actualiza en tiempo real

---

## Sprint 9 — Sistema SOS
> **Duración:** 1 semana  
> **Objetivo:** El botón SOS del paciente funciona completamente: activa la alerta, notifica al cuidador con ubicación en tiempo real y el cuidador puede ver el minimapa y trazar ruta.

### Pantallas a crear

**`SOSPatientScreen.kt`** (en `features/patient/`)
- Se activa al mantener presionado el botón SOS 2 segundos (LongPressGestureDetector)
- Fondo coral `#E8582A`
- Botón grande "LLAMAR A CUIDADOR" → `tel:` intent con el número del cuidador
- Texto "Tu cuidador ha sido notificado" con animación de pulso
- Botón "Cancelar" para cerrar la pantalla

**`SOSAlertScreen.kt`** (en `features/caregiver/`)
- Se abre al tocar la notificación push de SOS
- Nombre y avatar del paciente en crisis
- Botón "LLAMAR AL PACIENTE" → `tel:` intent con el número del paciente
- Minimapa de Google Maps con la ubicación en tiempo real del paciente (actualización cada 5 segundos)
- Botón "VER RUTA" → abrir Google Maps con la ruta desde la ubicación del cuidador hasta el paciente
- Botón "Marcar como resuelta" → actualizar la alerta en Firestore

### Lógica a implementar

- **Detección del long press** de 2 segundos en el botón SOS del dashboard
- **Al activar SOS**:
  1. Obtener ubicación actual del paciente con `FusedLocationProviderClient`
  2. Crear documento en `alerts/` con `type = "sos"`, `location = {lat, lng}`
  3. Actualizar `seenByCaregiverAt = null`
  4. Enviar notificación push FCM al cuidador con el `fcmToken` almacenado en Firestore (requiere Cloud Functions o servidor Node.js para enviar FCM)
- **Actualización de ubicación en tiempo real**: usar `LocationCallback` con intervalo de 5 segundos mientras la pantalla SOS esté abierta, actualizar `alerts.location` en Firestore con cada nueva posición
- **En el cuidador**: `addSnapshotListener` sobre el documento de la alerta para recibir actualizaciones de ubicación en tiempo real y mover el marcador en el mapa

> **Nota**: El envío de FCM desde el cliente no es posible directamente. Se necesita un **Cloud Function** o un backend Node.js mínimo que reciba el trigger de Firestore (nuevo documento en `alerts/`) y envíe la notificación push al `fcmToken` del cuidador.

### ✅ Criterio de éxito del Sprint 9
- El long press de 2 segundos activa la pantalla SOS
- La notificación push llega al cuidador con el mensaje de emergencia
- El minimapa en la pantalla del cuidador muestra la ubicación del paciente
- La ubicación se actualiza en tiempo real mientras el paciente tiene la pantalla abierta
- El botón "Llamar" abre la app de teléfono en ambos lados

---

## Sprint 10 — Lumi (Asistente IA)
> **Duración:** 1 semana  
> **Objetivo:** El paciente puede chatear con Lumi, la conversación persiste entre sesiones y el paciente puede iniciar nuevos chats.

### Pantallas a crear

**`LumiChatScreen.kt`** (en `features/patient/`)
- Header con avatar de Lumi + nombre "Lumi" + botón "Nuevo chat" (ícono de lápiz)
- Lista de mensajes con burbujas diferenciadas: paciente (verde derecha) / Lumi (gris claro izquierda)
- Campo de texto con botón de enviar
- Indicador "Lumi está escribiendo..." con animación de puntos mientras espera respuesta
- Scroll automático al último mensaje
- Al tocar "Nuevo chat": confirmar ("¿Iniciar nueva conversación? La actual quedará guardada") → marcar sesión actual como `isActive = false`, crear nueva sesión en `lumiSessions/`

**`LumiHistoryScreen.kt`** (en `features/patient/`)
- Lista de sesiones archivadas con título y fecha
- Al tocar una sesión → abrir en modo solo lectura (no se puede responder en sesiones archivadas)

### Lógica a implementar

En `data/remote/`:
- **`GeminiApiService.kt`**:
  - Inicializar el modelo `gemini-1.5-flash` con el SDK de Android
  - Configurar el system prompt (prompt inicial de Lumi como asistente de salud mental empático)
  - `sendMessage(history, newMessage)` → enviar historial completo + nuevo mensaje a Gemini, recibir respuesta en streaming
  - Guardar cada mensaje en `lumiSessions/{sessionId}/messages/` en Firestore
- **Al abrir `LumiChatScreen`**:
  - Buscar sesión activa (`isActive = true`) del paciente en `lumiSessions/`
  - Si existe: cargar historial de mensajes
  - Si no existe: crear nueva sesión con `isActive = true`
  - Verificar logro `lumi_first` si es la primera sesión

**System prompt sugerido para Lumi:**
```
Eres Lumi, un asistente de bienestar emocional empático y cálido dentro de la app RelaxMind.
Tu rol es acompañar al paciente, escuchar sus emociones sin juzgar, sugerir técnicas simples
de relajación y respiración, y motivarlo a mantener sus hábitos saludables.
Nunca reemplazas a un profesional de salud mental. Si el paciente expresa pensamientos de 
autolesión o crisis grave, debes sugerirle contactar a su cuidador o a una línea de crisis.
Responde siempre en español, con un tono cálido, conciso y esperanzador.
```

### ✅ Criterio de éxito del Sprint 10
- El paciente puede enviar mensajes y Lumi responde correctamente
- El historial de la conversación persiste al cerrar y reabrir la app
- "Nuevo chat" archiva la sesión anterior y crea una nueva
- Las sesiones archivadas son visibles y se pueden leer
- El logro `lumi_first` se desbloquea en la primera conversación

---

## Sprint 11 — Notificaciones Push y Automatizaciones
> **Duración:** 1 semana  
> **Objetivo:** Todas las notificaciones push funcionan: recordatorio de check-in a las 8PM, recordatorio de agenda 15 min antes, alertas al cuidador (SOS y check-in bajo), alerta de no check-in a las 23:59.

### Tareas

**Configuración de FCM en el cliente:**
- Implementar `FirebaseMessagingService` para recibir notificaciones en segundo plano
- Al recibir push de SOS: abrir `SOSAlertScreen` directamente desde la notificación
- Al recibir push de check-in bajo: abrir `PatientDetailScreen` del paciente afectado
- Crear canales de notificación (`NotificationChannel`) para Android 8+:
  - Canal "SOS" — importancia máxima, sonido y vibración
  - Canal "Recordatorios" — importancia normal
  - Canal "Alertas de bienestar" — importancia alta

**Recordatorio de check-in a las 8PM (cliente):**
- Usar `WorkManager` con `PeriodicWorkRequest` diaria a las 20:00
- Antes de mostrar la notificación: verificar si `checkInReminderEnabled = true` y si el paciente ya hizo check-in hoy (consulta `checkIns/` por fecha)
- Si ya lo hizo: cancelar la notificación del día

**Recordatorio de agenda 15 minutos antes (cliente):**
- Al crear un `appointment`, programar un `OneTimeWorkRequest` con `WorkManager` para ejecutarse 15 minutos antes de la hora del evento
- Al ejecutarse: verificar `notificationSent = false`, mostrar notificación, actualizar `notificationSent = true`

**Alertas al cuidador — SOS y check-in bajo (backend):**
- Implementar **Firebase Cloud Functions** (Node.js):
  - Trigger `onDocumentCreated` en `alerts/`: al crear una alerta de tipo `sos` o `low_score`, leer el `fcmToken` del cuidador desde `caregivers/` y enviar push con `firebase-admin` SDK

**Alerta de no check-in a las 23:59 (backend):**
- Cloud Function con trigger `pubsub.schedule` a las 23:59 diariamente:
  - Obtener todos los pacientes con `caregiverId != null`
  - Para cada uno: verificar si tiene check-in con la fecha de hoy en `checkIns/`
  - Si no tiene: crear alerta `no_checkin` en `alerts/` y verificar `lastNoCheckinAlertDate` para evitar duplicados

**`NotificationUtils.kt`** en `utils/`:
- `showLocalNotification(title, body, channel)` — mostrar notificación local
- `scheduleAppointmentReminder(appointment)` — programar recordatorio de evento
- `cancelAppointmentReminder(appointmentId)` — cancelar si el evento se elimina

### ✅ Criterio de éxito del Sprint 11
- El paciente recibe el recordatorio de check-in a las 8PM (solo si no lo hizo y tiene el toggle activo)
- El paciente recibe el recordatorio de agenda 15 minutos antes del evento
- El cuidador recibe push de SOS con el mensaje correcto
- El cuidador recibe push cuando un paciente tiene puntaje ≤ 20
- La alerta `no_checkin` aparece en el historial del cuidador a las 23:59 sin notificación push

---

## Sprint 12 — Pulido Visual, Animaciones y Pruebas Finales
> **Duración:** 1 semana  
> **Objetivo:** La app se ve y siente terminada. Animaciones fluidas, transiciones entre pantallas, estados de carga correctos y pruebas en dispositivo real.

### Tareas de pulido visual

**Transiciones entre pantallas:**
- Configurar animaciones de entrada/salida en `AppNavGraph` con `AnimatedContentTransitionScope`
- Transición suave de fade + slide entre pantallas principales
- Transición de slide vertical para modales y pantallas de detalle

**Estados de carga:**
- Implementar skeleton loaders para: dashboard, lista de pacientes, historial de check-ins, gráfico de progreso
- Mostrar `CircularProgressIndicator` mientras se espera respuesta de Gemini en el chat

**Animaciones de logros:**
- Al desbloquear un logro: mostrar un modal de celebración con animación Lottie de confeti
- El ícono del logro aparece con animación de escala (pop in)

**Animaciones del check-in:**
- Transición suave entre pasos del check-in (slide horizontal)
- Animación de check al completar el check-in (Lottie)

**Animación del botón SOS:**
- Pulso visual mientras se mantiene presionado (círculo que crece)
- Feedback háptico (vibración) al activar

**Modo oscuro:**
- Revisar todas las pantallas en modo oscuro y corregir colores que no se adapten correctamente
- Asegurar que los textos tengan contraste suficiente en ambos modos

### Pruebas finales

**Pruebas de flujo completo:**
- Registro → verificación → onboarding → test inicial → dashboard ✓
- Check-in diario → cálculo de puntaje → actualización de racha → desbloqueo de logro ✓
- Generación de QR → escaneo → vinculación paciente-cuidador ✓
- Activación SOS → notificación al cuidador → pantalla SOS con mapa ✓
- Chat con Lumi → cerrar app → reabrir → historial persiste ✓
- Crear evento en agenda → recibir notificación 15 min antes ✓
- Recordatorio de check-in a las 8PM ✓
- Alerta de no check-in a las 23:59 aparece en historial del cuidador ✓

**Pruebas en dispositivos reales:**
- Probar en al menos 2 dispositivos físicos con versiones de Android distintas (API 28 y API 33+)
- Verificar permisos en Android 13+ (READ_MEDIA_IMAGES vs READ_EXTERNAL_STORAGE)
- Verificar notificaciones en segundo plano (algunos fabricantes las bloquean)

### ✅ Criterio de éxito del Sprint 12
- Todos los flujos principales funcionan de principio a fin sin crashes
- La app se ve consistente en modo claro y oscuro
- Las animaciones no causan lag perceptible (60fps)
- Las notificaciones llegan correctamente en dispositivos reales

---

## Resumen de Sprints

| Sprint | Enfoque | Pantallas principales |
|---|---|---|
| **0** | Configuración + sistema de diseño | Componentes base, temas, navegación |
| **1** | Onboarding + autenticación | Welcome, Register, Login, Verify Email, Avatar |
| **2** | Dashboard paciente + check-in | Dashboard paciente, CheckIn |
| **3** | Test inicial + ajustes paciente | InitialTest, Settings |
| **4** | Progreso y logros | Progress, gráfico mensual, logros |
| **5** | Meditación | Meditate, MeditationDetail |
| **6** | Agenda y diario | Schedule, CreateAppointment, DiaryEntry |
| **7** | Vinculación + dashboard cuidador | LinkCaregiver, ScanQR, Dashboard cuidador |
| **8** | Vistas del cuidador | PatientsList, PatientDetail, AlertsHistory |
| **9** | Sistema SOS | SOSPatient, SOSAlert + mapa en tiempo real |
| **10** | Lumi IA | LumiChat, LumiHistory |
| **11** | Notificaciones push + backend | Cloud Functions, WorkManager, FCM |
| **12** | Pulido + pruebas | Animaciones, modo oscuro, QA |

---

## Orden de archivos a crear — Vista rápida

```
Sprint 0:
  ui/themes/ColorPalette.kt
  ui/themes/Typography.kt
  ui/themes/Theme.kt
  ui/components/RelaxButton.kt
  ui/components/RelaxInputField.kt
  ui/components/RelaxCard.kt
  ui/components/RelaxTopBar.kt
  ui/components/RelaxBottomNav.kt
  ui/components/LoadingIndicator.kt
  AppNavGraph.kt

Sprint 1:
  data/model/Patient.kt
  data/model/Caregiver.kt
  data/remote/FirebaseAuthService.kt
  data/remote/FirestoreRepository.kt
  features/auth/AuthViewModel.kt
  features/common/WelcomeScreen.kt
  features/auth/RegisterScreen.kt
  features/auth/EmailVerificationScreen.kt
  features/auth/AvatarSetupScreen.kt
  features/auth/NotificationPermissionScreen.kt
  features/auth/LoginScreen.kt
  features/auth/ForgotPasswordScreen.kt

Sprint 2:
  data/model/CheckIn.kt
  utils/WellnessScoreCalculator.kt
  utils/StreakManager.kt
  utils/AchievementChecker.kt
  features/patient/PatientViewModel.kt
  features/patient/DashboardPatientScreen.kt
  features/common/CheckInScreen.kt

Sprint 3:
  features/patient/InitialTestScreen.kt
  features/patient/SettingsPatientScreen.kt
  features/patient/EditProfileScreen.kt

Sprint 4:
  features/patient/ProgressScreen.kt
  ui/components/WellnessCalendarGrid.kt

Sprint 5:
  data/model/MeditationExercise.kt
  features/patient/MeditateScreen.kt
  features/patient/MeditationDetailScreen.kt

Sprint 6:
  data/model/Appointment.kt
  data/model/DiaryEntry.kt
  features/patient/ScheduleScreen.kt
  features/patient/CreateAppointmentScreen.kt
  features/patient/AppointmentDetailScreen.kt
  features/patient/DiaryEntryScreen.kt

Sprint 7:
  data/model/BindingCode.kt
  features/patient/LinkCaregiverScreen.kt
  features/caregiver/CaregiverViewModel.kt
  features/caregiver/ScanQRScreen.kt
  features/caregiver/DashboardCaregiverScreen.kt

Sprint 8:
  data/model/Alert.kt
  features/caregiver/PatientsListScreen.kt
  features/caregiver/PatientDetailScreen.kt
  features/caregiver/AlertsHistoryScreen.kt
  features/caregiver/SettingsCaregiverScreen.kt

Sprint 9:
  features/patient/SOSPatientScreen.kt
  features/caregiver/SOSAlertScreen.kt
  data/remote/MapsService.kt

Sprint 10:
  data/model/LumiChatSession.kt
  data/remote/GeminiApiService.kt
  features/patient/LumiChatScreen.kt
  features/patient/LumiHistoryScreen.kt

Sprint 11:
  utils/NotificationUtils.kt
  utils/PermissionUtils.kt
  FirebaseMessagingService.kt
  cloud-functions/index.js (backend Node.js)

Sprint 12:
  ui/animations/LottieAnimations.kt
  ui/animations/ComposeAnimations.kt
  [revisión y ajuste de todos los archivos anteriores]
```

---

## Reglas del equipo durante el desarrollo

1. **Nadie toca una pantalla sin que el sprint anterior esté terminado.** Las dependencias son reales: no puedes hacer el check-in sin la autenticación, ni el SOS sin la vinculación.

2. **Primero funcional, después bonito.** En los sprints 1 al 11, el objetivo es que funcione. Las animaciones y el pulido visual son del Sprint 12.

3. **Cada pantalla se prueba en el emulador Y en un dispositivo real** antes de marcarse como completada.

4. **Los datos de Firestore se validan siempre** antes de mostrarlos en pantalla. Nunca asumir que un campo existe; usar valores por defecto seguros.

5. **El modo oscuro se verifica en cada pantalla** desde el Sprint 1, no se deja todo para el final.

6. **Las Cloud Functions del Sprint 11** requieren una cuenta de Firebase con plan Blaze (pago por uso). Coordinar con el equipo antes del Sprint 11 para tener eso listo.