# RelaxMind — Prompts de Desarrollo por Sprint
> Usa estos prompts en Claude, Cursor, o cualquier asistente de código para implementar cada sprint correctamente.  
> Incluye contexto de diseño, arquitectura, flujo y criterio de éxito en cada uno.

---

## CONTEXTO BASE (incluir en todos los prompts)

```
Proyecto: RelaxMind — app Android de salud mental y bienestar emocional
Lenguaje: Kotlin + Jetpack Compose (Material 3, API 28 mín / API 34 target)
Backend: Firebase (Auth, Firestore, Storage, FCM)
Arquitectura: MVVM + repositorio, sin Room (todo vía Firestore)
Fuentes: Outfit (títulos), Urbanist (textos), Montserrat (botones)
Colores principales:
  - Paciente: #0F6E56 (verde)
  - Cuidador: #4338A8 (índigo)
  - SOS: #E8582A (coral)
  - Fondo claro: #F7F7F7 | Fondo oscuro: #1A1A2E
  - Surface claro: #FFFFFF | Surface oscuro: #252536
Roles: paciente y cuidador (flujos separados)
Sin caché local — todo en Firestore en tiempo real
```

---

## SPRINT 0 — Configuración del Proyecto y Sistema de Diseño

### Prompt 0.1 — Estructura base del proyecto

```
Estoy creando el proyecto Android "RelaxMind" con Kotlin + Jetpack Compose.
Necesito que configures la estructura de carpetas y dependencias base.

ESTRUCTURA DE CARPETAS a crear en /app/src/main/kotlin/:
  ui/
    components/   → botones, inputs, cards reutilizables
    themes/       → colores, tipografía, tema
    animations/   → Lottie y Compose animations
  features/
    auth/         → login, registro, verificación
    patient/      → vistas del paciente
    caregiver/    → vistas del cuidador
    common/       → vistas compartidas (onboarding, check-in)
  data/
    remote/       → Firebase services y repositorios
    model/        → data classes
  utils/          → validaciones, fechas, notificaciones, permisos

DEPENDENCIAS a agregar en build.gradle (app):
  - androidx.compose.ui:ui
  - androidx.compose.material3:material3
  - androidx.navigation:navigation-compose
  - com.google.firebase:firebase-auth-ktx
  - com.google.firebase:firebase-firestore-ktx
  - com.google.firebase:firebase-storage-ktx
  - com.google.firebase:firebase-messaging-ktx
  - com.google.maps.android:maps-compose
  - com.google.android.gms:play-services-location
  - com.google.ai.client.generativeai:generativeai
  - com.airbnb.android:lottie-compose
  - io.coil-kt:coil-compose

PERMISOS en AndroidManifest.xml:
  INTERNET, ACCESS_FINE_LOCATION, CAMERA, USE_BIOMETRIC,
  POST_NOTIFICATIONS, READ_EXTERNAL_STORAGE (maxSdk 32), READ_MEDIA_IMAGES

Configura minSdk=28, targetSdk=34. Crea los archivos vacíos con el package correcto.
Agrega el placeholder para google-services.json (no incluirlo, solo indicar dónde va).
```

### Prompt 0.2 — Sistema de colores y tipografía

```
Crea el sistema de diseño completo para RelaxMind en ui/themes/.

ARCHIVO ColorPalette.kt:
Define como val de Color de Compose los siguientes colores:
  PatientGreen = #0F6E56
  PatientGreenLight = #68D391
  CaregiverIndigo = #4338A8
  SOSCoral = #E8582A
  ScoreRed = #E53E3E
  ScoreOrange = #ED8936
  ScoreYellow = #ECC94B
  ScoreGreenLight = #68D391
  ScoreGreenDark = #0F6E56
  ScoreGray = #CBD5E0
  BackgroundLight = #F7F7F7
  BackgroundDark = #1A1A2E
  SurfaceLight = #FFFFFF
  SurfaceDark = #252536

ARCHIVO Typography.kt:
Crea un objeto Typography de Material 3 con:
  - displayLarge / displayMedium / displaySmall → fuente Outfit, Bold
  - headlineLarge / headlineMedium / headlineSmall → fuente Outfit, SemiBold
  - bodyLarge / bodyMedium / bodySmall → fuente Urbanist, Normal
  - labelLarge / labelMedium / labelSmall → fuente Montserrat, SemiBold
  (Las fuentes deben importarse como GoogleFont o como assets locales)

ARCHIVO Theme.kt:
Crea RelaxMindTheme(darkTheme: Boolean, content: @Composable () -> Unit) con:
  - ColorScheme claro usando BackgroundLight, SurfaceLight, PatientGreen como primary
  - ColorScheme oscuro usando BackgroundDark, SurfaceDark, PatientGreenLight como primary
  - Aplicar la tipografía definida en Typography.kt
  - El tema debe ser dinámico: recibe darkTheme como parámetro para cambiar en tiempo real

Incluye comentarios explicando cada sección.
```

### Prompt 0.3 — Componentes base reutilizables

```
Crea los 6 componentes base de RelaxMind en ui/components/.
Todos deben usar el tema de Material 3 y los colores de ColorPalette.kt.

1. RelaxButton.kt
   @Composable fun RelaxButton(
     text: String,
     onClick: () -> Unit,
     modifier: Modifier = Modifier,
     variant: ButtonVariant = ButtonVariant.PRIMARY,
     role: AppRole = AppRole.PATIENT,
     enabled: Boolean = true
   )
   Variantes: PRIMARY (filled), OUTLINE (border), DESTRUCTIVE (coral SOSCoral)
   Roles: PATIENT (verde), CAREGIVER (índigo)
   Tipografía Montserrat SemiBold, esquinas redondeadas 12dp

2. RelaxInputField.kt
   @Composable fun RelaxInputField(
     value: String, onValueChange: (String) -> Unit,
     label: String, modifier: Modifier = Modifier,
     isError: Boolean = false, errorMessage: String? = null,
     leadingIcon: ImageVector? = null, trailingIcon: @Composable (() -> Unit)? = null,
     keyboardType: KeyboardType = KeyboardType.Text,
     visualTransformation: VisualTransformation = VisualTransformation.None
   )
   Con label flotante animado, borde que cambia a rojo si isError, mensaje de error debajo

3. RelaxCard.kt
   @Composable fun RelaxCard(
     modifier: Modifier = Modifier,
     onClick: (() -> Unit)? = null,
     elevation: Dp = 2.dp,
     content: @Composable ColumnScope.() -> Unit
   )
   Sombra suave, esquinas redondeadas 16dp, padding interno 16dp

4. RelaxTopBar.kt
   @Composable fun RelaxTopBar(
     title: String,
     onBackClick: (() -> Unit)? = null,
     actions: @Composable RowScope.() -> Unit = {}
   )
   Si onBackClick != null muestra ícono de flecha atrás. Título con fuente Outfit Bold.

5. RelaxBottomNav.kt
   @Composable fun RelaxBottomNav(
     selectedRoute: String,
     onNavigate: (String) -> Unit,
     role: AppRole
   )
   Para PATIENT: Dashboard / Meditar / Progreso / Agenda / Lumi (íconos Material)
   Para CAREGIVER: Dashboard / Pacientes / Alertas / Ajustes
   El color activo cambia según el rol (verde o índigo)

6. LoadingIndicator.kt
   @Composable fun LoadingIndicator(modifier: Modifier = Modifier)
   CircularProgressIndicator centrado en Box con fondo semitransparente

Todos deben tener preview con @Preview en modo claro y oscuro.
```

### Prompt 0.4 — Navegación principal

```
Crea AppNavGraph.kt con la navegación completa de RelaxMind usando Navigation Compose.

RUTAS: Define un sealed class Screen con todas las rutas:
  // Auth
  Screen.Welcome, Screen.Login, Screen.Register,
  Screen.EmailVerification, Screen.AvatarSetup, Screen.NotificationPermission
  Screen.ForgotPassword
  // Patient
  Screen.PatientDashboard, Screen.CheckIn, Screen.InitialTest,
  Screen.Meditate, Screen.MeditationDetail(exerciseId: String),
  Screen.Progress, Screen.Schedule, Screen.CreateAppointment,
  Screen.AppointmentDetail(appointmentId: String), Screen.DiaryEntry,
  Screen.LumiChat, Screen.LumiHistory,
  Screen.PatientSettings, Screen.EditProfile, Screen.LinkCaregiver
  // Caregiver
  Screen.CaregiverDashboard, Screen.PatientsList,
  Screen.PatientDetail(patientId: String), Screen.AlertsHistory,
  Screen.SOSAlert(alertId: String), Screen.ScanQR,
  Screen.CaregiverSettings

LÓGICA DE INICIO:
  - Si usuario no autenticado → Screen.Welcome
  - Si autenticado con rol "patient" → Screen.PatientDashboard (o InitialTest si es nuevo)
  - Si autenticado con rol "caregiver" → Screen.CaregiverDashboard

Crea NavHost con todos los composable{} vacíos (solo un Text("Pantalla X") placeholder).
La función principal: @Composable fun AppNavGraph(navController: NavHostController, startDestination: String)
```

---

## SPRINT 1 — Onboarding y Autenticación

### Prompt 1.1 — Modelos de datos y servicios Firebase

```
Crea los modelos de datos y servicios Firebase para la autenticación de RelaxMind.

DATA CLASSES en data/model/:

Patient.kt:
data class Patient(
  val id: String = "", val role: String = "patient",
  val name: String = "", val lastName: String = "",
  val email: String = "", val emailVerified: Boolean = false,
  val phone: String = "", val birthDate: String = "",
  val sex: String = "", val condition: String = "",
  val avatarUrl: String = "", val fcmToken: String = "",
  val caregiverId: String? = null, val linkedCaregiverAt: String? = null,
  val darkMode: Boolean = false, val language: String = "es",
  val biometricEnabled: Boolean = false, val keepSessionActive: Boolean = false,
  val notificationsEnabled: Boolean = true, val checkInReminderEnabled: Boolean = true,
  val onboardingCompleted: Boolean = false,
  val isDeleted: Boolean = false, val deletedAt: String? = null,
  val deletionReason: String? = null, val createdAt: String = ""
)

Caregiver.kt: mismo patrón sin campos de paciente (caregiverId, linkedCaregiverAt, checkInReminderEnabled, condition)

FirebaseAuthService.kt en data/remote/:
  - suspend fun register(email, password): Result<FirebaseUser>
  - suspend fun login(email, password): Result<FirebaseUser>
  - fun logout()
  - suspend fun sendVerificationEmail(): Result<Unit>
  - suspend fun resetPassword(email): Result<Unit>
  - fun getCurrentUser(): FirebaseUser?
  - fun isLoggedIn(): Boolean

FirestoreRepository.kt en data/remote/:
  - suspend fun createPatient(patient: Patient): Result<Unit>
  - suspend fun createCaregiver(caregiver: Caregiver): Result<Unit>
  - suspend fun getPatientById(id: String): Result<Patient?>
  - suspend fun getCaregiverById(id: String): Result<Caregiver?>
  - suspend fun updatePatient(id: String, fields: Map<String, Any>): Result<Unit>
  - suspend fun updateCaregiver(id: String, fields: Map<String, Any>): Result<Unit>
  - suspend fun getRoleById(id: String): Result<String> (busca en patients/ y caregivers/)

Usa try-catch en cada función, devuelve Result<T>. Usa Firebase Auth y Firestore KTX.
```

### Prompt 1.2 — AuthViewModel

```
Crea AuthViewModel.kt en features/auth/ para RelaxMind.

Debe manejar todos los estados de UI de autenticación con StateFlow.

ESTADOS:
  data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
  )

FUNCIONES:
  - fun register(name, lastName, birthDate, email, password, confirmPassword, role)
    → valida campos con ValidationUtils, llama FirebaseAuthService.register(),
      crea documento en Firestore, emite success
  - fun login(email, password)
    → llama login(), obtiene rol del usuario, emite el rol para que la UI redirija
  - fun sendVerificationCode(): simula envío de código (Firebase usa link; adaptar a OTP custom si es necesario)
  - fun verifyCode(code: String): valida el código de 6 dígitos
  - fun resendCode(): reenvío con límite de 5 veces
  - fun updateAvatar(avatarUrl: String)
  - fun setNotificationPermission(enabled: Boolean)
  - fun resetPassword(email: String)
  - fun clearError()

StateFlows a exponer:
  val uiState: StateFlow<AuthUiState>
  val userRole: StateFlow<String?> (null, "patient", "caregiver")
  val resendCount: StateFlow<Int>
  val timerSeconds: StateFlow<Int> (cuenta regresiva de 120 a 0)

El timer debe iniciarse al pedir el código y detenerse al llegar a 0.
Inyectar FirebaseAuthService y FirestoreRepository en el constructor.
```

### Prompt 1.3 — WelcomeScreen (Onboarding)

```
Crea WelcomeScreen.kt en features/common/ para RelaxMind.

DISEÑO:
- Fondo degradado suave de BackgroundLight a un verde muy claro (#E8F5F0)
- 3 slides con HorizontalPager de Accompanist o Pager de Compose Foundation
- Cada slide tiene:
  * Ilustración grande (placeholder Box con color suave por ahora, después se reemplaza con imagen)
  * Título en Outfit Bold, tamaño 24sp, centrado
  * Descripción en Urbanist Regular, 16sp, gris, centrado, máx 2 líneas
- Indicador de posición (dots): círculo lleno = slide actual, círculo vacío = otros
  Color de los dots: PatientGreen (#0F6E56)
- Botón "Omitir" en top-end como TextButton
- Botón "Siguiente" / "Comenzar" (en el último slide) usando RelaxButton variant=PRIMARY role=PATIENT

CONTENIDO DE SLIDES:
  Slide 1: "Tu bienestar, cada día" / "Registra cómo te sientes y construye hábitos saludables paso a paso."
  Slide 2: "Mindfulness y respiración" / "Ejercicios guiados para calmar tu mente cuando más lo necesitas."
  Slide 3: "Siempre acompañado" / "Tu cuidador siempre conectado para estar ahí cuando lo necesites."

COMPORTAMIENTO:
- Al hacer swipe o presionar "Siguiente" avanza al siguiente slide
- Al presionar "Omitir" o "Comenzar" → navegar a LoginScreen
- Guardar en SharedPreferences que el onboarding ya fue visto (clave: "onboarding_seen")
- Si "onboarding_seen" es true al abrir la app → saltar directo a LoginScreen

Usa animación de fade entre slides. Los botones usan los componentes RelaxButton y RelaxTopBar de ui/components/.
```

### Prompt 1.4 — LoginScreen y RegisterScreen

```
Crea LoginScreen.kt y RegisterScreen.kt en features/auth/ para RelaxMind.

--- LOGIN SCREEN ---
DISEÑO:
- Logo de RelaxMind centrado arriba (placeholder circular verde por ahora)
- Título "Bienvenido de nuevo" Outfit Bold 28sp
- Subtítulo "Ingresa a tu cuenta" Urbanist 16sp gris
- RelaxInputField para correo (keyboardType = Email)
- RelaxInputField para contraseña (visualTransformation = Password, trailingIcon ojo para mostrar/ocultar)
- Row con Switch "Mantener sesión iniciada" + label Urbanist 14sp
- RelaxButton "Iniciar sesión" (PRIMARY, rol dinámico según sesión guardada o default PATIENT)
- TextButton "¿Olvidaste tu contraseña?" → navega a ForgotPasswordScreen
- Divider con "o" centrado
- TextButton "Crear cuenta" → navega a RegisterScreen
- Si hay biometría habilitada: ícono de huella debajo del botón principal

COMPORTAMIENTO:
- Al iniciar sesión: mostrar LoadingIndicator, llamar AuthViewModel.login()
- Si success: leer userRole del ViewModel → navegar a PatientDashboard o CaregiverDashboard
- Si error: mostrar Snackbar con el mensaje de error
- Validar que email no esté vacío y sea formato válido antes de habilitar el botón

--- REGISTER SCREEN ---
DISEÑO:
- RelaxTopBar con flecha atrás → LoginScreen
- Título "Crear cuenta" Outfit Bold 26sp
- ScrollColumn con los campos:
  * Nombre (texto)
  * Apellidos (texto)
  * Fecha de nacimiento (campo que abre DatePickerDialog al tocar)
  * Correo electrónico
  * Contraseña (con toggle de visibilidad)
  * Confirmar contraseña
- SELECTOR DE ROL: dos cards horizontales, una para "Paciente" y otra para "Cuidador"
  * Card seleccionada: borde del color del rol (verde o índigo), fondo tenue del color
  * Card no seleccionada: borde gris, fondo blanco
  * Animación suave de color al cambiar selección con animateColorAsState
  * El color del botón "Registrarme" cambia según el rol seleccionado
- Row con Checkbox + TextButton "Acepto los términos y condiciones"
- RelaxButton "Registrarme" deshabilitado hasta que el checkbox esté marcado

VALIDACIONES en tiempo real (mostrar error bajo cada campo):
  - Nombre y apellidos: mínimo 2 caracteres
  - Fecha: usuario mayor de 13 años
  - Email: formato válido
  - Contraseña: mínimo 8 caracteres, al menos 1 número
  - Confirmar contraseña: debe coincidir

Al registrar exitosamente → navegar a EmailVerificationScreen.
```

### Prompt 1.5 — EmailVerification, AvatarSetup y NotificationPermission

```
Crea 3 pantallas de flujo post-registro en features/auth/ para RelaxMind.

--- EMAIL VERIFICATION SCREEN ---
DISEÑO:
- Ícono grande de sobre/email centrado (usar Material Icon o placeholder)
- Título "Verifica tu correo" Outfit Bold 24sp
- Texto "Ingresa el código de 6 dígitos enviado a [email]" Urbanist 16sp
- 6 campos individuales de 1 dígito cada uno (OTP style):
  * Cajas cuadradas 52x52dp, borde redondeado
  * Al escribir un dígito, el foco salta automáticamente al siguiente campo
  * Al borrar, el foco vuelve al campo anterior
  * Estilo de borde: gris por defecto, verde (#0F6E56) cuando está enfocado o completo
- Timer: "El código expira en 1:58" en rojo cuando quedan menos de 30 segundos
- TextButton "Reenviar código" → deshabilitado mientras el timer está activo; se habilita al llegar a 0
  Mostrar "(3/5 intentos restantes)" junto al botón
- RelaxButton "Verificar" habilitado solo cuando los 6 campos están llenos

COMPORTAMIENTO:
- Al verificar exitosamente → navegar a AvatarSetupScreen
- Si el código es incorrecto: borrar campos y mostrar error "Código incorrecto. Intenta de nuevo."
- Tras 5 reenvíos: deshabilitar el botón permanentemente y mostrar "Límite de reenvíos alcanzado."

--- AVATAR SETUP SCREEN ---
DISEÑO:
- Título "Elige tu avatar" Outfit Bold 24sp
- Subtítulo "Puedes cambiarlo después en ajustes" Urbanist 14sp gris
- LazyVerticalGrid de 4 columnas con 12 avatares predefinidos (por ahora, círculos de colores distintos como placeholder)
  * Avatar seleccionado: borde verde 3dp + escala ligeramente aumentada (animateFloatAsState 1.0 → 1.08)
  * Avatar no seleccionado: sin borde, escala normal
- RelaxButton "Continuar" en la parte inferior
- TextButton "Omitir" → usar avatar por defecto (URL de placeholder)

COMPORTAMIENTO:
- Al continuar: llamar AuthViewModel.updateAvatar(avatarUrl) → actualizar en Firestore
- Navegar a NotificationPermissionScreen

--- NOTIFICATION PERMISSION SCREEN ---
DISEÑO:
- Ilustración de campana o notificación centrada (placeholder por ahora)
- Título "Mantente al día" Outfit Bold 24sp
- Descripción explicando por qué se necesitan notificaciones (check-in, agenda, alertas SOS) Urbanist 16sp gris
- RelaxButton "Permitir notificaciones" PRIMARY → solicitar permiso POST_NOTIFICATIONS
- TextButton "Ahora no" → continuar sin permiso

COMPORTAMIENTO:
- Guardar resultado en Firestore: patients/{id}.notificationsEnabled = true/false
- Navegar al dashboard según rol (PatientDashboard o CaregiverDashboard)
```

---

## SPRINT 2 — Dashboard del Paciente + Check-in Diario

### Prompt 2.1 — WellnessScoreCalculator y lógica de puntuación

```
Crea WellnessScoreCalculator.kt en utils/ para RelaxMind.

Implementa el algoritmo completo de puntuación de bienestar (escala 0-100):

data class CheckInAnswers(
  val emotionalState: Int,       // 1-5: 1=Muy mal, 5=Excelente
  val sleep: Int?,               // 1-5 (null si es test inicial)
  val energy: Int,               // 1-10
  val stress: Int,               // 1-10
  val frequencyAnswers: List<Int>, // lista de valores 1-5 (Nunca → Siempre)
  val binaryAnswers: List<Int>,   // lista de 0s y 1s (No / Sí)
  val notes: String = ""
)

CÁLCULO:
  Bloque 1 (30%): emotionalState.toDouble() / 5.0 * 0.30
  
  Bloque 2 (40%): 
    val sleepNorm = (sleep ?: 3).toDouble() / 5.0  // si es null usar 3 (Regular)
    val energyNorm = energy.toDouble() / 10.0
    val stressNorm = stress.toDouble() / 10.0
    ((sleepNorm + energyNorm + stressNorm) / 3.0) * 0.40
  
  Bloque 3 (20%):
    frequencyAnswers.average() / 5.0 * 0.20
  
  Bloque 4 (10%):
    binaryAnswers.average() * 0.10
  
  TOTAL: ((b1 + b2 + b3 + b4) / 1.0 * 100).roundToInt().coerceIn(0, 100)
  (Los bloques ya están normalizados a su peso, sumados dan un valor que al * 100 da el puntaje)

Función adicional:
  fun getCategory(score: Int): String {
    return when(score) {
      in 0..20 -> "Muy bajo"
      in 21..40 -> "Bajo"
      in 41..60 -> "Moderado"
      in 61..80 -> "Bueno"
      else -> "Excelente"
    }
  }

  fun getScoreColor(score: Int?): Color {
    return when {
      score == null -> ScoreGray
      score <= 20 -> ScoreRed
      score <= 40 -> ScoreOrange
      score <= 60 -> ScoreYellow
      score <= 80 -> ScoreGreenLight
      else -> ScoreGreenDark
    }
  }

Incluye unit tests básicos verificando el ejemplo de la documentación: resultado debe ser 64/100.
```

### Prompt 2.2 — CheckInScreen (flujo de check-in paso a paso)

```
Crea CheckInScreen.kt en features/common/ para RelaxMind.

Es una pantalla multi-paso (wizard) que sirve tanto para el test inicial como para el check-in diario.

PARÁMETRO: isInitialTest: Boolean (si true, no muestra el paso de sueño)

ESTRUCTURA GENERAL:
- LinearProgressIndicator en la parte superior que avanza con cada paso
- Animación de slide horizontal (AnimatedContent) al cambiar de paso
- Botón "Anterior" (texto) a la izquierda y botón "Siguiente" (RelaxButton) a la derecha
- Si isInitialTest=true: mostrar TextButton "Omitir test" en el top-end

PASOS:

Paso 1 — Estado emocional (siempre presente):
  Título: "¿Cómo te has sentido últimamente?"
  5 cards en columna, cada una con emoji grande + texto:
    😭 Muy mal | 😕 Mal | 😐 Bien | 🙂 Muy bien | 😄 Excelente
  Card seleccionada: borde verde 2dp, fondo verde muy claro (#E8F5F0)
  Solo se puede seleccionar una a la vez

Paso 2 — Sueño (solo si !isInitialTest):
  Título: "¿Cómo dormiste anoche?"
  5 cards: 😴 Pésimo | 😪 Mal | 😑 Regular | 😌 Bien | 😊 Excelente

Paso 3 — Energía:
  Título: "¿Cuánta energía sientes hoy?"
  Slider de 1 a 10, con número grande centrado y visible debajo del slider
  Color del track: gradiente de rojo a verde

Paso 4 — Estrés:
  Título: "¿Cuánto estrés sientes?"
  Mismo slider 1 a 10 (aquí 10 = mucho estrés, visualmente invertido con color rojo al final)

Paso 5 — Frecuencia de hábitos:
  Título: "¿Con qué frecuencia...?"
  Preguntas:
    "¿Realizas actividad física?" | "¿Mantienes contacto social con amigos o familia?"
    "¿Dedicas tiempo a actividades que disfrutas?" | "¿Sigues una rutina diaria?"
  Por cada pregunta: row horizontal con 5 opciones como chips: Nunca / Casi nunca / A veces / Casi siempre / Siempre
  Chip seleccionado: fondo verde, texto blanco. No seleccionado: borde gris, fondo blanco.

Paso 6 — Preguntas Sí/No:
  Título: "¿En el último tiempo...?"
  Preguntas:
    "¿Has podido concentrarte en tus actividades habituales?"
    "¿Has podido disfrutar momentos del día?"
  Por cada pregunta: card con swipe gesture
    → Swipe derecha (Sí): borde verde, fondo verde claro, texto "SÍ" aparece
    → Swipe izquierda (No): borde rojo, fondo rojo claro, texto "NO" aparece
    Usar detectHorizontalDragGestures con threshold de 80dp

Paso 7 — Notas:
  Título: "¿Algo más que quieras compartir?"
  OutlinedTextField multilínea, placeholder "Escribe libremente (opcional)"
  Contador de caracteres: "X / 500"
  Botón "Finalizar" → calcular puntaje con WellnessScoreCalculator, guardar en Firestore,
  mostrar pantalla de resultado animada (puntuación grande + categoría + botón "Ver mi dashboard")

STATE: Usa un ViewModel propio (CheckInViewModel) con stateFlow para cada respuesta.
Al finalizar, emitir el resultado calculado y navegar a la pantalla de resultado.
```

### Prompt 2.3 — DashboardPatientScreen

```
Crea DashboardPatientScreen.kt en features/patient/ para RelaxMind.

LAYOUT: Scaffold con RelaxBottomNav (role=PATIENT) y Column scrolleable como body.

HEADER:
  Row con:
    - Column: "Buenos días, [nombre]" Outfit SemiBold 20sp + "Hoy es [fecha]" Urbanist 14sp gris
    - AsyncImage (Coil) del avatar del paciente, circular, 48x48dp, al tocar → navega a EditProfile

CARD DE PUNTUACIÓN DEL DÍA:
  RelaxCard con fondo del color del puntaje (getScoreColor del WellnessScoreCalculator):
    - Si hay check-in hoy: número grande centrado "[X] / 100" + texto de categoría
    - Si no hay check-in: texto "Aún no has registrado tu check-in de hoy"
      + RelaxButton "Hacer check-in" (PRIMARY, PATIENT) → navega a CheckInScreen

CARD "META DE HOY":
  RelaxCard:
    - Título "🎯 Meta de Hoy" Outfit SemiBold 16sp
    - Nombre del ejercicio de meditación asignado (cargado de Firestore dailyGoals/)
    - Row: RelaxButton "Ir a meditar" (outline) + Checkbox "Completado" (deshabilitado si ya está marcado)
    - Si no hay meta asignada: placeholder "Se está generando tu meta..."

CARD "PRÓXIMO RECORDATORIO":
  RelaxCard:
    - Título "📅 Próximo Recordatorio"
    - Si hay appointments hoy con completed=false: mostrar el más cercano con hora y título
    - Si no hay: texto "No tienes recordatorios pendientes" en gris

CARD DE VINCULACIÓN:
  RelaxCard:
    - Si caregiverId == null: ícono de persona + "¿Tienes un cuidador?" + RelaxButton "Vincular"
    - Si caregiverId != null: avatar + nombre del cuidador + chip "Vinculado ✓"

BOTÓN SOS FLOTANTE:
  Box con Alignment.BottomEnd, padding 24dp
  CircleButton de 64x64dp, color SOSCoral (#E8582A)
  Ícono de SOS o teléfono de emergencia
  Usar combinedClickable con onLongClick (2 segundos con pointerInput + detectTapGestures)
  Al long press exitoso → navegar a SOSPatientScreen
  Feedback háptico: HapticFeedback.LongPress

Cargar datos del PatientViewModel con collectAsState(). Mostrar LoadingIndicator mientras carga.
```

---

## SPRINT 3 — Test Inicial y Ajustes del Paciente

### Prompt 3.1 — SettingsPatientScreen

```
Crea SettingsPatientScreen.kt en features/patient/ para RelaxMind.

ESTRUCTURA: Scaffold con RelaxTopBar "Ajustes" (sin flecha atrás, es pestaña del BottomNav).
ScrollColumn con secciones separadas por un pequeño Spacer y texto de sección en gris uppercase.

SECCIÓN "MI PERFIL":
  RelaxCard que al tocar navega a EditProfileScreen:
    Row con avatar (AsyncImage circular 52dp) + Column (nombre completo, correo) + ícono flecha derecha

SECCIÓN "APARIENCIA":
  SettingsToggleRow("Modo oscuro", ícono luna, darkMode) {
    PatientViewModel.updateDarkMode(it)  // actualiza Firestore + emite nuevo valor al Theme
  }

SECCIÓN "IDIOMA":
  SettingsRow("Idioma") → muestra "Español" / "English" con ícono flecha
  Al tocar: ModalBottomSheet con dos opciones: "Español (ES)" y "English (EN)"
  Al elegir: actualizar language en Firestore y re-lanzar activity para aplicar locale

SECCIÓN "NOTIFICACIONES":
  SettingsToggleRow("Notificaciones", ícono campana, notificationsEnabled)
  SettingsToggleRow("Recordatorio de check-in diario", ícono reloj, checkInReminderEnabled)
    (este segundo toggle visible solo si notificationsEnabled=true)

SECCIÓN "SEGURIDAD":
  SettingsToggleRow("Inicio con biometría", ícono huella, biometricEnabled)
  SettingsRow("Cerrar sesión", ícono salida, color rojo) {
    ShowDialog("¿Cerrar sesión?", "Tendrás que ingresar de nuevo.", onConfirm = { logout → navegar a LoginScreen })
  }

SECCIÓN "INFORMACIÓN":
  SettingsRow("Términos y condiciones") → abrir URL en navegador
  SettingsRow("Versión de la app") → Text "1.0.0" a la derecha (no clickeable)

SECCIÓN "DATOS PERSONALES":
  Si caregiverId != null:
    SettingsRow("Desvincular cuidador", color naranja) → showUnlinkDialog()
  SettingsRow("Borrar cuenta", color SOSCoral) → showDeleteAccountDialog()

DIÁLOGOS:
  Desvincular cuidador:
    "¿Deseas desvincularte de [nombre del cuidador]?"
    Campo de contraseña para confirmar → llamar FirebaseAuth.reauthenticate() → limpiar caregiverId en Firestore

  Borrar cuenta:
    Paso 1: "¿Estás seguro?" con lista de consecuencias
    Paso 2: Selector de motivo (DropdownMenu): "Ya no lo necesito" / "Problemas técnicos" / "Privacidad" / "Otro" (+ campo de texto si Otro)
    Paso 3: Campo de contraseña para confirmar → marcar isDeleted=true, deletedAt=now() en Firestore → logout

Crear un @Composable privado SettingsToggleRow(label, icon, checked, onToggle) para reutilizar.
Crear un @Composable privado SettingsRow(label, icon?, color?, onClick) para reutilizar.
```

---

## SPRINT 4 — Progreso y Logros

### Prompt 4.1 — WellnessCalendarGrid y ProgressScreen

```
Crea el componente WellnessCalendarGrid.kt en ui/components/ y la ProgressScreen.kt.

--- WellnessCalendarGrid.kt ---
@Composable fun WellnessCalendarGrid(
  year: Int, month: Int,
  checkIns: Map<Int, Int>,  // día del mes → puntaje (null si no hay check-in)
  modifier: Modifier = Modifier
)

DISEÑO:
- LazyVerticalGrid de 7 columnas (días de la semana)
- Primera fila: headers L M X J V S D en texto pequeño gris
- Rellenar con celdas vacías hasta el primer día del mes (para alinear con el día correcto)
- Cada celda: círculo de 36dp de diámetro
  * Color según getScoreColor(puntaje) — si no hay check-in: ScoreGray (#CBD5E0)
  * Si es el día de hoy: borde blanco 2dp adicional alrededor
  * Al tocar un día con check-in: mostrar Tooltip con "Puntaje: X/100 — Categoría"
- Leyenda debajo del grid: 5 círculos pequeños con su rango
  "0-20 Muy bajo" | "21-40 Bajo" | "41-60 Moderado" | "61-80 Bueno" | "81-100 Excelente"

--- ProgressScreen.kt ---
HEADER: RelaxTopBar "Mi Progreso" (sin flecha, es tab del BottomNav)

SECCIÓN RACHA:
  RelaxCard centrado:
    - Número grande del streak actual con LottieAnimation de llama (loop si streak > 0)
    - Texto "días seguidos" Urbanist 16sp
    - Texto "Mejor racha: X días" Urbanist 14sp gris
    - Si streak = 0: animación de llama apagada o ícono estático

SECCIÓN GRÁFICO MENSUAL:
  Row con botón "<" mes anterior — Texto "Mayo 2026" Outfit SemiBold 18sp — botón ">" mes siguiente
  WellnessCalendarGrid(year, month, checkIns) cargado del ViewModel

SECCIÓN LOGROS:
  Título "Logros" Outfit SemiBold 18sp
  LazyVerticalGrid de 3 columnas:
    Por cada logro del catálogo (12 en total):
      Si desbloqueado: AsyncImage del iconUrl + título + fecha pequeña en verde
      Si bloqueado: Image con colorFilter grayscale + título en gris + condición en gris claro

SECCIÓN HISTORIAL:
  Título "Historial de check-ins" Outfit SemiBold 18sp
  LazyColumn:
    Por cada check-in (ordenado por fecha desc):
      Row: [fecha formateada] — [puntaje / 100] — Chip con color y categoría

Cargar datos del PatientViewModel. Usar collectAsState().
Manejar estado vacío: si no hay check-ins en el mes, mostrar mensaje "Aún no hay registros este mes."
```

---

## SPRINT 5 — Módulo de Meditación

### Prompt 5.1 — MeditateScreen y MeditationDetailScreen

```
Crea MeditateScreen.kt y MeditationDetailScreen.kt en features/patient/ para RelaxMind.

--- MeditateScreen.kt ---
DISEÑO:
- Título "Meditar" Outfit Bold 26sp
- Subtítulo con el ejercicio asignado como meta del día (chip verde "⭐ Meta de hoy")
- LazyColumn con cards de ejercicios cargados de Firestore meditationExercises/:
  
  MeditationCard por cada ejercicio:
    RelaxCard horizontal:
      - Ícono del tipo (respiración 🫁 / mindfulness 🧘 / relajación 🌿) a la izquierda
      - Column: título Outfit SemiBold 16sp + tipo en gris + duración "[X] min"
      - Si es la meta del día: chip verde "Meta de hoy" a la derecha
      - Ícono flecha derecha
    Al tocar → navegar a MeditationDetailScreen(exerciseId)

--- MeditationDetailScreen.kt ---
DISEÑO:
- RelaxTopBar con título del ejercicio y flecha atrás
- LottieAnimation centrada, tamaño 280x280dp (usar placeholder hasta tener los archivos .json reales)
  La animación loop durante todo el ejercicio
- Texto de fase actual (cambia cada N segundos según el tipo de ejercicio):
  Ejemplo para 4-7-8: "Inhala... 4" → "Aguanta... 7" → "Exhala... 8"
  Texto Outfit Bold 22sp, centrado, con animación de fade entre fases (AnimatedContent)
- LinearProgressIndicator mostrando el tiempo total transcurrido (0 a durationMinutes)
- Texto "[tiempo restante] min restantes" Urbanist 14sp gris
- Botón "Pausar / Reanudar" (outline)
- RelaxButton "Completar ejercicio" (PRIMARY PATIENT) habilitado solo cuando el tiempo terminó
  O botón "Terminar antes" (outline pequeño) que también permite completar

TEMPORIZADOR:
  Usar LaunchedEffect + delay(1000L) en un loop para el contador
  Al completar: llamar ViewModel.completeMeditation(exerciseId)
    → guardar en completedMeditations/
    → si era meta del día: actualizar dailyGoals.completed = true
    → verificar logros: first_meditation, meditations_10
  Mostrar modal de celebración: "🎉 ¡Ejercicio completado!" + puntaje ganado + botón "Volver"
```

---

## SPRINT 6 — Agenda y Diario

### Prompt 6.1 — ScheduleScreen, CreateAppointmentScreen y DiaryEntryScreen

```
Crea las pantallas de agenda en features/patient/ para RelaxMind.

--- ScheduleScreen.kt ---
DISEÑO:
- RelaxTopBar "Agenda" + FAB (+) para crear evento → navega a CreateAppointmentScreen
- TabRow con 2 tabs: "Semana" y "Mes"

Vista SEMANAL:
  - Row horizontal con 7 días de la semana actual
  - El día seleccionado tiene fondo PatientGreen, texto blanco
  - Al tocar un día: LazyColumn con los appointments de ese día
  - Cada appointment card: colored dot según tipo (cita=verde, medicación=azul, recordatorio=naranja) + hora + título
  - Si está completado: tachado en gris

Vista MENSUAL:
  - Calendario estilo grid (usar misma base que WellnessCalendarGrid)
  - Días con appointments: punto de color debajo del número
  - Días con diaryEntries con fotos: miniatura de la primera foto como fondo (opacity 0.4)
  - Al tocar un día → BottomSheet con la lista de eventos de ese día

--- CreateAppointmentScreen.kt ---
DISEÑO:
- RelaxTopBar "Nuevo evento" con flecha atrás
- ScrollColumn con campos:
  * RelaxInputField "Título del evento" (obligatorio)
  * Selector de tipo: 3 cards horizontales — Cita médica / Medicación / Recordatorio
    Colores: cita=verde, medicación=azul, recordatorio=naranja
  * RelaxInputField "Categoría" (psicólogo, neurología, etc.) — solo si tipo=Cita
  * Row: DatePicker (botón con fecha formateada) + TimePicker (botón con hora)
  * RelaxInputField "Notas" multilínea opcional
  * RelaxButton "Guardar evento" → crear en appointments/ + programar WorkManager reminder
  
Al guardar: navegar atrás a ScheduleScreen.

--- DiaryEntryScreen.kt ---
DISEÑO:
- RelaxTopBar "Nueva entrada" con flecha atrás
- Selector de categoría: chips scrolleables horizontalmente
  Categorías: Estrés / Familia / Trabajo / Logro / Otro
  Chip seleccionado: fondo PatientGreen, texto blanco
- Selector de etiqueta emocional: fila de emojis con texto
  Opciones: 😟 Ansioso / 😌 Tranquilo / 😊 Feliz / 😢 Triste / 😤 Frustrado / 🤩 Emocionado
- OutlinedTextField "¿Qué quieres recordar de hoy?" multilínea, 6 líneas mínimo
- Sección de fotos:
  * LazyRow con las fotos agregadas (Box con AsyncImage + X para eliminar)
  * Botón "Agregar foto" (outline) → solicitar permiso → PhotoPicker o Intent galería
  * Máximo 5 fotos por entrada
- RelaxButton "Guardar entrada" → subir fotos a Firebase Storage → crear en diaryEntries/
  Verificar logros first_diary y diary_7

Mostrar LoadingIndicator mientras suben las fotos a Storage.
```

---

## SPRINT 7 — Vinculación y Dashboard del Cuidador

### Prompt 7.1 — LinkCaregiverScreen, ScanQRScreen y DashboardCaregiverScreen

```
Crea las pantallas de vinculación y dashboard del cuidador para RelaxMind.

--- LinkCaregiverScreen.kt (patient) ---
DISEÑO:
- RelaxTopBar "Vincular Cuidador" con flecha atrás
- Descripción "Muéstrale este código a tu cuidador para que pueda vincularse contigo"
- Box centrado con el QR generado (usar librería 'com.google.zxing:core' para generar bitmap del QR)
  El QR codifica: { "code": "123456", "patientId": "xxx" }
- Texto grande del código de 6 dígitos debajo del QR (formato "123 456" con espacio central)
- Timer circular o linear: "Este código expira en [tiempo]" — empieza en 10:00 y cuenta regresiva
- TextButton "Generar nuevo código" (deshabilitado mientras el código está vigente)
  Se habilita cuando el timer llega a 0:00

LÓGICA:
  Al entrar a la pantalla: crear documento en bindingCodes/ con código random de 6 dígitos y expiresAt = now + 10 min
  Timer: LaunchedEffect con countdown, al llegar a 0 el botón "Generar nuevo" se habilita
  Usar addSnapshotListener en el documento del código: si caregiverId se asignó → navegar de vuelta al Dashboard con mensaje "¡Cuidador vinculado exitosamente!"

--- ScanQRScreen.kt (caregiver) ---
DISEÑO:
- RelaxTopBar "Vincularme con paciente" con flecha atrás
- CameraX preview fullscreen para escanear QR (usar androidx.camera.* + ML Kit barcode scanning)
- Overlay con cuadro de enfoque central animado
- Divider "o" 
- RelaxInputField "Ingresar código manualmente" + RelaxButton "Verificar código"

LÓGICA al escanear/verificar código:
  1. Buscar en bindingCodes/ donde code == código ingresado y expiresAt > now()
  2. Si no existe o expiró → Snackbar "Código inválido o expirado"
  3. Si existe: obtener patientId → verificar patients/{patientId}.caregiverId == null
  4. Si ya tiene cuidador → Snackbar "Este paciente ya está vinculado a un cuidador. El paciente debe desvincularse primero."
  5. Si libre: actualizar patients/{patientId}.caregiverId = currentCaregiverId → eliminar el bindingCode → Snackbar "¡Vinculación exitosa!" → navegar al Dashboard

--- DashboardCaregiverScreen.kt ---
DISEÑO: Scaffold con RelaxBottomNav(role=CAREGIVER) + color tema índigo

HEADER:
  Row: Column("Hola, [nombre cuidador]" + "Tienes [N] pacientes") + AsyncImage avatar circular

SECCIÓN "ALERTAS ACTIVAS":
  Si hay alertas resolved=false:
    LazyColumn (máx 3 visibles) con AlertCard:
      Row: ícono de tipo (🆘/📊/📅) en fondo rojo/naranja + nombre paciente + hora + botón "Ver"
    TextButton "Ver todas las alertas" → navega a AlertsHistoryScreen
  Si no hay alertas: RelaxCard con mensaje "Todo tranquilo hoy ✓" en verde

SECCIÓN "MIS PACIENTES":
  Título "Mis pacientes ([N])" + TextButton "Ver todos" → PatientsListScreen
  LazyRow horizontal:
    PatientChip por cada paciente:
      Column centrada: AsyncImage circular 60dp con borde del color del último puntaje + nombre 12sp
      Al tocar → PatientDetailScreen(patientId)

FLOATING ACTION BUTTON:
  Si no tiene pacientes aún: fab prominente "Vincularme con un paciente" → ScanQRScreen
  Si ya tiene pacientes: fab pequeño con ícono "+" → ScanQRScreen
```

---

## SPRINT 8 — Vistas del Cuidador: Pacientes y Alertas

### Prompt 8.1 — PatientsListScreen, PatientDetailScreen y AlertsHistoryScreen

```
Crea las pantallas del cuidador para RelaxMind en features/caregiver/.

--- PatientsListScreen.kt ---
DISEÑO:
- RelaxTopBar "Mis Pacientes" (tab de BottomNav cuidador)
- SearchBar en la parte superior que filtra la lista en tiempo real por nombre
- LazyColumn:
  Por cada paciente vinculado:
    RelaxCard horizontal (clickeable → PatientDetailScreen):
      AsyncImage circular 52dp + borde del color del último puntaje (getScoreColor)
      Column: nombre completo + condición de salud (si existe) + "Último check-in: [fecha]"
      Chip de estado de bienestar: "Bueno 72/100" con color de fondo
      Ícono de alerta roja si hay alertas pendientes de ese paciente
- Estado vacío: ilustración + "Aún no tienes pacientes vinculados" + botón "Vincularme ahora"

--- PatientDetailScreen.kt ---
DISEÑO:
- RelaxTopBar con flecha atrás + título nombre del paciente + ícono teléfono (top-end)
  Al tocar ícono teléfono: Intent tel: con el número del paciente
- AsyncImage circular grande (80dp) centrado + nombre + condición
- TabRow con 3 tabs: "Progreso" | "Historial" | "Alertas SOS"

Tab "PROGRESO":
  Reutilizar WellnessCalendarGrid con los datos del paciente (patientId del cuidador)
  Selector de mes arriba
  Texto "Racha actual: X días" debajo del calendario

Tab "HISTORIAL":
  LazyColumn con check-ins del paciente: fecha, puntaje chip coloreado, categoría

Tab "ALERTAS SOS":
  LazyColumn con alertas type="sos" de ese paciente específico
  Cada alerta: fecha/hora + estado (resuelta/pendiente) + botón "Ver ubicación" → SOSAlertScreen

--- AlertsHistoryScreen.kt ---
DISEÑO:
- RelaxTopBar "Historial de Alertas" (tab de BottomNav cuidador)
- FilterChipRow: todos / SOS / Check-in bajo / Sin check-in (chips horizontales scrolleables)
- Si tiene más de 1 paciente: DropdownMenu para filtrar por paciente
- LazyColumn con máximo 10 alertas más recientes (paginación con Firestore .limit(10)):
  AlertHistoryCard:
    Row: ícono del tipo (🆘 rojo / 📊 naranja / 📅 gris) + Column(nombre paciente, fecha/hora, descripción)
    Chip "Pendiente" (rojo) o "Resuelta" (gris)
    Si pendiente: TextButton "Marcar como resuelta" → AlertDialog de confirmación → update Firestore

Cargar datos en tiempo real con addSnapshotListener filtrado por caregiverId.
```

---

## SPRINT 9 — Sistema SOS

### Prompt 9.1 — SOSPatientScreen y SOSAlertScreen

```
Crea el sistema SOS completo para RelaxMind.

--- SOSPatientScreen.kt (features/patient/) ---
DISEÑO:
- Fondo completamente SOSCoral (#E8582A), texto blanco
- Animación Lottie de pulso/radar en el centro (placeholder por ahora)
- Texto grande "SOS ACTIVADO" Outfit Bold 28sp blanco
- Subtítulo "Tu cuidador ha sido notificado" Urbanist 16sp blanco semitransparente
- RelaxButton grande "LLAMAR A CUIDADOR" (blanco con texto coral)
  → Intent tel: con el número del cuidador (obtenido de Firestore)
- TextButton "Cancelar" blanco abajo — al cancelar: AlertDialog "¿Cancelar la alerta SOS?" → si confirma: volver al Dashboard

LÓGICA al abrir esta pantalla:
  1. Solicitar permiso de ubicación si no se tiene
  2. Obtener ubicación actual con FusedLocationProviderClient
  3. Crear alerta en alerts/: { patientId, caregiverId, type:"sos", location:{lat,lng}, resolved:false, timestamp:now }
  4. Iniciar actualización de ubicación cada 5 segundos: actualizar alerts/{alertId}.location en Firestore
  5. Llamar a la Cloud Function (o endpoint) para enviar FCM al cuidador con el fcmToken
  6. Al salir de la pantalla: detener las actualizaciones de ubicación

--- SOSAlertScreen.kt (features/caregiver/) ---
DISEÑO:
- Fondo SOSCoral degradado de arriba a blanco a mitad de pantalla
- RelaxTopBar con flecha atrás (blanca) sobre el fondo coral
- Parte superior (fondo coral):
  Texto "ALERTA SOS" parpadeante (animateFloatAsState opacity 0.3→1.0 loop)
  Nombre del paciente Outfit Bold 24sp blanco
  Avatar del paciente circular blanco-bordeado 72dp
- Parte inferior (fondo blanco/gris claro):
  RelaxButton grande "LLAMAR AL PACIENTE" (PRIMARY PATIENT pero en coral) → tel: intent
  
  MINIMAPA integrado:
    GoogleMap composable de altura 220dp
    Marcador animado pulsante en la posición del paciente
    addSnapshotListener en alerts/{alertId} para actualizar la posición del marcador en tiempo real
    Zoom automático centrado en el paciente
  
  RelaxButton "VER RUTA" (OUTLINE) → Intent hacia Google Maps con:
    https://www.google.com/maps/dir/?api=1&destination={lat},{lng}&travelmode=driving
  
  RelaxButton "Marcar como resuelta" (pequeño, gris) → confirmar → resolved=true en Firestore → pop back

Cargar alertId desde el argumento de navegación. Escuchar la alerta en tiempo real con addSnapshotListener.
```

---

## SPRINT 10 — Lumi (Asistente IA)

### Prompt 10.1 — LumiChatScreen y GeminiApiService

```
Crea el módulo de IA Lumi para RelaxMind.

--- GeminiApiService.kt (data/remote/) ---
val LUMI_SYSTEM_PROMPT = """
Eres Lumi, un asistente de bienestar emocional empático y cálido dentro de la app RelaxMind.
Tu rol es acompañar al paciente, escuchar sus emociones sin juzgar, sugerir técnicas simples
de relajación y respiración, y motivarlo a mantener sus hábitos saludables.
Nunca reemplazas a un profesional de salud mental. Si el paciente expresa pensamientos de 
autolesión o crisis grave, debes sugerirle contactar a su cuidador o a una línea de crisis.
Responde siempre en español, con un tono cálido, conciso y esperanzador.
Mantén las respuestas entre 2 y 4 párrafos cortos.
"""

class GeminiApiService {
  private val model = GenerativeModel(
    modelName = "gemini-1.5-flash",
    apiKey = BuildConfig.GEMINI_API_KEY,
    generationConfig = generationConfig { temperature = 0.8f; maxOutputTokens = 512 }
  )

  suspend fun sendMessage(
    history: List<Content>,
    userMessage: String
  ): Flow<String> {
    val chat = model.startChat(history = listOf(content("user") { text(LUMI_SYSTEM_PROMPT) }) + history)
    return chat.sendMessageStream(userMessage).map { it.text ?: "" }
  }
}

--- LumiChatScreen.kt (features/patient/) ---
DISEÑO:
- RelaxTopBar con:
  Left: AsyncImage avatar de Lumi (círculo verde con ícono sparkle blanco como placeholder)
  Título: "Lumi" + subtítulo pequeño "Asistente de bienestar"
  Right: ícono de lápiz → botón "Nuevo chat"
  Right2: ícono historial → navega a LumiHistoryScreen

ÁREA DE MENSAJES (LazyColumn con reverseLayout = false):
  BubbleUser: Box con fondo PatientGreen, esquinas redondeadas (16dp, excepto bottom-end = 4dp)
    Texto blanco Urbanist 14sp, alineado a la derecha
  BubbleLumi: Box con fondo gris claro (#F0F0F0), esquinas redondeadas (16dp, excepto bottom-start = 4dp)
    Texto oscuro Urbanist 14sp, alineado a la izquierda
    Avatar de Lumi pequeño (24dp) a la izquierda de la burbuja
  
  Indicador "Lumi está escribiendo...":
    Row con avatar pequeño + 3 puntos animados (animateFloatAsState offset Y con distintos delay)
    Visible mientras la respuesta está en streaming

ÁREA DE INPUT (fija abajo):
  Row: RelaxInputField expandible + IconButton de enviar (ícono de avión de papel, color PatientGreen)
  El botón se deshabilita si el campo está vacío o si Lumi está escribiendo

LÓGICA:
  Al abrir: buscar sesión activa (isActive=true) en lumiSessions/ para el paciente
    Si existe: cargar mensajes de la subcolección messages/ ordenados por timestamp
    Si no existe: crear nueva sesión en lumiSessions/ con isActive=true

  Al enviar mensaje:
    1. Agregar mensaje user a la UI inmediatamente (optimistic update)
    2. Guardar en Firestore messages/ con role="user"
    3. Mostrar indicador "Lumi está escribiendo..."
    4. Llamar GeminiApiService.sendMessage(history, newMessage)
    5. Ir actualizando la burbuja de Lumi con el texto en streaming (collect en Flow)
    6. Al completar: guardar mensaje final en Firestore con role="model"
    7. Auto-scroll al final: LaunchedEffect(messages.size) { listState.animateScrollToItem(messages.lastIndex) }

  Al tocar "Nuevo chat":
    AlertDialog "¿Iniciar nueva conversación? La conversación actual quedará guardada en el historial."
    Si confirma: actualizar isActive=false en sesión actual → crear nueva sesión

  Al abrir por primera vez (lumiSessions vacío): verificar logro lumi_first

--- LumiHistoryScreen.kt ---
LazyColumn con sesiones archivadas (isActive=false):
  Por cada sesión: fecha formateada + título de la sesión + TextButton "Ver"
  Al tocar: abrir LumiChatScreen en modo lectura con el sessionId (no permite enviar mensajes nuevos)
```

---

## SPRINT 11 — Notificaciones Push

### Prompt 11.1 — Firebase Cloud Functions (backend)

```
Crea el backend de Cloud Functions para RelaxMind en Node.js.

ARCHIVO: functions/index.js

Necesito 3 funciones:

1. sendSOSAlert — Trigger: onDocumentCreated("alerts/{alertId}")
   Al crearse una alerta nueva:
   - Leer el documento creado
   - Si type !== "sos" && type !== "low_score" → salir sin hacer nada
   - Obtener fcmToken del cuidador desde caregivers/{caregiverId}
   - Si type === "sos": enviar notificación con:
     title: "🆘 ALERTA SOS — [nombre paciente]"
     body: "[nombre paciente] necesita ayuda inmediata. Toca para ver su ubicación."
     data: { alertId, type: "sos", screen: "SOSAlert" }
     priority: "high", channelId: "sos"
   - Si type === "low_score": enviar notificación con:
     title: "⚠️ Bienestar bajo — [nombre paciente]"  
     body: "[nombre paciente] registró bienestar muy bajo hoy ([score]/100). Revisa cómo está."
     data: { alertId, type: "low_score", screen: "PatientDetail" }
     channelId: "wellness_alerts"
   - Usar admin.messaging().send(message)

2. checkDailyCheckIns — Trigger: pubsub.schedule("59 23 * * *") timezone "America/Lima"
   Cada día a las 23:59 Lima:
   - Obtener todos los pacientes donde caregiverId !== null && isDeleted === false
   - Para cada paciente: buscar en checkIns/ donde patientId === patient.id && date === today
   - Si no existe check-in hoy:
     * Verificar que no se haya generado ya una alerta no_checkin hoy (streaks.lastNoCheckinAlertDate !== today)
     * Crear alerta: { patientId, caregiverId, type:"no_checkin", timestamp:now, resolved:false }
     * Actualizar streaks/{patientId}.lastNoCheckinAlertDate = today
   - NO enviar notificación push para este tipo de alerta

3. sendCheckInReminder — Trigger: pubsub.schedule("0 20 * * *") timezone "America/Lima"
   Cada día a las 20:00 Lima:
   - Obtener todos los pacientes donde checkInReminderEnabled === true && isDeleted === false
   - Para cada uno: verificar si ya hizo check-in hoy
   - Si no: enviar push a fcmToken del paciente:
     title: "📋 ¿Cómo estuvo tu día?"
     body: "No olvides registrar tu check-in de hoy y mantener tu racha."
     channelId: "reminders"

Incluir: const admin = require("firebase-admin"); admin.initializeApp();
Exportar las 3 funciones. Incluir manejo de errores con try/catch y logs con logger.info/logger.error.
```

### Prompt 11.2 — NotificationUtils y WorkManager (cliente Android)

```
Crea el sistema de notificaciones del cliente Android para RelaxMind.

--- FirebaseMessagingService.kt ---
class RelaxMindMessagingService : FirebaseMessagingService() {
  
  override fun onNewToken(token: String) {
    // Guardar nuevo token en Firestore: actualizar patients o caregivers según el rol activo
    // Obtener uid actual de FirebaseAuth y llamar FirestoreRepository.updateFcmToken(uid, token, role)
  }
  
  override fun onMessageReceived(message: RemoteMessage) {
    val data = message.data
    when(data["type"]) {
      "sos" → mostrar notificación en canal "SOS" con:
        PendingIntent que abre SOSAlertScreen con alertId del data
        Sonido máximo, vibración, priority MAX
      "low_score" → canal "wellness_alerts", abre PatientDetailScreen
      "reminder" → canal "reminders", abre CheckInScreen
    }
  }
}

Registrar en AndroidManifest.xml con intent-filter MESSAGING_EVENT.

--- NotificationUtils.kt ---
object NotificationUtils {
  const val CHANNEL_SOS = "channel_sos"
  const val CHANNEL_WELLNESS = "channel_wellness_alerts"  
  const val CHANNEL_REMINDERS = "channel_reminders"

  fun createNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      // Canal SOS: IMPORTANCE_HIGH, vibration=true, sonido de alarma
      // Canal Wellness: IMPORTANCE_HIGH
      // Canal Reminders: IMPORTANCE_DEFAULT
    }
  }

  fun scheduleAppointmentReminder(context: Context, appointment: Appointment) {
    val triggerTime = calcular(appointment.date, appointment.time) - 15 * 60 * 1000L
    if (triggerTime <= System.currentTimeMillis()) return
    
    val data = Data.Builder()
      .putString("appointmentId", appointment.id)
      .putString("title", appointment.title)
      .build()
    
    val request = OneTimeWorkRequestBuilder<AppointmentReminderWorker>()
      .setInitialDelay(triggerTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
      .setInputData(data)
      .addTag("appointment_${appointment.id}")
      .build()
    
    WorkManager.getInstance(context).enqueueUniqueWork(
      "appointment_${appointment.id}", ExistingWorkPolicy.REPLACE, request
    )
  }

  fun cancelAppointmentReminder(context: Context, appointmentId: String) {
    WorkManager.getInstance(context).cancelAllWorkByTag("appointment_$appointmentId")
  }
}

--- AppointmentReminderWorker.kt ---
class AppointmentReminderWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
  override suspend fun doWork(): Result {
    val title = inputData.getString("title") ?: return Result.failure()
    // Mostrar notificación local: "En 15 minutos: [título]"
    // Actualizar notificationSent=true en Firestore para el appointment
    return Result.success()
  }
}

--- CheckInReminderWorker.kt ---
PeriodicWorkRequest diaria a las 20:00:
  Verificar checkInReminderEnabled del usuario actual
  Verificar si ya hizo check-in hoy (query Firestore)
  Si no: mostrar notificación local de recordatorio de check-in
```

---

## SPRINT 12 — Pulido Visual y Animaciones

### Prompt 12.1 — Transiciones, animaciones y pulido final

```
Aplica el pulido visual final a RelaxMind en Sprint 12.

TRANSICIONES EN AppNavGraph.kt:
Configurar en cada composable{} de NavHost:
  enterTransition: slideInHorizontally(initialOffsetX = { it }) + fadeIn(tween(300))
  exitTransition: slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(tween(300))
  Para pantallas de detalle (PatientDetail, MeditationDetail, SOSAlert):
    enterTransition: slideInVertically(initialOffsetY = { it })
    exitTransition: slideOutVertically(targetOffsetY = { it })

SKELETON LOADERS (crear en ui/components/SkeletonLoaders.kt):
  @Composable fun ShimmerEffect(): Modifier (animación de brillo de izquierda a derecha)
  @Composable fun DashboardSkeleton(): placeholder del dashboard mientras cargan datos
  @Composable fun PatientListSkeleton(): 4 cards placeholder para la lista de pacientes
  @Composable fun ProgressCalendarSkeleton(): grid de círculos grises

MODAL DE LOGRO DESBLOQUEADO (crear en ui/components/AchievementUnlockedDialog.kt):
  Dialog animado que aparece al desbloquear un logro:
    - Fondo oscuro semitransparente
    - Card centrada con animación scale: 0.5 → 1.0 con spring(dampingRatio=Spring.DampingRatioMediumBouncy)
    - LottieAnimation de confeti (loop=false, una sola vez)
    - AsyncImage del ícono del logro, animado con scale pop-in
    - "¡Logro desbloqueado!" Outfit Bold 20sp
    - Título del logro en PatientGreen
    - Botón "¡Genial!" para cerrar
  Mostrar desde AchievementChecker cuando se desbloquea un logro nuevo

ANIMACIÓN BOTÓN SOS:
  En DashboardPatientScreen, el botón SOS debe tener:
    Animación de pulso permanente (círculos que se expanden desde el centro en loop)
    Implementar con InfiniteTransition:
      val scale by infiniteTransition.animateFloat(1f → 1.4f, infiniteRepeatable(tween(1000), RepeatMode.Restart))
      Dibujar 2 círculos concéntricos con alpha decreciente alrededor del botón

ANIMACIÓN CHECK-IN COMPLETADO:
  Al terminar el check-in, mostrar una pantalla de resultado animada:
    El número del puntaje cuenta de 0 hasta el valor real (animateIntAsState con spring)
    Debajo: texto de categoría que hace fade in después del contador
    LottieAnimation de check o celebración según el puntaje (diferente según categoría)

MODO OSCURO — revisión:
  Verificar que en todas las pantallas:
    Los textos grises claros tengan suficiente contraste en fondo oscuro (#1A1A2E)
    Los cards usen SurfaceDark (#252536) en modo oscuro
    Las animaciones Lottie no tengan colores hardcodeados blancos que desaparezcan en dark mode
    Los inputs OutlinedTextField tengan borde visible en ambos modos

FEEDBACK HÁPTICO:
  En el botón SOS: HapticFeedbackType.LongPress al activarse
  En el check-in al completar: HapticFeedbackType.TextHandleMove
  En los sliders: HapticFeedbackType.TextHandleMove al cambiar de valor entero
```

---

## NOTAS GENERALES PARA TODOS LOS SPRINTS

- Siempre usar `Result<T>` para operaciones de red/Firestore y manejar errores en el ViewModel
- Los ViewModels deben usar `viewModelScope.launch` para corrutinas
- Usar `collectAsStateWithLifecycle()` en lugar de `collectAsState()` para eficiencia
- Toda navegación debe hacerse desde la UI, nunca desde el ViewModel (emitir eventos con `Channel<UiEvent>`)
- Nombrar los archivos exactamente como se especifica en la documentación para mantener coherencia
- Los textos en español deben ir en `strings.xml` para preparar la internacionalización
- Agregar `@Preview` con `@PreviewLightDark` en todos los componentes y pantallas nuevas