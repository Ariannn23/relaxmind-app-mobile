# RelaxMind - Plan de trabajo por sprints en 14 semanas

Este documento divide el desarrollo de RelaxMind en 14 semanas. Cada semana puede contener uno o mas sprints o subsprints. La estructura esta pensada para presentar el proceso de trabajo, explicar que se construyo en cada etapa y relacionarlo con los modulos finales de la aplicacion.

## Semana 1 - Sprint 0: Configuracion inicial y base del proyecto

### Sprint 0.1 - Estructura base

Objetivo: crear el esqueleto del proyecto Android con Kotlin, Jetpack Compose y Firebase.

Trabajo realizado:

- Configuracion base del modulo `app`.
- Min SDK y target SDK.
- Estructura de carpetas:
  - `ui/components`
  - `ui/themes`
  - `ui/animations`
  - `features/auth`
  - `features/patient`
  - `features/caregiver`
  - `features/common`
  - `data/remote`
  - `data/model`
  - `utils`
- Integracion de dependencias principales:
  - Compose Material 3.
  - Navigation Compose.
  - Firebase Auth, Firestore, Storage, Messaging.
  - Maps Compose.
  - Location.
  - Lottie.
  - Coil.
  - ZXing.
  - CameraX.
  - ML Kit barcode scanning.
- Permisos del `AndroidManifest`:
  - Internet.
  - Ubicacion.
  - Camara.
  - Biometria.
  - Notificaciones.
  - Lectura de imagenes.

Archivos principales:

- `app/build.gradle.kts`
- `settings.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/kotlin/com/relaxmind/app/MainActivity.kt`

### Sprint 0.2 - Sistema de diseno

Objetivo: definir paleta, tipografia y tema global.

Trabajo realizado:

- Paleta de paciente en verde.
- Paleta de cuidador en indigo/morado.
- Color SOS coral.
- Fondos claros y oscuros.
- Tipografias Outfit, Urbanist, Montserrat y Lexend.
- Tema claro y oscuro.
- Estado global de tema e idioma.

Archivos:

- `ui/themes/ColorPalette.kt`
- `ui/themes/Typography.kt`
- `ui/themes/Theme.kt`
- `ui/themes/ThemeState.kt`
- `ui/themes/WellnessPalette.kt`

### Sprint 0.3 - Componentes base

Objetivo: crear componentes reutilizables para mantener consistencia visual.

Trabajo realizado:

- Botones primarios, outline y destructivos.
- Inputs estilizados.
- Cards.
- Top bars.
- Bottom nav.
- Loading indicators.
- Skeletons.
- Toasts.
- Dialogos reutilizables.

Archivos:

- `ui/components/RelaxButton.kt`
- `ui/components/RelaxInputField.kt`
- `ui/components/RelaxCard.kt`
- `ui/components/RelaxTopBar.kt`
- `ui/components/RelaxBottomNav.kt`
- `ui/components/PatientBottomNavigationBar.kt`
- `ui/components/CaregiverBottomNavigationBar.kt`
- `ui/components/SkeletonLoaders.kt`
- `ui/components/RelaxToast.kt`

## Semana 2 - Sprint 1: Onboarding, login y registro

### Sprint 1.1 - Modelos y servicios Firebase

Objetivo: implementar modelos principales y servicios de autenticacion.

Trabajo realizado:

- Modelo `Patient`.
- Modelo `Caregiver`.
- Servicio `FirebaseAuthService`.
- Repositorio `FirestoreRepository`.
- Registro de usuarios por rol.
- Login.
- Logout.
- Recuperacion de contraseña.
- Verificacion de correo.
- Obtencion de rol desde Firestore.

Archivos:

- `data/model/Patient.kt`
- `data/model/Caregiver.kt`
- `data/remote/FirebaseAuthService.kt`
- `data/remote/FirestoreRepository.kt`

### Sprint 1.2 - Login

Objetivo: crear pantalla de inicio de sesion moderna.

Trabajo realizado:

- UI del login.
- Campo de correo.
- Campo de contraseña.
- Mantener sesion.
- Login con Google.
- Recuperar contraseña.
- Navegacion por rol.
- Soporte de biometria.

Archivos:

- `features/auth/LoginScreen.kt`
- `features/auth/components/LoginFormCard.kt`
- `features/auth/components/LoginHeader.kt`
- `features/auth/components/LoginHeroIllustration.kt`

### Sprint 1.3 - Registro

Objetivo: crear flujo de creacion de cuenta.

Trabajo realizado:

- Formulario de registro.
- Seleccion de rol paciente/cuidador.
- Validaciones de nombre, email, contraseña, telefono y fecha.
- Barra de fuerza de contraseña.
- Mensajes de error por campo.
- Prohibicion de numeros en nombres.
- Eliminacion de emojis del diseño.
- Creacion de documento en Firestore segun rol.

Archivos:

- `features/auth/RegisterScreen.kt`
- `features/auth/components/RegisterFormCard.kt`
- `features/auth/components/RegisterHeader.kt`
- `utils/ValidationUtils.kt`
- `features/auth/AuthViewModel.kt`

### Sprint 1.4 - Verificacion y post-registro

Objetivo: completar el onboarding luego del registro.

Trabajo realizado:

- Pantalla de verificacion por enlace de correo.
- Reenvio de enlace.
- Timer.
- Avatar setup.
- Permisos de notificaciones.
- Flujo paciente: avatar -> test inicial.
- Flujo cuidador: avatar -> permisos -> dashboard.

Archivos:

- `features/auth/EmailVerificationScreen.kt`
- `features/auth/AvatarSetupScreen.kt`
- `features/auth/NotificationPermissionScreen.kt`
- `features/auth/AuthViewModel.kt`

## Semana 3 - Sprint 2: Dashboard paciente y check-in

### Sprint 2.1 - Dashboard del paciente

Objetivo: crear pantalla principal del paciente.

Trabajo realizado:

- Header con saludo y avatar.
- Card "Mi bienestar hoy".
- Card de check-in diario.
- Seccion "Para ti hoy".
- Accesos rapidos.
- Mi diario.
- Lumi.
- Mi cuidador.
- Centros cercanos.
- Boton SOS flotante.
- Modo oscuro inicial para dashboard.

Archivos:

- `features/patient/DashboardPatientScreen.kt`
- `features/patient/PatientViewModel.kt`
- `ui/components/PatientBottomNavigationBar.kt`

### Sprint 2.2 - Check-in diario

Objetivo: permitir que el paciente registre su bienestar diario.

Trabajo realizado:

- Formulario diario breve.
- Reutilizacion del flujo para test inicial.
- Calculo de puntaje 0-100.
- Guardado en Firestore.
- Resultado animado.
- Actualizacion de racha.
- Activacion de logros.
- Alerta al cuidador si score <= 30.

Archivos:

- `features/common/CheckInScreen.kt`
- `features/common/CheckInViewModel.kt`
- `features/common/WellnessResultScreen.kt`
- `utils/WellnessScoreCalculator.kt`
- `data/model/CheckIn.kt`

## Semana 4 - Sprint 3: Progreso, rachas y logros

### Sprint 3.1 - Pantalla de progreso

Objetivo: visualizar la evolucion del paciente.

Trabajo realizado:

- Calendario mensual con colores por puntaje.
- Leyenda de categorias.
- Racha actual.
- Mejor racha.
- Historial de check-ins.
- Link "Ver todo".
- Filtros por mes en historial.
- Ajustes visuales de tamaño y espaciado.
- Skeleton de progreso.

Archivos:

- `features/patient/ProgressScreen.kt`
- `ui/components/WellnessCalendarGrid.kt`
- `ui/components/ProgressChart.kt`
- `ui/components/ProgressEmptyState.kt`
- `data/model/Streak.kt`

### Sprint 3.2 - Logros

Objetivo: motivar al paciente con recompensas.

Trabajo realizado:

- Catalogo de logros.
- Desbloqueo por primer check-in.
- Desbloqueo por puntajes altos.
- Desbloqueo por rachas.
- Modal/pantalla de logro desbloqueado.

Archivos:

- `features/patient/AchievementCatalog.kt`
- `features/patient/AchievementManager.kt`
- `features/patient/AchievementLibraryScreen.kt`
- `features/patient/AchievementUnlockedScreen.kt`
- `data/model/UserAchievement.kt`

## Semana 5 - Sprint 4: Meditacion, sonidos y biblioteca

### Sprint 4.1 - Meditacion

Objetivo: ofrecer ejercicios de respiracion y mindfulness.

Trabajo realizado:

- Lista de ejercicios.
- Meta diaria.
- Detalle de meditacion.
- Progreso por ejercicio.
- Registro de meditaciones completadas.
- Seed de ejercicios por defecto.
- Skeleton de meditacion basado en cards.

Archivos:

- `features/patient/MeditateScreen.kt`
- `features/patient/MeditationDetailScreen.kt`
- `data/model/MeditationExercise.kt`
- `data/model/CompletedMeditation.kt`

### Sprint 4.2 - Sonidos relajantes

Objetivo: permitir reproducir sonidos para relajacion.

Trabajo realizado:

- Pantalla de sonidos.
- Reproductor con Media3/ExoPlayer.
- Opcion de repeticion para audios cortos.
- Manager central de sonido.

Archivos:

- `features/patient/RelaxSoundsScreen.kt`
- `utils/SoundPlayerManager.kt`

### Sprint 4.3 - Biblioteca

Objetivo: mostrar articulos de apoyo.

Trabajo realizado:

- Lista de articulos.
- Detalle de articulo.
- Datos mock.
- Soporte por rol.

Archivos:

- `features/common/LibraryScreen.kt`
- `features/common/ArticleDetailScreen.kt`
- `data/local/MockLibraryData.kt`
- `data/model/LibraryArticle.kt`

## Semana 6 - Sprint 5: Diario y collage

### Sprint 5.1 - Diario del paciente

Objetivo: permitir registrar emociones y recuerdos del dia.

Trabajo realizado:

- Pantalla principal del diario.
- Nueva entrada.
- Categoria.
- Emocion.
- Texto.
- Fotos opcionales.
- Guardado en Firestore.
- Collage de imagenes.
- Visualizacion por dia.

Archivos:

- `features/patient/DiaryScreen.kt`
- `features/patient/DiaryEntryScreen.kt`
- `features/patient/DiaryDayEntriesScreen.kt`
- `data/model/DiaryEntry.kt`

### Sprint 5.2 - Integracion Diario + Agenda

Objetivo: mostrar entradas de diario dentro de la agenda.

Trabajo realizado:

- Entradas visibles en calendario mensual.
- BottomSheet con notas y fotos.
- Mosaico fotografico.

Archivos:

- `features/patient/ScheduleScreen.kt`
- `data/remote/FirestoreRepository.kt`

## Semana 7 - Sprint 6: Agenda, recordatorios y notificaciones locales

### Sprint 6.1 - Agenda

Objetivo: gestionar eventos y recordatorios.

Trabajo realizado:

- Vista semanal.
- Vista calendario.
- Crear evento.
- Detalle de evento.
- Completar evento.
- Eliminar evento.
- Toast al crear evento.
- Indicadores de eventos por dia.

Archivos:

- `features/patient/ScheduleScreen.kt`
- `features/patient/CreateAppointmentScreen.kt`
- `features/patient/AppointmentDetailScreen.kt`
- `data/model/Appointment.kt`

### Sprint 6.2 - Recordatorios

Objetivo: programar alarmas y notificaciones.

Trabajo realizado:

- Recordatorio antes del evento.
- Repeticion semanal.
- Selector de dias.
- Reprogramacion al reiniciar.
- Canales de notificacion.

Archivos:

- `utils/ReminderManager.kt`
- `services/AppointmentAlarmReceiver.kt`
- `services/CheckInAlarmReceiver.kt`
- `services/BootReceiver.kt`
- `services/NotificationUtils.kt`

## Semana 8 - Sprint 7: Vinculacion paciente-cuidador

### Sprint 7.1 - Codigo y QR del paciente

Objetivo: permitir que el paciente genere codigo para vincular cuidador.

Trabajo realizado:

- QR con ZXing.
- Codigo de 6 digitos.
- Timer de expiracion.
- Nuevo codigo al expirar.
- Escucha en tiempo real del binding code.

Archivos:

- `features/patient/PatientLinkCaregiverScreen.kt`
- `features/patient/LinkCaregiverViewModel.kt`
- `data/model/BindingCode.kt`

### Sprint 7.2 - Escaneo del cuidador

Objetivo: permitir que el cuidador se vincule con un paciente.

Trabajo realizado:

- CameraX.
- ML Kit barcode scanning.
- Entrada manual de codigo.
- Confirmacion previa con datos basicos del paciente.
- Limite de 5 pacientes.
- Persistencia de estado de confirmacion.
- Manejo de codigos vencidos.

Archivos:

- `features/caregiver/CaregiverLinkPatientScreen.kt`
- `features/caregiver/CaregiverViewModel.kt`
- `data/remote/FirestoreRepository.kt`

## Semana 9 - Sprint 8: Dashboard cuidador, pacientes y alertas

### Sprint 8.1 - Dashboard cuidador

Objetivo: construir el inicio del cuidador.

Trabajo realizado:

- Header con nombre y avatar.
- Alertas activas.
- Card de pacientes.
- Boton flotante de agregar paciente.
- Fondo degradado morado.
- Navbar del cuidador.
- Skeletons para evitar parpadeo.

Archivos:

- `features/caregiver/DashboardCaregiverScreen.kt`
- `ui/components/CaregiverAddPatientButton.kt`
- `ui/components/CaregiverBottomNavigationBar.kt`

### Sprint 8.2 - Lista y detalle de pacientes

Objetivo: consultar pacientes vinculados.

Trabajo realizado:

- Lista con busqueda.
- Cards por paciente.
- Detalle de paciente.
- Contacto.
- Progreso.
- Historial.
- Desvinculacion segura.

Archivos:

- `features/caregiver/PatientsListScreen.kt`
- `features/caregiver/PatientDetailScreen.kt`
- `features/caregiver/CaregiverViewModel.kt`

### Sprint 8.3 - Historial de alertas

Objetivo: consultar y resolver alertas.

Trabajo realizado:

- Lista de alertas.
- Filtros por tipo.
- Filtro por paciente.
- Rango de fechas.
- Ultimas 10 alertas.
- Detalles.
- Resolver alerta.
- Flujo para no check-in y bajo bienestar.

Archivos:

- `features/caregiver/AlertsHistoryScreen.kt`
- `features/caregiver/SOSAlertScreen.kt`
- `features/caregiver/SOSAlertViewModel.kt`
- `ui/components/MissedCheckInDialog.kt`

## Semana 10 - Sprint 9: SOS y seguridad

### Sprint 9.1 - SOS paciente

Objetivo: crear alerta de emergencia.

Trabajo realizado:

- Boton SOS flotante.
- Animacion de pulso.
- Creacion de alerta.
- Ubicacion.
- Notificacion push.
- Cancelacion de alerta.
- Escucha de resolucion.

Archivos:

- `features/patient/SOSPatientScreen.kt`
- `features/patient/SOSPatientViewModel.kt`
- `data/remote/NotificationApiService.kt`

### Sprint 9.2 - SOS cuidador

Objetivo: atender alerta de emergencia.

Trabajo realizado:

- Detalle de alerta.
- Llamar paciente.
- Ver ruta.
- Compartir.
- Resolver.
- Estados no disponible, cancelada y resuelta.

Archivos:

- `features/caregiver/SOSAlertScreen.kt`
- `features/caregiver/SOSAlertViewModel.kt`

## Semana 11 - Sprint 10: Ajustes, perfil, idioma y terminos

### Sprint 10.1 - Ajustes paciente y cuidador

Objetivo: permitir administrar cuenta y preferencias.

Trabajo realizado:

- Perfil.
- Modo oscuro.
- Idioma.
- Notificaciones.
- Biometria.
- Cerrar sesion.
- Eliminar cuenta.
- Desvincular cuidador/paciente.

Archivos:

- `features/patient/SettingsPatientScreen.kt`
- `features/caregiver/SettingsCaregiverScreen.kt`
- `features/patient/EditProfileScreen.kt`
- `features/caregiver/EditProfileCaregiverScreen.kt`

### Sprint 10.2 - Terminos y condiciones

Objetivo: crear pantalla nativa de TyC.

Trabajo realizado:

- Pantalla nativa.
- Color por rol.
- Secciones numeradas.
- Navegacion desde ajustes paciente y cuidador.

Archivos:

- `features/common/TermsAndConditionsScreen.kt`
- `AppNavGraph.kt`

### Sprint 10.3 - Idioma

Objetivo: preparar localizacion.

Trabajo realizado:

- Strings en recursos.
- Cambio de idioma desde ajustes.
- Reactividad con `ThemeState.language`.

Archivos:

- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-en/strings.xml`
- `MainActivity.kt`
- `ui/themes/ThemeState.kt`

## Semana 12 - Sprint 11: Firebase, Vercel, push y backend

### Sprint 11.1 - Firebase

Objetivo: consolidar backend de datos.

Trabajo realizado:

- Auth.
- Firestore.
- Storage.
- FCM.
- Reglas de acceso.
- Tokens.
- Escuchas en tiempo real.

Archivos:

- `data/remote/FirebaseAuthService.kt`
- `data/remote/FirestoreRepository.kt`
- `MainActivity.kt`
- `services/RelaxMindMessagingService.kt`

### Sprint 11.2 - Vercel y notificaciones

Objetivo: enviar push desde backend externo.

Trabajo realizado:

- `NOTIFICATIONS_BASE_URL`.
- Servicio HTTP con OkHttp.
- Endpoint para SOS.
- Endpoint para bajo bienestar.

Archivos:

- `data/remote/NotificationApiService.kt`
- `app/build.gradle.kts`

### Sprint 11.3 - Firebase Functions

Objetivo: automatizar alertas y recordatorios.

Trabajo realizado:

- Trigger al crear alertas.
- Recordatorio diario de check-in.
- Deteccion de no check-in.
- Envio de FCM desde backend.

Archivos:

- `functions/index.js`
- `functions/package.json`

## Semana 13 - Sprint 12: Pulido visual, loaders y animaciones

### Sprint 12.1 - Transiciones y navbar fijo

Objetivo: mejorar experiencia entre tabs.

Trabajo realizado:

- Navbar fijo paciente/cuidador.
- Evitar destruir y reconstruir pantalla en tabs principales.
- Transiciones suaves.
- Tocar tab activo vuelve al inicio.
- FAB del cuidador corregido sobre navbar.

Archivos:

- `AppNavGraph.kt`
- `ui/components/PatientBottomNavigationBar.kt`
- `ui/components/CaregiverBottomNavigationBar.kt`
- `ui/components/RelaxBottomNav.kt`
- `ui/components/ScrollToTopEvents.kt`

### Sprint 12.2 - Skeletons y loaders

Objetivo: evitar pantallas vacias mientras cargan datos.

Trabajo realizado:

- Skeleton de dashboard.
- Skeleton de pacientes.
- Skeleton de alertas.
- Skeleton de meditacion.
- Skeleton de progreso.
- Skeleton de agenda.
- Loader radar.

Archivos:

- `ui/components/SkeletonLoaders.kt`
- `ui/components/RadarLoadingScreen.kt`
- `ui/components/RadarLoaderOverlay.kt`

### Sprint 12.3 - Animaciones y feedback

Objetivo: dar vida a la app sin exagerar.

Trabajo realizado:

- Pulso del boton SOS.
- Animaciones en cards.
- Transiciones de navbar.
- Resultado de check-in.
- Haptics en acciones sensibles.

Archivos:

- `features/patient/DashboardPatientScreen.kt`
- `features/common/WellnessResultScreen.kt`
- `ui/components/AchievementUnlockedDialog.kt`

## Semana 14 - Sprint 13: Cierre, QA y preparacion de exposicion

### Sprint 13.1 - Pruebas de flujos completos

Objetivo: validar la app de extremo a extremo.

Flujos revisados:

- Registro paciente.
- Registro cuidador.
- Login.
- Recuperar contraseña.
- Avatar.
- Test inicial.
- Check-in diario.
- Progreso.
- Agenda.
- SOS.
- Vinculacion.
- Desvinculacion.
- Alertas.
- Terminos y condiciones.
- Idioma.
- Notificaciones.
- Eliminacion y reactivacion de cuenta.

### Sprint 13.2 - Correcciones finales

Objetivo: resolver detalles visuales y de comportamiento.

Trabajo realizado:

- Ajustes de colores por rol.
- Correccion de modales.
- Correccion de botones con saltos de linea.
- Correccion de permisos Firebase.
- Correccion de loaders.
- Correccion de parpadeos de datos.
- Correccion de pantalla no disponible.
- Correccion de historial de alertas.

### Sprint 13.3 - Documentacion

Objetivo: dejar material listo para explicar el proyecto.

Documentos:

- `docs/exposicion_modulos_relaxmind.md`
- `docs/plan_sprints_14_semanas.md`

Contenido documentado:

- Check-in y progreso.
- Lumi y centros cercanos.
- Agenda y editar perfil.
- SOS e historial de alertas.
- Dashboard cuidador y lista de pacientes.
- Login y registro.
- Firebase y Vercel.
- Sprints por semana.

## Resumen final del proceso

RelaxMind se desarrollo de forma incremental:

1. Primero se construyo la base tecnica y visual.
2. Luego se implemento autenticacion y roles.
3. Despues se creo la experiencia del paciente.
4. Se agrego progreso, agenda, diario, meditacion y Lumi.
5. Se construyo el rol cuidador.
6. Se implemento vinculacion y alertas.
7. Se conectaron notificaciones, Firebase y backend.
8. Finalmente se hizo pulido visual, skeletons, loaders, animaciones y documentacion.

La arquitectura final sigue MVVM:

- Pantallas Compose: UI.
- ViewModels: estado y logica de presentacion.
- Repositorios: acceso a Firebase y servicios.
- Modelos: estructura de datos.
- Servicios: notificaciones, ubicacion, audio, biometria.
- Componentes compartidos: diseño consistente.

