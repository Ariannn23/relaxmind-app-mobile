# Resumen de cambios actuales - RelaxMind

## Dashboard paciente

- Se actualizo el dashboard con un estilo mas premium, tarjetas suaves y accesos rapidos.
- La tarjeta **Mi Cuidador** ahora muestra el estado de vinculacion.
- Se agrego modal con informacion del cuidador vinculado:
  - nombre completo
  - correo
  - fecha de nacimiento si existe
  - boton para llamar cuando hay telefono disponible
- Se corrigio la carga de datos del cuidador enlazado desde Firestore.
- Se reemplazaron iconos/emojis de accesos rapidos por assets locales:
  - `biblioteca.png`
  - `diario.png`
  - `centros_cercanos.png`
  - `cuidador.png`
  - `sonidos.png`
- Se agregaron mejoras visuales en cards de acceso rapido y se redujo el uso de iconos planos.

## Dashboard cuidador

- Se mejoro la vista principal del cuidador con colorimetria indigo/morada.
- Se mantiene el flujo de pacientes vinculados y alertas activas.
- Se corrigio el navbar para que use el mismo modelo visual del paciente, pero con:
  - Inicio
  - Pacientes
  - Alertas
  - Ajustes
- Se restauro el navbar fijo desde `AppNavGraph`, evitando que se destruya y reconstruya en cada cambio de tab.

## Navbar fijo

- El `RelaxBottomNav` ahora vive en el shell principal de `AppNavGraph`.
- Las pantallas principales ya no deben renderizar su propio navbar cuando se navega por tabs.
- Se desactivo el navbar interno con `showBottomNav = false` en pantallas principales.
- Esto evita saltos bruscos al cambiar entre pantallas del paciente o cuidador.

## Terminos y condiciones

- Se creo una pantalla nativa para **Terminos y condiciones**.
- Ya no depende de una redireccion externa.
- La pantalla usa estilo RelaxMind:
  - fondo suave segun rol
  - tarjeta blanca premium
  - secciones con texto estructurado
  - boton de regreso
- Se agrego ruta en `AppNavGraph`:
  - `common/terms-and-conditions/{role}`
- Desde ajustes de paciente y cuidador se navega a esta pantalla.

## Idioma / multiidioma

- Se implemento base de localizacion nativa Android:
  - `res/values/strings.xml`
  - `res/values-en/strings.xml`
- Se agregaron textos principales traducibles para ajustes, navbar y etiquetas comunes.
- Se agrego selector de idioma en ajustes:
  - Espanol
  - English
- El idioma se guarda en Firestore en el campo `language`.
- `ThemeState.language` actualiza el idioma activo en Compose.
- `MainActivity` aplica el locale seleccionado.
- Pendiente: migrar todas las pantallas restantes a `stringResource`, porque aun hay textos hardcodeados en algunas vistas.

## Pantalla de carga

- Se agrego una pantalla de carga custom para RelaxMind.
- Se integraron assets de carga:
  - `loaded.png`
  - `loaded3.png`
- Se reemplazaron varios indicadores antiguos por `FullScreenLoadingScreen`.
- Se corrigieron casos donde la carga se superponia sobre pantallas existentes.
- Pendiente: revisar pantallas antiguas que aun puedan usar el loader circular verde.

## Vinculacion paciente-cuidador

- Se reemplazaron pantallas antiguas:
  - `LinkCaregiverScreen`
  - `ScanQRScreen`
- Nuevas pantallas:
  - `PatientLinkCaregiverScreen`
  - `CaregiverLinkPatientScreen`
- Se agrego generacion de codigo/QR para vincular cuidador.
- Se agrego ingreso manual de codigo para cuidador.
- Se agrego validacion para que un cuidador pueda vincular varios pacientes.
- Se limito a maximo **5 pacientes por cuidador**.
- Se mejoro el manejo de errores para evitar cierres al abrir camara o vincular.

## Alertas y SOS

- Se mejoro el historial de alertas del cuidador.
- Se agregaron filtros:
  - Todos
  - SOS
  - Check-in bajo
  - Sin check-in
- Se agrego filtro por paciente y rango de fechas.
- Se limito la lista principal a las ultimas 10 alertas.
- Se corrigio que al marcar una alerta como resuelta la pantalla quedara en blanco.
- Se mejoro `SOSPatientViewModel` para crear alerta SOS aunque falle la ubicacion.
- Se agrego manejo estable de pantalla resuelta o alerta no disponible.

## Check-in diario

- Se agrego card de check-in diario debajo de bienestar.
- Si el check-in no fue hecho, aparece boton para iniciarlo.
- Si ya fue completado, aparece como listo.
- Se agregaron mejoras visuales y animaciones en resultado de check-in.

## Agenda

- Se refactorizo la agenda para dejar tabs principales:
  - Semana
  - Calendario
- Se corrigio visualizacion de eventos creados en otros dias.
- Se agrego indicador/punto en dias con eventos.
- Se agrego toast al crear un nuevo evento.
- Se mejoro la vista mensual y semanal con eventos y notas del diario.

## Progreso

- Se ajusto el calendario de progreso para recuperar numeros visibles.
- Se corrigio borde oscuro no deseado.
- Se redujeron tamanos y grosores para evitar que el calendario se vea apretado.
- Se agrego historial con opcion de ver mas y filtrar por mes.

## Cuidador - pacientes

- Se creo/mejoro lista de pacientes vinculados.
- Se mejoro detalle de paciente con tabs:
  - Progreso
  - Historial
  - Alertas SOS
- Se agrego vista de cuidador como lectura, sin edicion.
- Se ajusto el calendario del paciente para cuidador.
- Se mejoro historial para que sea mas compacto.

## Ajustes

- Se estandarizo header de pantalla con:
  - titulo grande
  - subtitulo
  - padding consistente
- Se aplico a pantallas como ajustes, progreso, meditar, pacientes y alertas.
- Se corrigio la pantalla de editar perfil del cuidador:
  - datos cargados desde Firestore
  - colorimetria morada
  - permisos y guardado revisados

## Componentes nuevos o modificados

- `ScreenHeader`
- `RelaxBottomNav`
- `FullScreenLoadingScreen`
- `SkeletonLoaders`
- `AchievementUnlockedDialog`
- `TermsAndConditionsScreen`
- `PatientLinkCaregiverScreen`
- `CaregiverLinkPatientScreen`
- `LibraryScreen`
- `ArticleDetailScreen`
- `RelaxSoundsScreen`

## Estado actual

- Rama actual: `main`
- Ultima compilacion verificada:
  - `./gradlew.bat :app:assembleDebug --console=plain`
  - Resultado: `BUILD SUCCESSFUL`

## Pendientes

- Migrar todos los textos hardcodeados a `stringResource`.
- Revisar loaders restantes que puedan usar el indicador antiguo.
- Probar en emulador el navbar fijo en paciente y cuidador.
- Probar vinculacion de hasta 5 pacientes por cuidador.
- Validar reglas de Firestore para:
  - multiples pacientes por cuidador
  - actualizacion de alertas SOS
  - edicion de perfil del cuidador
