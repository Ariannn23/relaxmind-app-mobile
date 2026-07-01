# RelaxMind - Documentacion tecnica por modulos para exposicion

Este documento explica las partes principales desarrolladas en RelaxMind, una aplicacion Android de bienestar emocional construida con Kotlin, Jetpack Compose, MVVM, Firebase y servicios externos. La explicacion esta pensada para una exposicion: primero se describe que hace cada modulo, luego como se implemento, que flujo sigue el usuario y que archivos participaron.

## 1. Check-in diario y Progreso

### Objetivo del modulo

El modulo de check-in permite que el paciente registre como se siente durante el dia. A partir de respuestas simples, la app calcula una puntuacion de bienestar entre 0 y 100, clasifica el resultado y lo guarda en Firestore. Esa informacion alimenta la pantalla de Progreso, el dashboard del paciente, el historial visible para el cuidador y las alertas automaticas cuando el bienestar es bajo.

### Flujo de usuario

1. El paciente entra al dashboard.
2. Si aun no hizo el check-in del dia, aparece una card de "Check-in diario" con boton para iniciar.
3. El formulario diario usa pocas preguntas para evitar fatiga:
   - Como te sientes hoy.
   - Cuanto dormiste.
   - Nivel de energia.
   - Nivel de estres.
4. Al completar, se calcula el puntaje.
5. Se muestra una pantalla de resultado.
6. Se guarda el check-in en Firestore.
7. Se actualiza la racha.
8. Si el puntaje es bajo, se genera alerta para el cuidador.

### Diferencia entre test inicial y check-in diario

El proyecto diferencia dos tipos de evaluacion:

- `initial_test`: evaluacion inicial del paciente, mas completa, usada en onboarding.
- `daily_checkin`: evaluacion diaria, breve y repetible.

Ambas comparten base visual y logica, pero no tienen el mismo peso de preguntas. El test inicial usa mas bloques de respuestas, mientras que el check-in diario se simplifico a 4 entradas para que el paciente pueda completarlo rapidamente.

### Calculo de puntaje

El calculo esta centralizado en:

`app/src/main/kotlin/com/relaxmind/app/utils/WellnessScoreCalculator.kt`

Funciones principales:

- `calculateScore(answers: CheckInAnswers)`: calcula el puntaje del test inicial con bloques de estado emocional, energia, estres, frecuencia y respuestas binarias.
- `calculateDailyScore(emotionalState, sleep, energy, stress)`: calcula el puntaje del check-in diario.
- `getCategory(score)`: convierte el numero en categoria.
- `shouldAlertCaregiver(score)`: define si se debe avisar al cuidador. Actualmente considera alerta cuando el puntaje es menor o igual a 30.
- `getScoreColor(score)`: retorna el color visual asociado al puntaje.

La formula diaria usa ponderaciones:

- Estado emocional: 35%.
- Sueño: 25%.
- Energia: 20%.
- Estres: 20%, invertido porque menor estres significa mejor bienestar.

### Modelo de datos usado

Archivo:

`app/src/main/kotlin/com/relaxmind/app/data/model/CheckIn.kt`

Campos principales:

- `id`: identificador unico.
- `patientId`: paciente dueño del registro.
- `type`: `initial_test` o `daily_checkin`.
- `date`: fecha en formato `YYYY-MM-DD`.
- `score`: puntaje de 0 a 100.
- `category`: categoria textual.
- `emotionalState`, `sleep`, `energy`, `stress`: respuestas principales.
- `frequencyAnswers`, `binaryAnswers`: respuestas extra para test inicial.
- `notes`: notas opcionales.
- `createdAt`: timestamp de Firestore.

### ViewModel del check-in

Archivo:

`app/src/main/kotlin/com/relaxmind/app/features/common/CheckInViewModel.kt`

Responsabilidades:

- Mantener estado de respuestas con `StateFlow`.
- Validar valores por defecto si el usuario omite campos.
- Crear el objeto `CheckIn`.
- Calcular puntaje usando `WellnessScoreCalculator`.
- Guardar en Firestore con `createCheckIn`.
- Actualizar `onboardingCompleted` cuando es test inicial.
- Actualizar la racha con `updatePatientStreak`.
- Evaluar logros con `AchievementManager`.
- Crear alerta de bajo bienestar si el paciente esta enlazado a cuidador.
- Enviar push de bienestar bajo mediante `NotificationApiService`.

Funcion clave:

- `submitCheckIn(isInitialTest: Boolean)`: ejecuta todo el flujo de guardado, calculo, alertas, racha y logros.

### Pantallas relacionadas

Archivos:

- `app/src/main/kotlin/com/relaxmind/app/features/common/CheckInScreen.kt`
- `app/src/main/kotlin/com/relaxmind/app/features/common/WellnessResultScreen.kt`
- `app/src/main/kotlin/com/relaxmind/app/features/patient/DashboardPatientScreen.kt`
- `app/src/main/kotlin/com/relaxmind/app/features/patient/ProgressScreen.kt`

`CheckInScreen.kt` contiene la UI del formulario. Recibe `isInitialTest` para decidir si se comporta como test inicial o check-in diario.

`WellnessResultScreen.kt` muestra el resultado final, categoria y mensaje de recomendacion.

`DashboardPatientScreen.kt` consume el check-in de hoy para:

- Mostrar "Mi bienestar hoy".
- Cambiar la card de check-in entre "Iniciar" y "Listo".
- Mostrar puntaje y categoria.

`ProgressScreen.kt` muestra:

- Racha actual.
- Mejor racha.
- Calendario mensual de bienestar.
- Leyenda de colores.
- Logros.
- Historial de check-ins.

### Firestore usado en el modulo

Archivo:

`app/src/main/kotlin/com/relaxmind/app/data/remote/FirestoreRepository.kt`

Funciones relacionadas:

- `createCheckIn(checkIn)`
- `getTodayCheckIn(patientId, date)`
- `getPatientCheckIns(patientId)`
- `getLatestCheckIn(patientId)`
- `updatePatientStreak(patientId, checkInDateStr)`
- `getPatientStreak(patientId)`
- `getPatientAchievements(patientId)`
- `unlockAchievement(userAchievement)`
- `createAlert(alert)`

Colecciones usadas:

- `checkIns`
- `streaks`
- `patients`
- `alerts`
- `achievements`

### Alertas por check-in bajo

Cuando `shouldAlertCaregiver(score)` devuelve `true`, `CheckInViewModel` crea una alerta tipo `low_checkin`.

Archivo de modelo:

`app/src/main/kotlin/com/relaxmind/app/data/model/CaregiverAlert.kt`

Campos relevantes:

- `caregiverId`
- `patientId`
- `patientName`
- `type`
- `title`
- `message`
- `severity`
- `resolved`
- `createdAtText`
- `createdAt`

Ademas de guardar la alerta en Firestore, se llama a:

`app/src/main/kotlin/com/relaxmind/app/data/remote/NotificationApiService.kt`

Funcion:

- `sendWellnessAlert(...)`

## 2. Chat con Lumi y Centros cercanos

### Objetivo del modulo Lumi

Lumi es el asistente de bienestar emocional de RelaxMind. Su funcion es acompañar al paciente con respuestas empaticas, breves y utiles. No reemplaza a un profesional de salud mental, pero puede sugerir respiracion, escritura en diario, meditacion y pasos simples de autocuidado.

### Pantallas de Lumi

Archivos:

- `app/src/main/kotlin/com/relaxmind/app/features/patient/lumi/LumiChatScreen.kt`
- `app/src/main/kotlin/com/relaxmind/app/features/patient/lumi/LumiChatViewModel.kt`
- `app/src/main/kotlin/com/relaxmind/app/features/patient/lumi/LumiHistoryScreen.kt`
- `app/src/main/kotlin/com/relaxmind/app/features/patient/lumi/LumiHistoryViewModel.kt`
- `app/src/main/kotlin/com/relaxmind/app/data/model/LumiModels.kt`
- `app/src/main/kotlin/com/relaxmind/app/data/remote/GroqApiService.kt`

### Implementacion del chat

`LumiChatScreen.kt` maneja la interfaz:

- Header con avatar de Lumi.
- Lista de mensajes tipo chat.
- Burbuja de paciente.
- Burbuja de Lumi.
- Indicador de escritura.
- Campo de texto.
- Boton de enviar.
- Acceso al historial.

`LumiChatViewModel.kt` maneja:

- Creacion o recuperacion de sesion activa.
- Envio del mensaje del paciente.
- Escucha en tiempo real de mensajes guardados.
- Streaming de respuesta del modelo.
- Guardado de mensajes en Firestore.
- Actualizacion del titulo de la conversacion.

### Servicio de IA

Archivo:

`app/src/main/kotlin/com/relaxmind/app/data/remote/GroqApiService.kt`

La app usa Groq con endpoint compatible con OpenAI:

- Modelo: `llama-3.3-70b-versatile`.
- Streaming: activado con Server-Sent Events.
- Temperatura: `0.65`.
- Max tokens: `220`.

El prompt del sistema define a Lumi como:

- Empatico.
- Calido.
- Breve.
- No sustituto medico.
- Capaz de recomendar ayuda del cuidador o linea de crisis si hay riesgo.

Se limito el tamaño de respuesta para evitar textos demasiado largos:

- 1 o 2 parrafos cortos.
- Maximo aproximado de 80 palabras.
- Si da pasos, maximo 3 puntos.

### Persistencia de Lumi

FirestoreRepository contiene:

- `getActiveLumiSession(patientId)`
- `createLumiSession(session)`
- `listenToLumiMessages(sessionId, onChange, onError)`
- `addLumiMessage(sessionId, message)`
- `getLumiSessionsHistory(patientId)`
- `archiveLumiSession(sessionId)`
- `updateLumiSessionTitle(sessionId, title)`
- `updateLumiSessionTitleIfDefault(sessionId, text)`

Colecciones usadas:

- `lumiSessions`
- `lumiMessages` o subcolecciones segun estructura del repositorio.

### Centros cercanos

El modulo de centros cercanos ayuda al paciente a ubicar centros de salud o asistencia cercanos.

Archivos:

- `app/src/main/kotlin/com/relaxmind/app/features/patient/NearbyHealthScreen.kt`
- `app/src/main/kotlin/com/relaxmind/app/features/patient/NearbyHealthViewModel.kt`
- `app/src/main/kotlin/com/relaxmind/app/data/model/NearbyHealthCenter.kt`
- `app/src/main/kotlin/com/relaxmind/app/data/remote/MapsService.kt`

### Implementacion de centros cercanos

`NearbyHealthScreen.kt` presenta:

- Lista de centros.
- Informacion de direccion.
- Telefono si existe.
- Distancia aproximada.
- Horario.
- Tipo de centro.

`NearbyHealthViewModel.kt` coordina permisos, ubicacion y carga de datos.

`MapsService.kt` encapsula la busqueda. Actualmente usa una lista mock ordenada por distancia, con centros de Trujillo y calculo local mediante `Location.distanceBetween`. Tambien esta preparado para Places API con:

- `PlacesClient`
- `SearchNearbyRequest`
- `CircularBounds`

Dependencias relacionadas:

- `com.google.maps.android:maps-compose`
- `com.google.android.gms:play-services-location`
- `com.google.android.libraries.places:places`

## 3. Agenda y Editar perfil

### Objetivo de Agenda

La agenda permite al paciente registrar citas, medicacion y recordatorios. La informacion se muestra por semana y calendario mensual. Tambien se integra con notificaciones locales y alarmas para recordar eventos.

### Pantallas y archivos

- `app/src/main/kotlin/com/relaxmind/app/features/patient/ScheduleScreen.kt`
- `app/src/main/kotlin/com/relaxmind/app/features/patient/CreateAppointmentScreen.kt`
- `app/src/main/kotlin/com/relaxmind/app/features/patient/AppointmentDetailScreen.kt`
- `app/src/main/kotlin/com/relaxmind/app/data/model/Appointment.kt`
- `app/src/main/kotlin/com/relaxmind/app/utils/ReminderManager.kt`
- `app/src/main/kotlin/com/relaxmind/app/services/AppointmentAlarmReceiver.kt`
- `app/src/main/kotlin/com/relaxmind/app/services/BootReceiver.kt`
- `app/src/main/kotlin/com/relaxmind/app/services/NotificationUtils.kt`

### Funcionalidad de Agenda

`ScheduleScreen.kt` implementa:

- Header con titulo y boton de crear evento.
- Selector de tabs: semana y calendario.
- Vista semanal con los dias de la semana.
- Vista mensual con puntos o indicadores de eventos.
- Integracion con entradas de diario para ver actividad del dia.
- BottomSheet para revisar eventos de un dia del calendario.
- Navegacion a detalle de evento.

`CreateAppointmentScreen.kt` implementa:

- Formulario de nuevo evento.
- Categoria del evento.
- Fecha.
- Hora.
- Recordatorio antes del evento.
- Repeticion semanal.
- Selector de dias L, M, M, J, V, S, D.
- Validacion antes de guardar.

`AppointmentDetailScreen.kt` implementa:

- Vista detallada del evento.
- Fecha y hora.
- Categoria.
- Recordatorio.
- Boton para marcar como completado.
- Boton para eliminar evento.

### Firestore en Agenda

Funciones de `FirestoreRepository.kt`:

- `createAppointment(appointment)`
- `getAppointments(patientId, date)`
- `getAppointment(appointmentId)`
- `updateAppointmentCompletion(appointmentId, completed)`
- `updateAppointmentNotificationSent(appointmentId)`
- `deleteAppointment(appointmentId)`
- `getAppointmentsForMonth(patientId, yearMonth)`

Coleccion principal:

- `appointments`

### Notificaciones y alarmas de agenda

`ReminderManager.kt` programa recordatorios locales.

`AppointmentAlarmReceiver.kt` recibe la alarma y muestra notificacion local.

`NotificationUtils.kt` crea canales de notificacion, por ejemplo:

- Recordatorios.
- SOS.
- Alertas de bienestar.
- Racha/check-in.

`BootReceiver.kt` permite reprogramar recordatorios despues de reiniciar el dispositivo.

### Editar perfil

Hay dos pantallas de edicion:

- Paciente: `app/src/main/kotlin/com/relaxmind/app/features/patient/EditProfileScreen.kt`
- Cuidador: `app/src/main/kotlin/com/relaxmind/app/features/caregiver/EditProfileCaregiverScreen.kt`

### Funciones implementadas en editar perfil

La edicion permite actualizar:

- Nombre.
- Apellidos.
- Fecha de nacimiento.
- Sexo.
- Telefono.
- Condicion o notas de salud, en el caso del paciente.
- Avatar, cuando aplica.

ViewModels relacionados:

- `PatientViewModel.updateProfile(...)`
- `CaregiverViewModel.updateProfile(...)`

Repositorio:

- `FirestoreRepository.updatePatient(id, fields)`
- `FirestoreRepository.updateCaregiver(id, fields)`

Validaciones:

- `app/src/main/kotlin/com/relaxmind/app/utils/ValidationUtils.kt`

Se corrigieron problemas importantes:

- Fecha de nacimiento editable incluso si no estaba registrada.
- Boton de guardar se activa cuando cambia cualquier campo.
- Colorimetria diferenciada por rol: verde paciente, morado cuidador.

## 4. Alerta SOS e Historial de alertas

### Objetivo de SOS

El SOS permite al paciente enviar una alerta urgente a su cuidador. La alerta puede incluir ubicacion, estado activo, cancelacion y resolucion. El cuidador puede verla desde el dashboard o historial, abrir detalle, llamar al paciente, ver ruta y marcarla como resuelta.

### Archivos del paciente

- `app/src/main/kotlin/com/relaxmind/app/features/patient/SOSPatientScreen.kt`
- `app/src/main/kotlin/com/relaxmind/app/features/patient/SOSPatientViewModel.kt`

### Archivos del cuidador

- `app/src/main/kotlin/com/relaxmind/app/features/caregiver/SOSAlertScreen.kt`
- `app/src/main/kotlin/com/relaxmind/app/features/caregiver/SOSAlertViewModel.kt`
- `app/src/main/kotlin/com/relaxmind/app/features/caregiver/AlertsHistoryScreen.kt`
- `app/src/main/kotlin/com/relaxmind/app/features/caregiver/GlobalCaregiverAlertObserver.kt`

### Modelo de alerta

Archivo:

`app/src/main/kotlin/com/relaxmind/app/data/model/CaregiverAlert.kt`

Campos:

- `id`
- `caregiverId`
- `patientId`
- `patientName`
- `type`
- `title`
- `message`
- `severity`
- `resolved`
- `latitude`
- `longitude`
- `createdAtText`
- `createdAt`

Tipos usados:

- `sos`
- `sos_cancelled`
- `low_checkin`
- `no_checkin`
- `UNLINK`

### Flujo SOS

1. Paciente presiona boton SOS.
2. La app valida cuidador enlazado y permisos necesarios.
3. Se crea o actualiza alerta en Firestore.
4. Se obtiene ubicacion si esta disponible.
5. Se llama a `NotificationApiService.sendSosAlert`.
6. El cuidador recibe notificacion push.
7. El cuidador abre `SOSAlertScreen`.
8. Puede llamar al paciente, ver ruta, compartir o marcar como resuelta.
9. Si el paciente cancela, la alerta deja de aparecer como activa, pero queda en historial como cancelada.
10. Si el cuidador resuelve, la vista del paciente escucha el cambio y muestra estado resuelto.

### Funciones del paciente

`SOSPatientViewModel.kt`:

- Crea o actualiza alerta.
- Actualiza ubicacion.
- Escucha alerta actual para saber si fue resuelta.
- Actualiza preferencias de notificaciones.

Repositorio:

- `createAlert(alert)`
- `updateAlertLocation(alertId, latitude, longitude)`
- `listenToAlert(alertId, onChange, onError)`
- `updateAlertFields(alertId, fields)`

### Funciones del cuidador

`SOSAlertViewModel.kt`:

- Escucha alerta por id.
- Carga datos del paciente.
- Marca alerta como resuelta.
- Actualiza `resolved`.

`SOSAlertScreen.kt`:

- Muestra pantalla de emergencia.
- Presenta datos del paciente.
- Muestra mapa o espacio de ubicacion.
- Permite llamar.
- Permite ver ruta.
- Permite compartir.
- Permite marcar como resuelta.
- Muestra estados de alerta no disponible, cancelada o resuelta.

### Historial de alertas

Archivo:

`app/src/main/kotlin/com/relaxmind/app/features/caregiver/AlertsHistoryScreen.kt`

Funcionalidades:

- Lista de alertas del cuidador.
- Filtros por tipo:
  - Todos.
  - SOS.
  - Check-in bajo.
  - Sin check-in.
  - Desvinculado.
- Filtro por paciente.
- Filtro por rango:
  - Ultimas 10.
  - Hoy.
  - 7 dias.
  - 30 dias.
- Badges de estado:
  - Pendiente.
  - Resuelta.
  - Cancelada.
- BottomSheet o detalle de alerta.
- Accion de marcar como resuelta.
- Skeletons de carga para evitar parpadeo de estado vacio.

## 5. Cuidador: Dashboard y Lista de pacientes

### Objetivo del rol cuidador

El cuidador puede monitorear varios pacientes vinculados, ver alertas activas, consultar historial, entrar al detalle de cada paciente y vincular nuevos pacientes mediante QR o codigo manual. Se limito el maximo de pacientes a 5.

### Dashboard del cuidador

Archivo:

`app/src/main/kotlin/com/relaxmind/app/features/caregiver/DashboardCaregiverScreen.kt`

Elementos principales:

- Header con saludo y avatar.
- Card de alertas activas.
- Card de pacientes vinculados.
- Boton flotante para agregar paciente.
- Bottom nav morado.
- Fondo degradado del cuidador.
- Skeleton/loading mientras cargan datos para evitar mostrar "sin pacientes" antes de recibir Firestore.

### ViewModel del cuidador

Archivo:

`app/src/main/kotlin/com/relaxmind/app/features/caregiver/CaregiverViewModel.kt`

Funciones relevantes:

- `observeCaregiverData()`: observa perfil, pacientes y alertas.
- `markAlertResolved(alertId)`: resuelve alerta.
- `unlinkPatient(patientId, passwordConfirm, reason, onSuccess, onError)`: desvincula paciente.
- `updateProfile(...)`: actualiza datos del cuidador.
- `updateDarkMode(enabled)`
- `updateLanguage(lang)`
- `updateNotificationsEnabled(enabled)`
- `updateBiometricEnabled(enabled)`
- `deleteAccount(...)`

### Lista de pacientes

Archivo:

`app/src/main/kotlin/com/relaxmind/app/features/caregiver/PatientsListScreen.kt`

Funcionalidades:

- Header estandarizado.
- SearchBar para filtrar por nombre o condicion.
- LazyColumn de pacientes.
- Card por paciente.
- Avatar circular.
- Borde por estado de bienestar.
- Chip de puntaje.
- Icono de alerta si hay pendientes.
- Estado vacio cuando realmente no hay pacientes.
- Skeleton tipo lista durante carga.
- Boton para vincular si no hay pacientes.

### Detalle de paciente

Archivo:

`app/src/main/kotlin/com/relaxmind/app/features/caregiver/PatientDetailScreen.kt`

Muestra:

- Header con nombre.
- Accion de llamar.
- Accion de desvincular.
- Avatar.
- Condicion.
- Contacto: telefono y correo.
- Tabs:
  - Progreso.
  - Historial.
- Grafica de bienestar.
- Historial de check-ins.
- Acceso a detalle de check-in.

Se ajusto para que el cuidador solo vea informacion, sin editar datos del paciente.

### Vinculacion paciente-cuidador

Archivos:

- Paciente: `app/src/main/kotlin/com/relaxmind/app/features/patient/PatientLinkCaregiverScreen.kt`
- Paciente ViewModel: `app/src/main/kotlin/com/relaxmind/app/features/patient/LinkCaregiverViewModel.kt`
- Cuidador: `app/src/main/kotlin/com/relaxmind/app/features/caregiver/CaregiverLinkPatientScreen.kt`
- Modelo: `app/src/main/kotlin/com/relaxmind/app/data/model/BindingCode.kt`

Flujo:

1. Paciente genera codigo y QR.
2. Se crea documento en `bindingCodes`.
3. El codigo tiene expiracion.
4. Cuidador escanea QR o ingresa codigo manual.
5. Se previsualizan datos basicos del paciente.
6. Cuidador confirma vinculacion.
7. Firestore valida que el cuidador no supere 5 pacientes.
8. Se actualiza `patients/{patientId}.caregiverId`.
9. Se elimina o invalida el codigo.
10. Paciente escucha el documento y vuelve al dashboard cuando se completa.

Repositorio:

- `createBindingCode(...)`
- `listenToBindingCode(...)`
- `previewPatientForBindingCode(...)`
- `linkPatientWithCode(...)`
- `deleteBindingCode(...)`
- `listenPatientsForCaregiver(...)`

## 6. Login y registros

### Objetivo

El modulo de autenticacion permite crear cuentas, iniciar sesion, recuperar contraseña, verificar correo, configurar avatar, solicitar permisos de notificacion y diferenciar flujos por rol: paciente o cuidador.

### Archivos principales

- `app/src/main/kotlin/com/relaxmind/app/features/auth/LoginScreen.kt`
- `app/src/main/kotlin/com/relaxmind/app/features/auth/RegisterScreen.kt`
- `app/src/main/kotlin/com/relaxmind/app/features/auth/AuthViewModel.kt`
- `app/src/main/kotlin/com/relaxmind/app/features/auth/EmailVerificationScreen.kt`
- `app/src/main/kotlin/com/relaxmind/app/features/auth/AvatarSetupScreen.kt`
- `app/src/main/kotlin/com/relaxmind/app/features/auth/NotificationPermissionScreen.kt`
- `app/src/main/kotlin/com/relaxmind/app/features/auth/ForgotPasswordScreen.kt`
- `app/src/main/kotlin/com/relaxmind/app/features/auth/BiometricLockScreen.kt`
- `app/src/main/kotlin/com/relaxmind/app/features/auth/components/LoginFormCard.kt`
- `app/src/main/kotlin/com/relaxmind/app/features/auth/components/RegisterFormCard.kt`
- `app/src/main/kotlin/com/relaxmind/app/features/auth/components/LoginHeader.kt`
- `app/src/main/kotlin/com/relaxmind/app/features/auth/components/RegisterHeader.kt`

### Servicios de autenticacion

Archivo:

`app/src/main/kotlin/com/relaxmind/app/data/remote/FirebaseAuthService.kt`

Funciones:

- `register(email, password)`
- `login(email, password)`
- `loginWithGoogleCredential(idToken)`
- `logout()`
- `sendVerificationEmail()`
- `resetPassword(email)`
- `getCurrentUser()`
- `isLoggedIn()`

### ViewModel de Auth

`AuthViewModel.kt` implementa:

- Registro con validaciones.
- Creacion de perfil en Firestore segun rol.
- Login por email y password.
- Login con Google.
- Recuperacion de contraseña.
- Envio y reenvio de enlace de verificacion.
- Comprobacion de correo verificado.
- Actualizacion de avatar.
- Guardado de token FCM.
- Reactivacion de cuenta eliminada si esta dentro del periodo de 7 dias.

### Registro

Flujo:

1. Usuario completa nombre, apellido, fecha, correo, telefono, contraseña y rol.
2. Se validan campos con `ValidationUtils.kt`.
3. Se crea usuario en Firebase Auth.
4. Se crea documento en `patients` o `caregivers`.
5. Se envia correo de verificacion.
6. Se navega a verificacion.
7. Luego se configura avatar.
8. Si es paciente, continua al test inicial.
9. Si es cuidador, continua a permisos/notificaciones y dashboard.

### Login

Flujo:

1. Usuario ingresa correo y contraseña.
2. Firebase Auth valida credenciales.
3. Firestore resuelve rol con `getRoleById`.
4. Se verifica si la cuenta estaba eliminada y puede reactivarse.
5. Se guarda token FCM.
6. Se navega al dashboard correspondiente.

### Biometria

Archivos:

- `app/src/main/kotlin/com/relaxmind/app/features/auth/BiometricLockScreen.kt`
- `app/src/main/kotlin/com/relaxmind/app/utils/BiometricHelper.kt`
- `app/src/main/kotlin/com/relaxmind/app/utils/SecurityPreferences.kt`

La app puede bloquear acceso y pedir biometria al iniciar, con colorimetria segun rol.

## 7. Firebase y Vercel

### Firebase usado

Dependencias configuradas en:

`app/build.gradle.kts`

Firebase:

- `firebase-auth-ktx`
- `firebase-firestore-ktx`
- `firebase-storage-ktx`
- `firebase-messaging-ktx`

Tambien se aplica:

- `com.google.gms.google-services`

Archivo esperado:

- `app/google-services.json`

### FirestoreRepository

Archivo:

`app/src/main/kotlin/com/relaxmind/app/data/remote/FirestoreRepository.kt`

Es el repositorio central. Encapsula acceso a Firestore para que las pantallas y ViewModels no consulten directamente la base de datos.

Grupos de funciones:

Autenticacion y perfiles:

- `createPatient`
- `createCaregiver`
- `getPatientById`
- `getCaregiverById`
- `getLinkedCaregiverProfile`
- `updatePatient`
- `updateCaregiver`
- `updateFcmToken`
- `getRoleById`

Vinculacion:

- `createBindingCode`
- `listenToBindingCode`
- `deleteBindingCode`
- `previewPatientForBindingCode`
- `linkPatientWithCode`
- `listenPatientsForCaregiver`

Alertas:

- `listenAlertsForCaregiver`
- `listenAlertsForPatient`
- `createAlert`
- `updateAlertLocation`
- `listenToAlert`
- `getActiveCaregiverAlerts`
- `updateAlertResolved`
- `updateAlertFields`

Check-in y progreso:

- `createCheckIn`
- `getTodayCheckIn`
- `getLatestCheckIn`
- `getPatientCheckIns`
- `getPatientStreak`
- `updatePatientStreak`
- `getPatientAchievements`
- `unlockAchievement`

Meditacion:

- `getMeditationExercise`
- `createMeditationExercise`
- `getMeditationExercises`
- `createCompletedMeditation`
- `getCompletedMeditations`

Agenda:

- `createAppointment`
- `getAppointments`
- `getAppointment`
- `updateAppointmentCompletion`
- `updateAppointmentNotificationSent`
- `deleteAppointment`
- `getAppointmentsForMonth`

Diario:

- `createDiaryEntry`
- `getDiaryEntriesForMonth`
- `getDiaryEntries`
- `getDiaryEntriesCount`

Lumi:

- `getActiveLumiSession`
- `createLumiSession`
- `listenToLumiMessages`
- `addLumiMessage`
- `getLumiSessionsHistory`
- `archiveLumiSession`
- `updateLumiSessionTitle`
- `updateLumiSessionTitleIfDefault`

Biblioteca:

- `getLibraryArticles`
- `getArticleById`

### Colecciones principales

- `patients`
- `caregivers`
- `bindingCodes`
- `alerts`
- `checkIns`
- `streaks`
- `appointments`
- `diaryEntries`
- `lumiSessions`
- `libraryArticles`
- `completedMeditations`
- `userAchievements`

### FCM

FCM se usa para notificaciones push.

Archivos:

- `app/src/main/kotlin/com/relaxmind/app/MainActivity.kt`
- `app/src/main/kotlin/com/relaxmind/app/services/RelaxMindMessagingService.kt`
- `app/src/main/kotlin/com/relaxmind/app/services/NotificationUtils.kt`
- `app/src/main/kotlin/com/relaxmind/app/data/remote/NotificationApiService.kt`

`MainActivity.kt` obtiene token FCM y lo guarda en Firestore con `updateFcmToken`.

`RelaxMindMessagingService.kt` recibe mensajes push y los convierte en notificaciones locales/navegacion segun payload.

`NotificationUtils.kt` crea canales.

### Vercel

En `app/build.gradle.kts` existe:

`NOTIFICATIONS_BASE_URL`

Por defecto:

`https://relaxmind-notifications.vercel.app`

Servicio Android:

`app/src/main/kotlin/com/relaxmind/app/data/remote/NotificationApiService.kt`

Endpoints usados:

- `/api/send-sos-alert`
- `/api/send-wellness-alert`

Este servicio arma JSON y lo envia por HTTP con OkHttp. La idea es que Vercel actue como backend seguro para enviar push sin exponer credenciales administrativas en la app Android.

### Firebase Functions

Carpeta:

`functions/`

Archivo:

`functions/index.js`

Funciones:

- `sendSOSAlert`: trigger al crear documentos en `alerts/{alertId}`. Si el tipo es `sos` o `low_score`, obtiene el `fcmToken` del cuidador y envia notificacion.
- `checkDailyCheckIns`: tarea programada a las 23:59 America/Lima. Revisa pacientes enlazados sin check-in y genera alerta `no_checkin`.
- `sendCheckInReminder`: tarea programada a las 20:00 America/Lima. Recuerda al paciente hacer check-in si aun no lo hizo.

### Navegacion global

Archivo:

`app/src/main/kotlin/com/relaxmind/app/AppNavGraph.kt`

Responsabilidades:

- Define `sealed class Screen`.
- Decide rutas de auth, paciente, cuidador y comunes.
- Resuelve destino inicial con `resolveStartDestination`.
- Centraliza `NavHost`.
- Mantiene bottom nav fijo para tabs principales.
- Maneja FAB de cuidador para vincular paciente.
- Conecta pantallas con lambdas de navegacion.

### Tema y componentes compartidos

Archivos:

- `app/src/main/kotlin/com/relaxmind/app/ui/themes/ColorPalette.kt`
- `app/src/main/kotlin/com/relaxmind/app/ui/themes/Typography.kt`
- `app/src/main/kotlin/com/relaxmind/app/ui/themes/Theme.kt`
- `app/src/main/kotlin/com/relaxmind/app/ui/themes/ThemeState.kt`
- `app/src/main/kotlin/com/relaxmind/app/ui/components/RelaxButton.kt`
- `app/src/main/kotlin/com/relaxmind/app/ui/components/RelaxInputField.kt`
- `app/src/main/kotlin/com/relaxmind/app/ui/components/RelaxCard.kt`
- `app/src/main/kotlin/com/relaxmind/app/ui/components/RelaxBottomNav.kt`
- `app/src/main/kotlin/com/relaxmind/app/ui/components/PatientBottomNavigationBar.kt`
- `app/src/main/kotlin/com/relaxmind/app/ui/components/CaregiverBottomNavigationBar.kt`
- `app/src/main/kotlin/com/relaxmind/app/ui/components/SkeletonLoaders.kt`
- `app/src/main/kotlin/com/relaxmind/app/ui/components/RadarLoadingScreen.kt`
- `app/src/main/kotlin/com/relaxmind/app/ui/components/RadarLoaderOverlay.kt`
- `app/src/main/kotlin/com/relaxmind/app/ui/components/ScrollToTopEvents.kt`

Componentes importantes:

- Bottom nav fijo por rol.
- Skeletons por pantalla.
- Loader radar.
- Toasts visuales.
- Dialogos de permisos.
- Dialogos de desvinculacion.
- Header estandarizado.
- Comportamiento de tocar tab activo para volver arriba.

