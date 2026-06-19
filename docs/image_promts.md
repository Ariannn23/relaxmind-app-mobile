# RelaxMind — Prompts de Generación de Imágenes e Íconos
> Prompts completos para TODAS las pantallas del flujo. Usa en Midjourney, DALL·E 3, Leonardo AI o Stable Diffusion.  
> Un prompt de mockup UI por pantalla + prompts de ilustraciones, íconos y animaciones.

---

## ESTILO VISUAL BASE PRIMARIA

```
Estilo global RelaxMind — incluir en todos los prompts de ilustraciones:
- Flat design con formas orgánicas suaves, líneas redondeadas, sombras suaves
- Paleta: verde #0F6E56, verde claro #68D391, índigo #4338A8, coral #E8582A, fondos blancos y cremas
- Personas: abstractas/minimalistas, siluetas simples, diversas
- Ambiente: tranquilo, reconfortante, hopeful — nunca oscuro ni angustiante
- Estilo similar a Headspace / Calm app
- Formato: PNG con fondo transparente salvo que se indique fondo
```

---

## PARTE 1 — MOCKUPS UI POR PANTALLA (flujo completo)

> Úsalos en Figma AI, Galileo AI, Uizard o cualquier generador de UI como referencia de diseño.
> Colores exactos: paciente = #0F6E56 verde | cuidador = #4338A8 índigo | SOS = #E8582A coral

---

### FLUJO COMÚN — ONBOARDING Y AUTENTICACIÓN

---

#### 01. WelcomeScreen — Slide 1 de 3

```
Mobile app onboarding screen design, Android UI, wellness mental health app called RelaxMind,
slide 1 of 3 onboarding, white background with soft green gradient at top,
large illustration area center: serene person sitting on a green hill holding a phone with a wellness dashboard,
floating icons around them: sun, leaf, heart, checkmark in soft green tones,
below the illustration:
- page indicator dots: 3 dots, first one filled green #0F6E56, others gray
- title text: "Tu bienestar, cada día" in Outfit Bold dark gray 24sp
- subtitle: "Registra cómo te sientes y construye hábitos saludables paso a paso." Urbanist 16sp light gray
- "Omitir" text button top-right corner
- "Siguiente" green filled button at bottom #0F6E56
Material Design 3, rounded corners, clean and modern, calm wellness aesthetic
```

#### 02. WelcomeScreen — Slide 2 de 3

```
Mobile app onboarding screen design, Android UI, wellness mental health app RelaxMind,
slide 2 of 3, white background,
large illustration center: abstract peaceful figure in meditation pose surrounded by soft concentric breathing circles,
floating leaves and gentle wave shapes around them in green and lavender,
below:
- page indicator: 3 dots, second filled green #0F6E56
- title: "Mindfulness y respiración" Outfit Bold dark 24sp
- subtitle: "Ejercicios guiados para calmar tu mente cuando más lo necesitas." Urbanist 16sp gray
- "Omitir" top-right, "Siguiente" green button bottom
Material Design 3, calm aesthetic
```

#### 03. WelcomeScreen — Slide 3 de 3

```
Mobile app onboarding screen design, Android UI, wellness mental health app RelaxMind,
slide 3 of 3, white background,
large illustration center: two abstract human figures side by side, one in green (patient) one in indigo #4338A8 (caregiver),
a glowing connection line or heart between them, subtle phone icons around them,
below:
- page indicator: 3 dots, third filled green
- title: "Siempre acompañado" Outfit Bold dark 24sp
- subtitle: "Tu cuidador siempre conectado para estar ahí cuando lo necesites." Urbanist 16sp gray
- "Comenzar" large green filled button at bottom (no skip button here)
Material Design 3, warm and supportive aesthetic
```

#### 04. LoginScreen

```
Mobile app login screen design, Android UI, wellness app RelaxMind, white background,
top area: circular green logo placeholder + "RelaxMind" app name,
title: "Bienvenido de nuevo" Outfit Bold 28sp dark,
subtitle: "Ingresa a tu cuenta" Urbanist 16sp light gray,
form fields in middle:
- email input field with floating label "Correo electrónico", envelope icon left
- password input field "Contraseña", lock icon left, eye icon right
- row below: small switch toggle + label "Mantener sesión iniciada"
- "Iniciar sesión" green filled button #0F6E56 full width rounded
- "¿Olvidaste tu contraseña?" text button centered below
- thin divider with "o" centered
- "Crear cuenta" outline text button below
fingerprint icon below for biometric hint,
Material Design 3, OutlinedTextField style, clean and professional
```

#### 05. RegisterScreen

```
Mobile app registration screen design, Android UI, wellness app RelaxMind, white background,
top bar with back arrow + title "Crear cuenta" Outfit Bold 26sp,
scrollable form:
- "Nombre" outlined text field
- "Apellidos" outlined text field
- "Fecha de nacimiento" field with calendar icon (shows date picker on tap)
- "Correo electrónico" field
- "Contraseña" field with eye toggle
- "Confirmar contraseña" field
role selector section below fields:
- two horizontal cards side by side: "Paciente" card with green border and green icon + "Cuidador" card with indigo border,
  selected card has colored background tint and bold label
- "Paciente" card is selected (green #0F6E56 border, soft green background)
- checkbox row: "Acepto los términos y condiciones" with link underline
- "Registrarme" large green button at bottom (disabled if checkbox unchecked)
Material Design 3, clean registration form, rounded inputs 12dp
```

#### 06. EmailVerificationScreen

```
Mobile app email verification screen design, Android UI, wellness app RelaxMind, white background,
top bar with back arrow,
center illustration: large rounded envelope icon with small sparkles, green color #0F6E56,
title: "Verifica tu correo" Outfit Bold 24sp,
subtitle: "Ingresa el código de 6 dígitos enviado a tu correo" Urbanist 16sp gray,
OTP input row: 6 individual square boxes 52x52dp each, rounded corners 8dp,
boxes styled: green border when focused/filled, gray border when empty,
digits "1", "4", "3", "8", "2", " " shown (last box empty and focused),
below OTP boxes:
- "El código expira en 1:48" countdown text in red
- "Reenviar código (2/5 intentos)" text button in gray
- "Verificar" green filled button at bottom
Material Design 3, OTP style input
```

#### 07. AvatarSetupScreen

```
Mobile app avatar selection screen design, Android UI, wellness app RelaxMind, white background,
top bar with back arrow,
title: "Elige tu avatar" Outfit Bold 24sp,
subtitle: "Puedes cambiarlo después en ajustes" Urbanist 14sp gray,
4-column grid of 12 avatar circles, each 72dp diameter:
row 1: green avatar selected (larger scale 1.08, green border 3dp), orange avatar, purple avatar, teal avatar
row 2: pink avatar, blue avatar, yellow avatar, red avatar
row 3: mint avatar, coral avatar, lavender avatar, warm beige avatar
selected avatar has green ring border and is slightly enlarged,
bottom: "Continuar" green button + "Omitir" text button below,
clean grid layout, Material Design 3
```

#### 08. NotificationPermissionScreen

```
Mobile app notification permission screen design, Android UI, wellness app RelaxMind, white background,
center illustration: large friendly bell icon with soft notification dots floating from it, green tones,
title: "Mantente al día" Outfit Bold 24sp,
body text: 3 short bullet points explaining why notifications matter:
"✓ Recordatorio de check-in diario a las 8PM"
"✓ Alertas antes de tus citas médicas"  
"✓ Avisos importantes de tu cuidador"
in Urbanist 16sp gray,
"Permitir notificaciones" large green filled button #0F6E56,
"Ahora no" small text button below in gray,
clean centered layout, welcoming and non-intrusive
```

#### 09. ForgotPasswordScreen

```
Mobile app forgot password screen design, Android UI, wellness app RelaxMind, white background,
top bar with back arrow + title "Recuperar contraseña",
center illustration: envelope with lock icon, soft green tones,
title: "¿Olvidaste tu contraseña?" Outfit Bold 22sp,
subtitle: "Ingresa tu correo y te enviaremos un enlace para restablecerla" Urbanist 16sp gray,
"Correo electrónico" outlined text field with envelope icon,
"Enviar enlace" green filled button below,
small note below button: "Revisa también tu carpeta de spam" Urbanist 14sp gray,
Material Design 3, clean and simple
```

---

### FLUJO PACIENTE

---

#### 10. InitialTestScreen — Bienvenida al test

```
Mobile app initial wellness test intro screen, Android UI, RelaxMind, white background,
green theme #0F6E56,
soft illustration at top: person with clipboard or thought bubbles,
title: "Cuéntanos cómo te sientes" Outfit Bold 24sp,
subtitle: "Este test nos ayuda a personalizar tu experiencia. Solo toma 2 minutos." Urbanist 16sp gray,
progress indicator showing step 0 of 7 (empty progress bar),
"Comenzar test" large green filled button,
"Omitir por ahora" text button below in gray,
calm and inviting design, no pressure aesthetic
```

#### 11. CheckInScreen — Paso 1: Estado Emocional

```
Mobile app wellness check-in step screen, Android UI, RelaxMind, white background, green theme,
progress bar at very top: 1/7 filled green strip,
"Paso 1 de 7" small label in gray,
title: "¿Cómo te has sentido últimamente?" Outfit SemiBold 20sp,
5 vertical cards stacked, each card: large emoji left + label text right:
card 1: "😭 Muy mal" — gray border, white background
card 2: "😕 Mal" — gray border, white background
card 3: "😐 Bien" — GREEN border #0F6E56, soft green tint background — SELECTED with checkmark
card 4: "🙂 Muy bien" — gray border, white background
card 5: "😄 Excelente" — gray border, white background
bottom row: "Anterior" text button left + "Siguiente" green filled button right,
Material Design 3, rounded cards 12dp
```

#### 12. CheckInScreen — Paso 2: Sueño

```
Mobile app wellness check-in step screen, Android UI, RelaxMind, white background, green theme,
progress bar at top: 2/7 filled,
title: "¿Cómo dormiste anoche?" Outfit SemiBold 20sp,
5 vertical cards:
"😴 Pésimo" | "😪 Mal" | "😑 Regular" | "😌 Bien" — SELECTED green | "😊 Excelente"
selected card: green border and tint, check icon,
bottom: "Anterior" text + "Siguiente" green button,
same clean card layout as step 1
```

#### 13. CheckInScreen — Paso 3: Energía (Slider)

```
Mobile app wellness check-in step screen, Android UI, RelaxMind, white background, green theme,
progress bar at top: 3/7 filled,
title: "¿Cuánta energía sientes hoy?" Outfit SemiBold 20sp,
subtitle: "Desliza para indicar tu nivel" Urbanist 14sp gray,
large number "7" centered below title, Outfit Bold 72sp in green #0F6E56,
horizontal slider below: 1 to 10, thumb at position 7, track colored left=green right=gray,
labels "1 Muy poca" left and "10 Mucha" right in small gray text,
bottom: "Anterior" text + "Siguiente" green button,
clean spacious layout
```

#### 14. CheckInScreen — Paso 4: Estrés (Slider)

```
Mobile app wellness check-in step screen, Android UI, RelaxMind, white background,
progress bar at top: 4/7 filled,
title: "¿Cuánto estrés sientes?" Outfit SemiBold 20sp,
large number "4" centered, Outfit Bold 72sp in orange #ED8936 (medium stress color),
horizontal slider: 1 to 10, thumb at 4, track gradient left=green to right=red,
labels "1 Sin estrés" left and "10 Mucho" right in small gray,
bottom: "Anterior" text + "Siguiente" green button
```

#### 15. CheckInScreen — Paso 5: Frecuencia de Hábitos

```
Mobile app wellness check-in step screen, Android UI, RelaxMind, white background, green theme,
progress bar at top: 5/7 filled,
title: "¿Con qué frecuencia...?" Outfit SemiBold 20sp,
4 question rows, each with:
- question text in dark gray Urbanist 14sp
- row of 5 small chip buttons: "Nunca" | "Casi nunca" | "A veces" | "Casi siempre" | "Siempre"
question 1 "¿Realizas actividad física?" — "A veces" chip selected (green filled, white text)
question 2 "¿Mantienes contacto social?" — "Casi siempre" chip selected
question 3 "¿Disfrutas actividades tuyas?" — "Siempre" chip selected
question 4 "¿Sigues una rutina diaria?" — no selection yet (all chips outlined gray)
bottom: "Anterior" text + "Siguiente" green button,
chip style: selected=green fill white text, unselected=gray border white background
```

#### 16. CheckInScreen — Paso 6: Preguntas Sí/No (Swipe)

```
Mobile app wellness check-in swipe card screen, Android UI, RelaxMind, white background, green theme,
progress bar at top: 6/7 filled,
title: "¿En el último tiempo...?" Outfit SemiBold 20sp,
subtitle: "Desliza la tarjeta → para Sí, ← para No" Urbanist 14sp gray,
large swipe card center:
card slightly tilted right (being swiped right), green glow border, 
"SÍ" label top-right corner in green bold,
question text centered: "¿Has podido concentrarte en tus actividades habituales?" Outfit SemiBold 18sp,
second card peeking below (slightly smaller, not swiped yet),
left/right arrow hints at card edges,
progress: "Pregunta 1 de 2" below cards,
bottom: "Anterior" text button
```

#### 17. CheckInScreen — Paso 7: Notas Adicionales

```
Mobile app wellness check-in final step, Android UI, RelaxMind, white background, green theme,
progress bar at top: 7/7 almost full,
title: "¿Algo más que quieras compartir?" Outfit SemiBold 20sp,
subtitle: "Es completamente opcional" Urbanist 14sp gray,
large outlined multiline text field, placeholder: "Escribe libremente..." 6 lines visible,
character counter bottom-right: "0 / 500" in small gray,
below the field: "Finalizar" large green filled button,
"Anterior" text button above the main button,
calm and spacious layout with generous padding
```

#### 18. CheckInResultScreen — Resultado del Check-in

```
Mobile app wellness check-in result screen, Android UI, RelaxMind, white background, green theme,
centered layout:
large animated number "74" Outfit ExtraBold 88sp in green #0F6E56 (score counter),
"/ 100" smaller text right-aligned to the score,
"Bueno" category label in green chip below the score,
short motivational message: "¡Sigue así! Tu bienestar va por buen camino." Urbanist 16sp gray centered,
horizontal color legend below (5 colored circles with their ranges),
Lottie animation placeholder: confetti or checkmark above the score (shown as sparkle stars),
"Ver mi dashboard" large green filled button at bottom,
celebratory but calm aesthetic
```

#### 19. DashboardPatientScreen

```
Mobile app patient dashboard screen design, Android UI, RelaxMind, white background, green theme #0F6E56,
top: greeting row "Buenos días, Carlos 👋" Outfit SemiBold 20sp + user circular avatar right,
"Hoy, miércoles 18 de junio" Urbanist 14sp gray,

4 cards stacked:
CARD 1 "Mi bienestar hoy" — green background #0F6E56:
  "74 / 100" large white text centered + "Bueno" white label below

CARD 2 "🎯 Meta de Hoy" — white card with green accent:
  "Respiración 4-7-8 · 8 min" + "Ir a meditar" green outline button right + unchecked checkbox

CARD 3 "📅 Próximo Recordatorio" — white card:
  "Cita con psicólogo — 15:00" with calendar icon

CARD 4 "👤 Mi Cuidador" — white card:
  avatar circle + "María García · Vinculada ✓" green chip

bottom-right: floating SOS button, coral circle #E8582A with SOS icon, pulsing ring animation,
bottom navigation bar: Dashboard(active green) / Meditar / Progreso / Agenda / Lumi,
Material Design 3, rounded cards 16dp, soft shadows
```

#### 20. MeditateScreen — Lista de Ejercicios

```
Mobile app meditation list screen, Android UI, RelaxMind, white background, green theme,
top bar: "Meditar" title Outfit Bold 24sp,
small banner card: "⭐ Tu meta de hoy: Respiración 4-7-8" green background banner,

LazyColumn with 5 meditation exercise cards:
each card horizontal: 
  left: colored rounded square icon (type icon: lung for breathing, person for mindfulness, leaf for relaxation)
  center column: title Outfit SemiBold 16sp + type gray + duration "8 min"
  right: arrow icon, "Meta de hoy" green chip on first card
  
cards:
1. "Respiración 4-7-8" · Respiración · 8 min — green icon — "META DE HOY" chip
2. "Respiración de caja" · Respiración · 10 min — blue icon
3. "Escaneo corporal" · Mindfulness · 15 min — purple icon
4. "Meditación de gratitud" · Mindfulness · 12 min — teal icon
5. "Respiración diafragmática" · Relajación · 6 min — mint icon

bottom navigation active on "Meditar" tab, Material Design 3
```

#### 21. MeditationDetailScreen — Ejercicio Activo

```
Mobile app meditation exercise active screen, Android UI, RelaxMind,
dark green gradient background #0F6E56 to #0A4D3C full screen,
top: back arrow white + exercise title "Respiración 4-7-8" Outfit SemiBold 18sp white,

center: large circular breathing animation (800dp area):
  soft white translucent circle expanding (inhale phase), glow effect,
  inner circle with "Inhala..." text Outfit Bold 26sp white centered,

below animation area (white card section bottom 35% of screen):
  current phase label: "Inhala... 4" large white text on green,
  linear progress bar: green track, 60% filled, label "4:32 restantes" below,
  row of 2 buttons: "Pausar" outline white button + "Completar" outline white smaller button,

immersive full-screen design, minimal distractions, centered calm aesthetic
```

#### 22. ProgressScreen — Pantalla de Progreso

```
Mobile app progress tracking screen, Android UI, RelaxMind, white background, green theme,
top bar: "Mi Progreso" Outfit Bold 24sp,

SECTION 1 — Racha:
centered card: flame emoji large + "12" Outfit ExtraBold 52sp green + "días seguidos" below,
"Mejor racha: 18 días" Urbanist 14sp gray,

SECTION 2 — Gráfico mensual:
row: "< Abril" — "Mayo 2026" Outfit SemiBold 18sp — "Junio >"
7-column grid of circles (calendar), each 36dp:
header row: L M X J V S D in small gray,
rows of circles: most green (good days), some yellow, gray (no check-in), a few red,
color legend below: 5 small circles with labels "Muy bajo · Bajo · Moderado · Bueno · Excelente"

SECTION 3 — Logros:
"Logros" title + 4-column grid of achievement icons:
3 colored icons (unlocked) + gray desaturated icons (locked) with lock overlay,

SECTION 4 — Historial:
"Historial" title + 3 list rows: date | score chip colored | category text,
Material Design 3, data-rich but clean layout
```

#### 23. ScheduleScreen — Vista Semanal

```
Mobile app schedule screen weekly view, Android UI, RelaxMind, white background, green theme,
top bar: "Agenda" + FAB "+" top right corner (green circle),

week row: L 16 | M 17 | X 18 selected (green circle, white text) | J 19 | V 20 | S 21 | D 22,

list of events for Wednesday 18:
- 09:00 · green dot · "Cita con psicólogo" card with category chip "Psicología"
- 13:00 · blue dot · "Tomar Sertralina 50mg" card with category chip "Medicación"  
- 15:30 · orange dot · "Llamar a mamá" card with category chip "Recordatorio"
each event card: time left + dot + title + category chip + arrow right,

"sin eventos para el resto del día" gray placeholder if list ends,
bottom navigation: Agenda tab active,
Material Design 3, clean calendar style
```

#### 24. ScheduleScreen — Vista Mensual (Collage)

```
Mobile app schedule screen monthly calendar view, Android UI, RelaxMind, white background,
month header: "Junio 2026" centered + "<" ">" navigation arrows,
calendar grid 7 columns:
header: L M X J V S D in small gray,
day cells larger (about 48dp): number at top,
some cells have: small photo thumbnail as background (diary entry photos, low opacity),
some cells have colored dots below the number (appointment types),
today (18) highlighted with green circle around number,
days with diary photos show a collage thumbnail,

below calendar: "Toca un día para ver sus eventos" hint text in gray,
bottom navigation: Agenda active, Material Design 3
```

#### 25. CreateAppointmentScreen

```
Mobile app create appointment screen, Android UI, RelaxMind, white background, green theme,
top bar: back arrow + "Nuevo evento" title,
form content:
- "Título del evento" outlined text field — filled: "Cita con neuróloga"
- Type selector: 3 horizontal cards: 
  "🏥 Cita médica" GREEN SELECTED border | "💊 Medicación" gray | "📌 Recordatorio" gray
- "Categoría" field — filled: "Neurología"
- Date row: "📅 Martes, 24 junio 2026" outlined button
- Time row: "🕐 10:30 AM" outlined button
- "Notas" multiline outlined field, placeholder "Detalles opcionales..."
- "Guardar evento" large green button at bottom full width,
Material Design 3, scrollable form, rounded inputs
```

#### 26. AppointmentDetailScreen

```
Mobile app appointment detail screen, Android UI, RelaxMind, white background, green theme,
top bar: back arrow + "Detalle del evento",
card at top with green left border accent:
  "Cita con neuróloga" Outfit Bold 22sp,
  "🏥 Cita médica · Neurología" chip row,
  "📅 Martes 24 junio · 10:30 AM" with calendar icon,
  "📌 Recordatorio 15 min antes" smaller text gray,
notes section: "Llevar estudios de resonancia" in outlined card,
below: "✓ Marcar como completado" green outline button,
"Eliminar evento" red text button at bottom with trash icon,
Material Design 3, clean detail view
```

#### 27. DiaryEntryScreen

```
Mobile app diary entry creation screen, Android UI, RelaxMind, white background, green theme,
top bar: back arrow + "Nueva entrada",
category chips scrollable horizontal row:
"Estrés" | "Familia" | "Trabajo" — SELECTED green | "Logro" | "Otro" 
selected chip = green fill white text, others = gray border,

emotional tag row: emoji buttons horizontal:
😟 Ansioso | 😌 Tranquilo — SELECTED with green underline | 😊 Feliz | 😢 Triste | 😤 Frustrado,

large outlined text field 6 lines: "¿Qué quieres recordar de hoy?"
partial text visible: "Hoy fue un día bastante tranquilo, terminé el proyecto..."

photos section below:
row of 2 small photo thumbnails (added photos, each with X button) + "＋ Agregar foto" outline button,

"Guardar entrada" green button at bottom,
Material Design 3, warm journaling aesthetic
```

#### 28. SettingsPatientScreen

```
Mobile app patient settings screen, Android UI, RelaxMind, white background, green theme,
top bar: "Ajustes" title (no back arrow, it's a tab),
ScrollColumn layout:

PROFILE SECTION:
card row: circular avatar 52dp + "Carlos Mendoza" bold + "carlos@email.com" gray + arrow right,

"APARIENCIA" section header gray uppercase small:
toggle row: moon icon + "Modo oscuro" + switch OFF,

"IDIOMA" section:
row: globe icon + "Idioma" + "Español" right + arrow,

"NOTIFICACIONES" section:
toggle: bell icon + "Notificaciones" + switch ON,
toggle: clock icon + "Recordatorio de check-in" + switch ON (indented slightly),

"SEGURIDAD" section:
toggle: fingerprint icon + "Inicio con biometría" + switch ON,
row: logout icon red + "Cerrar sesión" red text,

"INFORMACIÓN" section:
row: document icon + "Términos y condiciones" + arrow,
row: info icon + "Versión de la app" + "1.0.0" right,

"DATOS PERSONALES" section (bottom):
row: link-off icon orange + "Desvincular cuidador" orange text,
row: trash icon red + "Borrar cuenta" red text,

Material Design 3, list-style settings, dividers between sections
```

#### 29. EditProfileScreen

```
Mobile app edit profile screen, Android UI, RelaxMind, white background, green theme,
top bar: back arrow + "Editar perfil" + "Guardar" text button right green,

top center: large circular avatar 90dp with pencil edit badge bottom-right (green circle with pen icon),
"Cambiar foto" green text button below avatar,

form fields below:
- "Nombre" field — value: "Carlos"
- "Apellidos" field — value: "Mendoza García"
- "Fecha de nacimiento" — value: "12 / 05 / 1998"
- "Sexo" dropdown — value: "Masculino"
- "Condición de salud" multiline field — value: "Trastorno de ansiedad generalizada"

all fields are outlined text fields, editable,
Material Design 3, profile edit style
```

#### 30. LinkCaregiverScreen — Código QR

```
Mobile app link caregiver QR code screen, Android UI, RelaxMind, white background, green theme,
top bar: back arrow + "Vincular Cuidador",
instruction text: "Muestra este código a tu cuidador" Urbanist 16sp gray centered,

large QR code centered, 240x240dp, black QR pattern on white, green corner markers,
below QR: "o comparte el código:" label in gray,
"143 827" large monospace digits, Outfit ExtraBold 42sp dark, slight letter spacing,
timer below: "⏱ Este código expira en 07:23" — orange color for urgency,
"Generar nuevo código" text button below (grayed out, disabled while timer active),

Material Design 3, trustworthy secure aesthetic
```

---

### FLUJO CUIDADOR

---

#### 31. DashboardCaregiverScreen

```
Mobile app caregiver dashboard screen, Android UI, RelaxMind, white background, INDIGO theme #4338A8,
top: "Hola, María 👋" Outfit SemiBold 20sp + caregiver avatar right,

CARD "ALERTAS ACTIVAS" — slightly red-tinted card:
  SOS alert row: red circle icon "🆘" + "Carlos Mendoza · hace 3 min" + "Ver" indigo button,
  low score row: orange circle "📊" + "Ana Torres · Puntaje 18/100 · hoy" + "Ver",

SECTION "MIS PACIENTES":
"Mis pacientes (3)" label + "Ver todos" text link right,
horizontal scroll row of 3 patient circles:
  each: circular avatar 64dp with colored ring border (green=ok, orange=medium, red=low)
  + name below small

bottom area: if no more content, floating "+" FAB indigo for adding patient,
bottom navigation: Dashboard(active indigo) / Pacientes / Alertas / Ajustes,
Material Design 3, professional caring aesthetic
```

#### 32. ScanQRScreen — Escanear código del paciente

```
Mobile app QR scanner screen, Android UI, RelaxMind caregiver side, INDIGO theme #4338A8,
top bar: back arrow white + "Vincularme con paciente" white,
camera view fullscreen: dark overlay with transparent square scanning area in center 240x240dp,
scanning square: white corner brackets, animated scanning line moving top to bottom in indigo,

below camera area (white bottom sheet):
OR divider "o ingresa el código manualmente",
"Código de 6 dígitos" outlined text field,
"Verificar código" indigo filled button,
small helper text: "Pide al paciente que genere un código desde su app" gray,

Material Design 3, scanner aesthetic
```

#### 33. PatientsListScreen

```
Mobile app patients list screen, Android UI, RelaxMind caregiver, white background, INDIGO theme,
top bar: "Mis Pacientes" + search icon right,
search bar: "Buscar paciente..." with magnifier icon, rounded, light gray background,

patient list cards (3 items visible):
CARD 1: avatar circle (green ring) + "Carlos Mendoza" bold + "Trastorno de ansiedad" gray + chip "Bueno 74/100" green,
  last check-in: "Hoy, 09:32" + red bell icon (has pending alert),
CARD 2: avatar (orange ring) + "Ana Torres" + "Depresión leve" + chip "Moderado 52/100" yellow,
  "Ayer, 22:15" — no alert,
CARD 3: avatar (red ring) + "Roberto Lima" + "Estrés crónico" + chip "Bajo 28/100" orange,
  "Hace 2 días" + red bell icon (alert),
each card has arrow right icon,
Material Design 3, list style with health indicators
```

#### 34. PatientDetailScreen — Tab Progreso

```
Mobile app patient detail screen, Android UI, RelaxMind caregiver, white background, INDIGO theme,
top bar: back arrow + "Carlos Mendoza" + phone icon right (to call),
patient header: avatar 72dp circular + "Carlos Mendoza" Outfit Bold 22sp + "Trastorno de ansiedad" gray,

3 tabs: "Progreso" (active underline indigo) | "Historial" | "Alertas SOS",

TAB PROGRESO content:
month selector: "< Mayo" — "Junio 2026" — "Julio >"
wellness calendar grid: circles colored by score, same color system as patient view,
"Racha actual: 8 días 🔥" below calendar,

NOTE: caregiver sees exact same data patient sees but cannot interact,
Material Design 3, read-only data view
```

#### 35. PatientDetailScreen — Tab Historial

```
Mobile app patient history tab screen, Android UI, RelaxMind caregiver, white background, INDIGO theme,
top section same as detail screen header (avatar + name + tabs),
TAB "Historial" active,

list of check-in history:
row 1: "Hoy · 09:32" + chip "74/100 Bueno" green + "Ver detalle" small link
row 2: "Ayer · 21:45" + chip "68/100 Bueno" green
row 3: "Hace 2 días · 20:10" + chip "52/100 Moderado" yellow
row 4: "Hace 3 días · 19:58" + chip "18/100 Muy bajo" red + "⚠ Alerta enviada" small red label
row 5: "Hace 4 días" + chip "71/100 Bueno" green,
smooth list with alternating subtle separator lines,
Material Design 3
```

#### 36. AlertsHistoryScreen

```
Mobile app alerts history screen, Android UI, RelaxMind caregiver, white background, INDIGO theme,
top bar: "Historial de Alertas" Outfit Bold 22sp,

filter chips scrollable horizontal: 
"Todos" (active indigo) | "SOS" | "Check-in bajo" | "Sin check-in" — all outlined gray except active,
patient filter dropdown if multiple patients: "Paciente: Todos ▼",

alert list (10 items limit):
ALERT 1: red SOS icon circle + "Carlos Mendoza · SOS" bold + "Hoy 02:15" + chip "PENDIENTE" red outline
  "Marcar como resuelta" small indigo text button indented,
ALERT 2: orange chart icon + "Ana Torres · Bienestar muy bajo (18/100)" + "Ayer 22:15" + chip "RESUELTA" gray,
ALERT 3: gray calendar icon + "Roberto Lima · Sin check-in" + "Ayer" + chip "RESUELTA" gray,
ALERT 4: similar pattern...

Material Design 3, alert management list style
```

#### 37. SOSPatientScreen — Pantalla SOS activa (Paciente)

```
Mobile app SOS emergency active screen, Android UI, RelaxMind patient side,
FULL SCREEN coral background #E8582A, all text white,
large animated concentric rings pulsing from center (semi-transparent white circles),
center: 
  "SOS ACTIVADO" Outfit ExtraBold 32sp white, 
  below: circular pulsing border animation,
  "Tu cuidador ha sido notificado" Urbanist 18sp white 80% opacity,
  "María García está en camino" Urbanist 16sp white 60% opacity,

large white button centered: "LLAMAR A CUIDADOR" with phone icon, coral text on white fill,
"Cancelar" small white text button at very bottom,
emergency but not panic-inducing, organized urgent design
```

#### 38. SOSAlertScreen — Pantalla SOS (Cuidador)

```
Mobile app SOS alert screen for caregiver, Android UI, RelaxMind,
top third: coral red background #E8582A,
  "🆘 ALERTA SOS" pulsing Outfit ExtraBold 28sp white,
  patient avatar circular 72dp with white border,
  "Carlos Mendoza" Outfit Bold 22sp white,
  
bottom two-thirds: white background:
  large coral "LLAMAR AL PACIENTE" button with phone icon (full width),
  
  Google Maps embedded mini-map 220dp height:
    map showing patient location with animated red pulsing marker pin,
    "Carlos está aquí" tooltip on marker,
  
  "VER RUTA COMPLETA" indigo outline button below map,
  "Marcar como resuelta" small gray text button at very bottom,
  
emergency organized layout, Material Design 3, clear information hierarchy
```

#### 39. SettingsCaregiverScreen

```
Mobile app caregiver settings screen, Android UI, RelaxMind, white background, INDIGO theme #4338A8,
top bar: "Ajustes" (tab, no back arrow),
layout identical to patient settings but:
- Profile card: caregiver avatar + "María García" + email
- SAME toggle sections: Modo oscuro / Idioma / Notificaciones / Biometría
- "Cerrar sesión" red row
- "Información" section with terms and version
- "DATOS PERSONALES" section: ONLY "Borrar cuenta" red (NO unlink caregiver option)
Material Design 3, identical structure to patient settings for consistency
```

---

### PANTALLA LUMI

---

#### 40. LumiChatScreen — Chat activo

```
Mobile app AI wellness chat screen, Android UI, RelaxMind, white background, green theme,
top bar: 
  left: green circular Lumi avatar (orb with sparkle) 40dp + "Lumi" bold + "Asistente de bienestar" small gray
  right: pencil icon (new chat) + clock icon (history),

chat message area (bubbles):
  Lumi bubble left: light gray background rounded card, small Lumi avatar left, 
    text: "Hola Carlos 👋 ¿Cómo te has sentido hoy?" Urbanist 14sp,
  User bubble right: green background #0F6E56, 
    text: "Un poco ansioso la verdad, tuve reuniones todo el día" white text Urbanist 14sp,
  Lumi bubble left: gray card,
    text: "Entiendo, los días cargados de reuniones pueden agotar mucho. ¿Quieres que practiquemos un ejercicio de respiración rápido?" Urbanist 14sp,
  "Lumi está escribiendo..." indicator: small Lumi avatar + 3 animated dots,

bottom input row: rounded outlined text field "Escribe un mensaje..." + send button (paper plane green icon),
Material Design 3, messaging app aesthetic, warm and supportive
```

#### 41. LumiHistoryScreen — Historial de conversaciones

```
Mobile app Lumi chat history screen, Android UI, RelaxMind, white background, green theme,
top bar: back arrow + "Historial de Lumi",

list of archived sessions:
SESSION 1: "💬 Hoy" date header, 
  card: green chat bubble icon + "Sesión de hoy" Outfit SemiBold 16sp + "Activa" green chip right,
  preview: "Hola Carlos 👋 ¿Cómo te has senti..." Urbanist 14sp gray,
SESSION 2 header "Junio 2026":
  card: gray chat icon + "15 jun 2026 — 21:32" + preview text truncated,
SESSION 3:
  card: gray chat icon + "12 jun 2026 — 09:15" + preview,
SESSION 4:
  card + "08 jun 2026 — 14:45",

archived sessions have gray icon and no green chip,
tapping any session opens read-only view with "Esta conversación está archivada" banner,
Material Design 3, clean list style
```

---

### PANTALLAS DE ESTADO VACÍO Y RESULTADO

---

#### 42. Empty State — Sin check-in del día (Dashboard)

```
Mobile app empty state for dashboard wellness score card, Android UI, RelaxMind,
inside a white card section within the dashboard:
soft illustration: calendar icon with question mark or small plants growing from calendar,
text: "Aún no has registrado tu check-in de hoy" Outfit SemiBold 16sp dark,
subtitle: "¿Cómo te sientes hoy?" Urbanist 14sp gray,
"Hacer mi check-in ahora" green filled button,
small text: "Toma solo 2 minutos" gray italic below button,
card style with rounded corners, warm empty state design
```

#### 43. Empty State — Sin cuidador vinculado

```
Mobile app empty state card, Android UI, RelaxMind, white card background,
illustration inside card: two puzzle piece halves in green and indigo, not yet connected,
text: "No tienes un cuidador vinculado" Outfit SemiBold 16sp,
subtitle: "Tu cuidador podrá ver tu bienestar y recibir alertas" Urbanist 14sp gray,
"Vincular cuidador" green outline button,
hopeful and inviting, not urgent, small card design that fits within dashboard
```

#### 44. Empty State — Sin pacientes (Cuidador dashboard)

```
Mobile app empty state screen, Android UI, RelaxMind caregiver, white background, INDIGO theme,
large centered illustration: caregiver figure with open arms, soft indigo tones, abstract flat style,
title: "Aún no tienes pacientes" Outfit Bold 22sp,
subtitle: "Vincúlate con un paciente para comenzar a acompañarlos" Urbanist 16sp gray centered,
"Vincularme con un paciente" large indigo filled button,
helper text: "El paciente debe generar un código QR desde su app" Urbanist 14sp gray,
centered layout, supportive and welcoming
```

#### 45. Modal — Logro Desbloqueado

```
Mobile app achievement unlock modal/dialog, Android UI, RelaxMind,
dark semi-transparent overlay background,
centered floating card white, rounded 24dp, shadow strong:
  confetti/sparkles animation area at top (placeholder: colored dots and stars),
  achievement icon centered: golden star clay icon 80dp,
  "¡Logro desbloqueado!" Outfit Bold 22sp dark, centered,
  "Bienestar alto" achievement name in green Outfit SemiBold 18sp,
  "Completaste un check-in con 80 puntos o más" Urbanist 14sp gray,
  "¡Genial! 🎉" green filled button at bottom full width,
celebratory but concise, animated scale-in entrance, Material Design 3
```

---

## PARTE 2 — ÍCONOS CLAY PLASTICINE (12 Logros)

> Mismo estilo en todos: 3D clay plasticine, brillante, sombra suave, fondo blanco/transparente.

#### `first_checkin` — Brote verde

```
Cute 3D clay plasticine icon, small green seedling sprout emerging from dark brown soil,
two tiny leaves unfolding, bright lime green glossy clay,
soft drop shadow underneath, white background, isolated product shot,
no text, 1:1 square, ultra detailed clay render, cheerful and fresh mood
```

#### `streak_3` — Llama pequeña

```
Cute 3D clay plasticine small cozy flame icon,
warm orange-yellow gradient clay, small and rounded flame shape,
glossy surface with soft inner glow, soft shadow, white background,
isolated, no text, 1:1 square, ultra detailed clay render
```

#### `streak_7` — Llama mediana

```
Cute 3D clay plasticine medium flame icon,
brighter orange and golden yellow clay, taller more energetic shape,
glossy surface, dynamic upward motion, soft shadow, white background,
isolated, no text, 1:1 square, ultra detailed clay render, motivating mood
```

#### `streak_14` — Llama grande

```
Cute 3D clay plasticine large vibrant flame,
deep orange base to bright yellow tip, tall powerful shape,
small sparks and dots around it, very glossy, dramatic shadow,
white background, isolated, no text, 1:1 square, ultra detailed clay render
```

#### `streak_30` — Trofeo dorado

```
Cute 3D clay plasticine golden trophy cup,
shiny warm gold clay with metallic sheen, star engraved on front,
small sparkle dots floating around it, glossy, celebration mood,
soft shadow, white background, isolated, no text, 1:1 square, ultra detailed clay render
```

#### `first_meditation` — Figura meditando

```
Cute 3D clay plasticine small human figure sitting cross-legged in meditation pose,
calm expression, emerald green and teal clay, soft glowing aura around figure,
smooth rounded body shape, very glossy, soft shadow, white background,
isolated, no text, 1:1 square, ultra detailed clay render, serene mood
```

#### `meditations_10` — Nube tranquila

```
Cute 3D clay plasticine fluffy cloud with a calm gentle face (subtle smile),
soft sky blue and white pastel clay, very smooth and rounded,
small soft rays or sparkles around it suggesting peace,
glossy, soft shadow, white background, isolated, no text, 1:1 square, ultra detailed clay render
```

#### `first_diary` — Diario con estrella

```
Cute 3D clay plasticine small diary book,
forest green clay cover with bright yellow star badge on front,
slightly open showing white pages inside, coral ribbon bookmark,
glossy clay with fabric-feel cover texture, soft shadow, white background,
isolated, no text, 1:1 square, ultra detailed clay render
```

#### `diary_7` — Diario brillante

```
Cute 3D clay plasticine glowing diary book,
bright emerald green clay cover with golden sparkle particles floating around it,
small stars and light dots near it, slightly open, dynamic energy,
glossy, soft shadow, white background, isolated, no text, 1:1 square, ultra detailed clay render
```

#### `score_80` — Estrella dorada

```
Cute 3D clay plasticine five-pointed star,
warm gold and amber yellow gradient clay, perfectly shaped,
very glossy surface with metallic sheen and sparkle reflections on surface,
soft shadow, white background, isolated, no text, 1:1 square, ultra detailed clay render
```

#### `score_100` — Gema azul

```
Cute 3D clay plasticine precious gem or crystal,
bright sapphire blue to aquamarine gradient, faceted hexagonal shape,
inner glow as if lit from within, clay-meets-crystal aesthetic,
soft shadow, white background, isolated, no text, 1:1 square, ultra detailed clay render
```

#### `lumi_first` — Burbuja de chat verde

```
Cute 3D clay plasticine chat speech bubble, rounded rectangle with tail bottom-left,
bright emerald green #0F6E56 glossy clay,
small sparkle star inside the bubble (suggesting AI magic),
glossy, soft shadow, white background, isolated, no text, 1:1 square, ultra detailed clay render
```

---

## PARTE 3 — ILUSTRACIONES PLANAS (pantallas y estados)

#### Onboarding Slide 1

```
Flat design illustration, wellness mental health app, no text,
serene person sitting on a soft green hill holding a smartphone,
wellness dashboard visible on phone screen with green charts,
floating small icons around: sun, leaf, heart, checkmark in soft greens and yellows,
color palette: #0F6E56 dark green, soft cream, warm yellow, white,
organic rounded shapes, modern minimalist style similar to Headspace,
transparent background, 4:3 ratio
```

#### Onboarding Slide 2

```
Flat design illustration, wellness app, no text, transparent background,
abstract peaceful figure in lotus meditation pose,
surrounded by soft concentric circles (breathing rhythm visualization),
floating leaves, dots, gentle curves around the figure,
color palette: #0F6E56 green, #68D391 light green, soft lavender, warm white,
calm and centering mood, modern flat style, 4:3 ratio
```

#### Onboarding Slide 3

```
Flat design illustration, wellness app, no text, transparent background,
two abstract simplified human figures standing side by side,
one in #0F6E56 green (patient), one in #4338A8 indigo (caregiver),
glowing connection line between them, subtle phone/app icon floating near,
warm and supportive mood, soft gradient light between the figures,
modern flat style, 4:3 ratio
```

#### Email Verificación

```
Flat design icon illustration, wellness app, no text, transparent background,
large rounded envelope icon, green color #0F6E56,
small sparkle stars and dots floating from the envelope,
clean modern minimal flat icon style, soft shadow, 1:1 square
```

#### Permiso de Notificaciones

```
Flat design illustration, wellness app, no text, transparent background,
large friendly notification bell, green #0F6E56,
soft bubbles and small notification dots floating upward from the bell,
warm and inviting, not alarming, modern flat style, 1:1 square
```

#### Recuperación de Contraseña

```
Flat design illustration, wellness app, no text, transparent background,
envelope icon with a small padlock on it, green and gray tones,
minimal and trustworthy, modern flat style, 1:1 square
```

#### Lumi Avatar

```
Flat design avatar for an AI wellness assistant named Lumi, no text, transparent background,
abstract friendly glowing orb or rounded shape with subtle sparkle/star inside,
NOT a human face — a warm ambient presence,
bright emerald green #0F6E56 with soft white inner glow,
modern, slightly futuristic but warm and approachable,
1:1 square, suitable for chat bubble avatar
```

#### Estado Vacío — Sin Check-in

```
Flat design illustration for empty state, wellness app, no text, transparent background,
a soft calendar with small plants or flowers growing from empty day cells,
hopeful and growth-suggesting mood, no pressure aesthetic,
green and soft gray tones, modern flat style, portrait ratio
```

#### Estado Vacío — Sin Cuidador

```
Flat design illustration for empty state, wellness app, no text, transparent background,
two puzzle pieces in green (#0F6E56) and indigo (#4338A8), slightly separated,
clearly designed to fit together, hopeful and inviting mood,
organic rounded shapes, modern flat style, portrait ratio
```

#### Estado Vacío — Sin Pacientes (Cuidador)

```
Flat design illustration for empty state, caregiver wellness app, no text, transparent background,
abstract caregiver figure with open arms, soft indigo #4338A8 tones,
small heart or people icons floating suggesting future patients,
welcoming and supportive mood, modern flat style, portrait ratio
```

#### Resultado Check-in — Excelente (81-100)

```
Flat design illustration for excellent wellness score result, no text, transparent background,
abstract sunshine, green leaves, small happy figure doing a victory pose,
confetti dots in green, yellow, white, pure celebration,
color palette: #0F6E56, #68D391, warm yellow, white,
uplifting and joyful, modern flat style, portrait ratio
```

#### Resultado Check-in — Muy Bajo (0-20)

```
Flat design illustration for low wellness score result, compassionate tone, no text, transparent background,
small abstract figure surrounded by gentle soft glowing arms or warm light,
mood: "you are supported, you are not alone", NOT sad or discouraging,
muted warm colors: soft coral, warm beige, gentle green,
modern flat style, portrait ratio, compassionate wellness aesthetic
```

#### Ilustración Meditación (header)

```
Flat design illustration for meditation section, wellness app, no text, transparent background,
abstract human figure in lotus meditation pose with soft circular aura rings,
floating leaves and soft dots around them,
color palette: #0F6E56, #68D391, soft lavender, warm white,
peaceful and centering, modern flat style, 4:3 landscape or 1:1 square ratio
```

#### SOS Activado (Pantalla Paciente)

```
Flat design illustration for SOS activated screen, wellness app, no text, transparent background,
use only coral #E8582A and white colors,
abstract hands reaching toward each other, OR concentric ripple circles suggesting help coming,
reassuring rather than scary, organized urgency not panic,
modern flat style, portrait ratio, all elements white on intended coral background
```

---

## PARTE 4 — AVATARES DE USUARIO (12 opciones)

```
Create a set of 12 diverse user avatar illustrations for a wellness mobile app,
style: cute rounded simplified cartoon face, flat design with soft shadows,
each avatar: different hair color and style, different skin tone, calm gentle expression (subtle smile),
diverse representation: different genders, ethnicities, hair textures,
no text, no complex details, clean shapes, all at same scale and style,
transparent background or white background,
arrange in a 4x3 grid for reference, export individually named avatar_01 to avatar_12,
friendly and inclusive, wellness app aesthetic
```

---

## PARTE 5 — ANIMACIONES LOTTIE (búsqueda y especificaciones)

> Buscar en **LottieFiles.com** con los términos indicados. Filtrar: Free + Editable colors.

| # | Archivo | Buscar en LottieFiles | Duración | Loop | Colores a editar |
|---|---|---|---|---|---|
| 1 | `anim_checkin_complete.json` | `"success checkmark green"` | 2s | false | primary → #0F6E56 |
| 2 | `anim_achievement_unlock.json` | `"confetti celebration achievement"` | 3s | false | multicolor verde/dorado |
| 3 | `anim_breathing_circle.json` | `"breathing circle inhale exhale meditation"` | 8-12s | true | #0F6E56 y blanco |
| 4 | `anim_meditation_mindfulness.json` | `"meditation lotus mindful calm"` | 6s | true | #0F6E56 y #68D391 |
| 5 | `anim_streak_flame.json` | `"flame fire streak"` | 2s | true | naranja → dorado |
| 6 | `anim_lumi_typing.json` | `"typing indicator chat dots"` | 1.5s | true | gris → #0F6E56 |
| 7 | `anim_sos_pulse.json` | `"pulse ring emergency alert"` | 1.5s | true | → #E8582A coral |
| 8 | `anim_loading.json` | `"loading spinner minimal circular"` | 1s | true | → #0F6E56 |
| 9 | `anim_sos_radar.json` | `"radar ping wave emergency"` | 2s | true | → #E8582A coral |
| 10 | `anim_notification_bell.json` | `"notification bell ring"` | 2s | false | → #0F6E56 |
| 11 | `anim_empty_state.json` | `"empty state search nothing found"` | 3s | true | → #0F6E56 |
| 12 | `anim_splash_logo.json` | `"logo reveal intro wellness"` | 3s | false | → #0F6E56 |

---

## GUÍA DE NOMBRES DE ARCHIVOS

```
MOCKUPS UI (referencias de diseño):
  screen_01_welcome_slide1.png
  screen_02_welcome_slide2.png
  screen_03_welcome_slide3.png
  screen_04_login.png
  screen_05_register.png
  screen_06_email_verification.png
  screen_07_avatar_setup.png
  screen_08_notification_permission.png
  screen_09_forgot_password.png
  screen_10_initial_test_intro.png
  screen_11_checkin_step1_emotion.png
  screen_12_checkin_step2_sleep.png
  screen_13_checkin_step3_energy.png
  screen_14_checkin_step4_stress.png
  screen_15_checkin_step5_frequency.png
  screen_16_checkin_step6_binary.png
  screen_17_checkin_step7_notes.png
  screen_18_checkin_result.png
  screen_19_dashboard_patient.png
  screen_20_meditate_list.png
  screen_21_meditation_detail.png
  screen_22_progress.png
  screen_23_schedule_weekly.png
  screen_24_schedule_monthly.png
  screen_25_create_appointment.png
  screen_26_appointment_detail.png
  screen_27_diary_entry.png
  screen_28_settings_patient.png
  screen_29_edit_profile.png
  screen_30_link_caregiver_qr.png
  screen_31_dashboard_caregiver.png
  screen_32_scan_qr.png
  screen_33_patients_list.png
  screen_34_patient_detail_progress.png
  screen_35_patient_detail_history.png
  screen_36_alerts_history.png
  screen_37_sos_patient.png
  screen_38_sos_caregiver.png
  screen_39_settings_caregiver.png
  screen_40_lumi_chat.png
  screen_41_lumi_history.png
  screen_42_empty_no_checkin.png
  screen_43_empty_no_caregiver.png
  screen_44_empty_no_patients.png
  screen_45_modal_achievement.png

ÍCONOS CLAY (logros):
  achievement_first_checkin.png
  achievement_streak_3.png
  achievement_streak_7.png
  achievement_streak_14.png
  achievement_streak_30.png
  achievement_first_meditation.png
  achievement_meditations_10.png
  achievement_first_diary.png
  achievement_diary_7.png
  achievement_score_80.png
  achievement_score_100.png
  achievement_lumi_first.png

ILUSTRACIONES:
  illus_onboarding_1.png
  illus_onboarding_2.png
  illus_onboarding_3.png
  illus_email_verification.png
  illus_notification_permission.png
  illus_forgot_password.png
  illus_lumi_avatar.png
  illus_empty_checkin.png
  illus_empty_caregiver.png
  illus_empty_patients.png
  illus_result_excellent.png
  illus_result_low.png
  illus_meditation.png
  illus_sos_activated.png

AVATARES:
  avatar_01.png → avatar_12.png
```

---

## TIPS POR HERRAMIENTA

- **Midjourney**: añadir `--ar 1:1 --style raw --q 2` para íconos clay. Para pantallas: `--ar 9:19 --style raw`
- **DALL·E 3**: especificar colores HEX literalmente en el prompt. Funciona mejor para ilustraciones planas.
- **Leonardo AI**: modelo "Kino XL" para ilustraciones planas, "3D Animation Style" para clay icons
- **Stable Diffusion**: usar LoRA de "clay 3D icon" o "flat design illustration" disponibles en CivitAI
- **LottieFiles**: filtrar siempre por `Free` y preferir los que tienen `AE Source` para editar colores con After Effects o con el editor online de LottieFiles
- **Para mockups UI**: Galileo AI, Uizard o v0.dev son más precisos que generadores de imagen genéricos