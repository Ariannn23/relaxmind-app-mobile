package com.relaxmind.app.data.local

import com.relaxmind.app.data.model.LibraryArticle

object MockLibraryData {
    val articles = listOf(
        // CATEGORÍA: ESTRÉS
        LibraryArticle(
            id = "estres_01",
            title = "Desactivando el Estrés Crónico",
            summary = "Aprende a identificar y romper el ciclo del estrés constante en tu vida diaria.",
            category = "estres",
            readTimeMinutes = 5,
            author = "Dr. Felipe Vargas",
            coverImageUrl = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?auto=format&fit=crop&q=80&w=800",
            targetRole = "both",
            content = """
                ## El Peligro del Estrés Invisible
                El estrés crónico es silencioso. A diferencia de un susto puntual, se acumula día a día por la presión constante. Tu cuerpo permanece en un estado de "alerta" que agota tus reservas de energía.
                
                ## Señales Físicas que Ignoramos
                - Dolor de cabeza tensional constante.
                - Tensión en la mandíbula (bruxismo).
                - Problemas digestivos sin causa aparente.
                
                ## La Técnica de las Micro-Pausas
                No necesitas una hora libre para desestresarte. Configura una alarma cada 2 horas y toma **2 minutos exactos** para:
                - Alejarte de la pantalla.
                - Estirar el cuello.
                - Respirar profundamente tres veces.
                Estas micro-pausas interrumpen la acumulación de cortisol antes de que llegue a niveles tóxicos.
            """.trimIndent()
        ),
        LibraryArticle(
            id = "estres_02",
            title = "Límites: Tu Escudo Contra el Estrés",
            summary = "Por qué aprender a decir 'no' es la herramienta más efectiva para reducir tu carga mental.",
            category = "estres",
            readTimeMinutes = 4,
            author = "Psic. Ana Rivera",
            coverImageUrl = "https://images.unsplash.com/photo-1499209974431-9dddcece7f88?auto=format&fit=crop&q=80&w=800",
            targetRole = "both",
            content = """
                ## Sobrecarga por Complacencia
                Gran parte de nuestro estrés proviene de aceptar responsabilidades que no nos corresponden. El miedo a decepcionar a otros termina decepcionándonos a nosotros mismos.
                
                ## La Regla del "Sí Diferido"
                Nunca digas "sí" inmediatamente a un nuevo compromiso. Acostúmbrate a responder:
                - "Déjame revisar mi agenda y te confirmo."
                - "Suena interesante, lo pensaré y te digo algo mañana."
                Esto te da espacio mental para evaluar si realmente tienes el tiempo y la energía, o si solo estás actuando por inercia.
                
                ## Delegar no es Fracasar
                Si eres cuidador o tienes demasiadas responsabilidades en casa, delegar no significa que no puedas con ello, significa que eres lo suficientemente inteligente para gestionar tus recursos a largo plazo.
            """.trimIndent()
        ),
        // CATEGORÍA: ANSIEDAD
        LibraryArticle(
            id = "ansiedad_01",
            title = "Comprendiendo la Ansiedad: Tu Guía de Inicio",
            summary = "Descubre qué es la ansiedad, por qué ocurre y técnicas iniciales para recuperar el control.",
            category = "ansiedad",
            readTimeMinutes = 4,
            author = "Dr. Roberto Casas",
            coverImageUrl = "https://picsum.photos/id/13/800/600",
            targetRole = "both",
            content = """
                ## ¿Qué es realmente la ansiedad?
                La ansiedad es una respuesta natural del cuerpo ante situaciones que percibe como amenazantes. No es tu enemiga, sino un sistema de alarma que, a veces, se activa cuando no hay un peligro real. Entender esto es el primer paso para desarmarla.
                
                ## Identificando los síntomas
                Es común experimentar:
                - Palpitaciones rápidas o sensación de ahogo.
                - Tensión muscular en hombros y cuello.
                - Pensamientos acelerados o catastróficos.
                - Dificultad para concentrarse en el presente.
                
                ## Técnica de anclaje "5-4-3-2-1"
                Cuando sientas que la ansiedad sube, usa tus sentidos para volver al presente:
                - Observa **5** cosas a tu alrededor.
                - Toca **4** texturas diferentes (tu ropa, la silla).
                - Escucha **3** sonidos distintos.
                - Identifica **2** olores.
                - Saborea **1** cosa (toma un sorbo de agua o imagina un sabor).
                
                ## El poder de la respiración cuadrada
                Una de las herramientas más efectivas es respirar en cuatro tiempos:
                1. **Inhala** profundamente por la nariz durante 4 segundos.
                2. **Sostén** el aire por 4 segundos.
                3. **Exhala** lentamente por la boca durante 4 segundos.
                4. **Espera** 4 segundos antes de volver a inhalar.
                Repite este ciclo 4 veces y notarás cómo tu sistema nervioso comienza a relajarse automáticamente.
            """.trimIndent()
        ),
        LibraryArticle(
            id = "ansiedad_02",
            title = "Estrategias Prácticas para Ataques de Pánico",
            summary = "Conoce las herramientas de respuesta rápida para desactivar un ataque de pánico antes de que escale.",
            category = "ansiedad",
            readTimeMinutes = 5,
            author = "Psi. Laura Gómez",
            coverImageUrl = "https://picsum.photos/id/14/800/600",
            targetRole = "both",
            content = """
                ## Diferenciando la Ansiedad del Pánico
                Mientras que la ansiedad es una preocupación sostenida, un ataque de pánico es un episodio súbito de miedo intenso que alcanza su pico en pocos minutos. Sus síntomas físicos pueden imitar los de un problema cardíaco, pero son inofensivos.
                
                ## El Principio de la Aceptación Radical
                Luchar contra el pánico suele intensificarlo. La estrategia más efectiva es observar las sensaciones y decir: **"Esto es solo adrenalina. Mi cuerpo está reaccionando a una falsa alarma. Estoy a salvo"**.
                
                ## El Ejercicio del Cubo de Hielo
                Cuando sientas que un ataque de pánico está por comenzar, un cambio brusco de temperatura puede "reiniciar" tu sistema nervioso.
                - Sostén un cubo de hielo en tus manos hasta que se derrita.
                - Si estás en casa, salpica agua fría en tu rostro.
                El frío intenso estimula el nervio vago y ralentiza la frecuencia cardíaca instantáneamente.
                
                ## La Técnica de Distracción Cognitiva
                Obliga a tu cerebro a utilizar su corteza prefrontal (la parte lógica) para desactivar la amígdala (el centro del miedo):
                - Cuenta hacia atrás desde 100 de 7 en 7 (100, 93, 86, 79...).
                - Nombra países que comiencen con cada letra del abecedario.
            """.trimIndent()
        ),
        
        // CATEGORÍA: SUEÑO
        LibraryArticle(
            id = "sueno_01",
            title = "Higiene del Sueño: Descanso Reparador",
            summary = "Estrategias probadas para conciliar el sueño más rápido y despertar con energía.",
            category = "sueño",
            readTimeMinutes = 5,
            author = "Dra. Elena Martínez",
            coverImageUrl = "https://picsum.photos/id/15/800/600",
            targetRole = "both",
            content = """
                ## La importancia del descanso
                El sueño no es un lujo, es una necesidad biológica. Durante la noche, el cerebro consolida memorias, repara tejidos y regula hormonas esenciales para tu bienestar emocional.
                
                ## Creando un santuario para dormir
                Tu habitación debe ser un lugar diseñado exclusivamente para el descanso:
                - **Oscuridad total:** Usa cortinas gruesas o un antifaz. La melatonina necesita oscuridad para producirse.
                - **Temperatura ideal:** Mantenla fresca, entre 18°C y 20°C.
                - **Silencio:** Considera usar tapones para los oídos o ruido blanco (como sonido de lluvia) si hay ruidos molestos.
                
                ## Rituales previos a dormir
                Establece una rutina de desconexión 60 minutos antes de ir a la cama:
                - Evita pantallas (celular, TV) por la luz azul que inhibe el sueño.
                - Toma un baño de agua tibia o lee un libro de papel.
                - Practica estiramientos suaves o ejercicios de respiración profunda.
                
                ## ¿Qué evitar por la noche?
                - **Cafeína y alcohol:** Alteran la arquitectura del sueño.
                - **Cenas pesadas:** Hacen que la digestión interrumpa tu descanso.
                - **Mirar el reloj:** Si no puedes dormir, ver la hora solo aumenta la ansiedad.
            """.trimIndent()
        ),
        LibraryArticle(
            id = "sueno_02",
            title = "Combatiendo el Insomnio por Estrés",
            summary = "Cómo apagar una mente hiperactiva por las noches y lograr dormir.",
            category = "sueño",
            readTimeMinutes = 6,
            author = "Dr. Martín Silva",
            coverImageUrl = "https://picsum.photos/id/16/800/600",
            targetRole = "both",
            content = """
                ## El ciclo del Insomnio Inducido
                El estrés genera cortisol, y el cortisol te mantiene alerta. Si pasas horas en la cama dando vueltas, tu cerebro asocia la cama con la preocupación y la frustración, creando un círculo vicioso de insomnio crónico.
                
                ## La Técnica del "Vaciado Mental"
                Si tu mente no se apaga, no trates de forzarla. En su lugar, usa un cuaderno (no el celular) junto a tu cama.
                - Anota cada pendiente o preocupación que cruce tu mente.
                - Al escribirlo, dile a tu cerebro: **"Ya está documentado, nos ocuparemos de esto mañana"**.
                
                ## La Regla de los 20 Minutos
                Si no logras conciliar el sueño después de 20 minutos (o si te sientes frustrado):
                - Levántate de la cama.
                - Ve a otra habitación con luz muy tenue.
                - Haz una actividad aburrida o relajante (como leer un libro técnico o hacer un rompecabezas sencillo).
                - Regresa a la cama **solo** cuando los párpados te pesen.
                
                ## Relajación Muscular Progresiva
                Acostado, tensa fuertemente los músculos de los pies por 5 segundos y luego suéltalos. Sube por las pantorrillas, muslos, abdomen, brazos y rostro. Este ejercicio físico agota la tensión residual.
            """.trimIndent()
        ),

        // CATEGORÍA: AUTOESTIMA
        LibraryArticle(
            id = "autoestima_01",
            title = "Reconstruyendo tu Amor Propio",
            summary = "Ejercicios diarios para cambiar tu diálogo interno y valorarte como mereces.",
            category = "autoestima",
            readTimeMinutes = 6,
            author = "Psic. Camila Torres",
            coverImageUrl = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?auto=format&fit=crop&q=80&w=800",
            targetRole = "both",
            content = """
                ## El impacto del diálogo interno
                La forma en que te hablas a ti mismo moldea tu realidad. A menudo, somos nuestros jueces más severos. Reconstruir la autoestima comienza por cambiar la voz crítica por una voz compasiva.
                
                ## Identificando al crítico interior
                Presta atención a frases como "nunca hago nada bien" o "soy un desastre". Cuando las detectes:
                - Ponles un **alto** consciente.
                - Cuestiónalas: ¿Le dirías eso a tu mejor amigo?
                - Reemplázalas por algo realista: "Cometí un error, pero estoy aprendiendo y mejorando".
                
                ## El diario de gratitud personal
                Cada noche, anota tres cosas de las que estés orgulloso de haber hecho hoy. Pueden ser cosas pequeñas:
                - Haber tomado agua suficiente.
                - Haber terminado una tarea difícil.
                - Haber respondido con calma en una discusión.
                
                ## Aprende a recibir cumplidos
                Cuando alguien te elogie, evita minimizar el halago (ej: "no es para tanto" o "fue suerte"). Simplemente di **"Gracias"** y permítete sentir que lo mereces.
            """.trimIndent()
        ),
        LibraryArticle(
            id = "autoestima_02",
            title = "Límites Sanos y Autocuidado",
            summary = "Por qué aprender a decir 'no' es el acto más grande de respeto propio.",
            category = "autoestima",
            readTimeMinutes = 5,
            author = "Psi. Sofía Mendoza",
            coverImageUrl = "https://picsum.photos/id/28/800/600",
            targetRole = "both",
            content = """
                ## ¿Qué son los límites personales?
                Son líneas invisibles que trazamos para proteger nuestra energía, tiempo y emociones. Sin límites, permitimos que las necesidades de los demás siempre pasen por encima de las nuestras, agotando nuestra autoestima.
                
                ## El miedo a decir "No"
                Muchos evitan decir "no" por miedo a decepcionar o a causar conflicto. Sin embargo, decir "sí" por compromiso genera resentimiento interno.
                - **Recuerda:** "No" es una oración completa. No necesitas dar largas justificaciones.
                - Un "no" amable pero firme fortalece tus relaciones a largo plazo porque se basan en la honestidad.
                
                ## Pasos para establecer un límite
                1. **Identifica tu necesidad:** Reconoce qué situación te está drenando.
                2. **Comunica el límite con claridad:** "Me encanta ayudarte, pero hoy necesito descansar."
                3. **Mantén el límite:** Las personas pueden reaccionar negativamente al principio si están acostumbradas a que siempre cedas. Mantente firme.
                
                ## El Autocuidado Activo
                El autocuidado no es solo un baño de espuma. Es tomar decisiones difíciles a favor de tu paz mental: alejarte de personas tóxicas, respetar tus horas de sueño y honrar tus valores.
            """.trimIndent()
        ),

        // CATEGORÍA: DEPRESIÓN
        LibraryArticle(
            id = "depresion_01",
            title = "Navegando los Días Grises",
            summary = "Estrategias compasivas para sobrellevar los días donde falta la energía y la motivación.",
            category = "depresion",
            readTimeMinutes = 5,
            author = "Dr. Andrés López",
            coverImageUrl = "https://picsum.photos/id/29/800/600",
            targetRole = "both",
            content = """
                ## La neblina de la depresión
                La depresión no es simplemente estar triste. Es una falta persistente de energía, la sensación de estar caminando bajo el agua y la incapacidad de disfrutar las cosas. Validar cómo te sientes es fundamental.
                
                ## La regla del "Suficientemente Bueno"
                Cuando estás en un episodio depresivo, tus estándares habituales son inalcanzables. Adopta la filosofía del "suficientemente bueno":
                - Si no puedes ducharte, lavarte la cara es suficientemente bueno.
                - Si no puedes cocinar, comer una manzana y queso es suficientemente bueno.
                Reduce las expectativas y celebra cada micro-logro.
                
                ## Conexión, no aislamiento
                El instinto principal de la depresión es aislarse, lo cual alimenta el ciclo negativo.
                - Intenta enviar al menos **un mensaje de texto** a alguien de confianza.
                - Siéntate en una habitación donde haya otras personas, incluso si no hablan. La mera presencia compartida ayuda.
                
                ## Cuidado Profesional
                Si los días grises son la norma y no la excepción, la intervención profesional es clave. La terapia y, si es necesario, la medicación, son herramientas vitales, no señales de debilidad.
            """.trimIndent()
        ),
        LibraryArticle(
            id = "depresion_02",
            title = "La Rutina como Ancla en la Tormenta",
            summary = "Por qué mantener una estructura diaria mínima es esencial para la recuperación emocional.",
            category = "depresion",
            readTimeMinutes = 6,
            author = "Psi. Carlos Navarro",
            coverImageUrl = "https://picsum.photos/id/46/800/600",
            targetRole = "both",
            content = """
                ## El caos de la falta de estructura
                La depresión disuelve la motivación. Si esperas a "tener ganas" de hacer las cosas, es probable que no hagas nada. Una rutina elimina la necesidad de tomar decisiones, reduciendo la carga cognitiva de tu cerebro.
                
                ## Diseñando una Rutina de Supervivencia
                No intentes planear un día de máxima productividad. Tu rutina de supervivencia debe incluir solo 3 pilares:
                1. **Levantarte a la misma hora** todos los días.
                2. **Exponerte a la luz solar** (idealmente salir a caminar 10 minutos).
                3. **Comer en horarios regulares**.
                
                ## La Activación Conductual
                Este es un principio psicológico que dice: **La acción precede a la motivación**.
                En lugar de esperar a sentirte bien para actuar, actúas para empezar a sentirte bien.
                - Empieza la tarea por solo 5 minutos.
                - A menudo, una vez que rompes la inercia del inicio, es más fácil continuar.
                
                ## Sé amable contigo mismo
                Habrá días en que la rutina fallará por completo. Eso no borra tu progreso. El objetivo no es la perfección, es la persistencia compasiva. Mañana es un nuevo día para intentarlo.
            """.trimIndent()
        ),

        // CATEGORÍA: CUIDADO DEL CUIDADOR
        LibraryArticle(
            id = "cuidado_01",
            title = "El Síndrome del Cuidador Quemado",
            summary = "Identifica las señales de agotamiento y aprende estrategias vitales de autocuidado.",
            category = "cuidado_del_cuidador",
            readTimeMinutes = 7,
            author = "Instituto de Cuidado Familiar",
            coverImageUrl = "https://images.unsplash.com/photo-1499209974431-9dddcece7f88?auto=format&fit=crop&q=80&w=800",
            targetRole = "both",
            content = """
                ## ¿Qué es el Síndrome del Cuidador?
                Es un estado de agotamiento físico, emocional y mental profundo que puede estar acompañado por un cambio de actitud, pasando de la empatía y cuidado a sentirse negativo y desapegado. Ocurre cuando el cuidador no obtiene la ayuda que necesita o intenta hacer más de lo que puede.
                
                ## Señales de alerta rojas
                No ignores estos síntomas:
                - Retraimiento respecto a amigos, familiares y otras personas queridas.
                - Pérdida de interés en actividades que antes disfrutabas.
                - Sentimientos de tristeza, irritabilidad, desesperanza y agotamiento constantes.
                - Cambios en los patrones de sueño o peso.
                
                ## La regla de la máscara de oxígeno
                En los aviones te dicen: **"Ponte tu propia máscara de oxígeno antes de ayudar a otros"**. Esto aplica perfectamente al cuidado. Si tú te desmoronas, no podrás cuidar a la otra persona. Cuidar de ti no es egoísmo, es una necesidad.
                
                ## Estrategias de supervivencia diaria
                - **Micro-descansos:** Toma 5 minutos cada par de horas solo para respirar, lejos de las exigencias.
                - **Busca grupos de apoyo:** Hablar con personas en tu misma situación valida tus sentimientos y reduce el aislamiento.
            """.trimIndent()
        ),
        LibraryArticle(
            id = "cuidado_02",
            title = "Compartiendo la Carga: Cómo Pedir Ayuda",
            summary = "Pedir ayuda no te hace menos capaz. Es una decisión inteligente para asegurar un cuidado sostenible.",
            category = "cuidado_del_cuidador",
            readTimeMinutes = 5,
            author = "Psi. Andrea Solís",
            coverImageUrl = "https://picsum.photos/id/137/800/600",
            targetRole = "both",
            content = """
                ## El mito de la autosuficiencia total
                Muchos asumen la carga entera creyendo que "solo yo puedo hacerlo bien" o por culpa de pedir tiempo para sí mismos. A largo plazo, este enfoque colapsa y afecta negativamente a todos los involucrados.
                
                ## ¿Por qué las personas no ofrecen ayuda?
                A menudo, amigos y familiares quieren ayudar pero no saben cómo. Si te ven siempre fuerte y resolviendo todo, asumen que lo tienes bajo control y temen entrometerse.
                
                ## Delegando de forma efectiva
                La clave para conseguir ayuda es ser **específico**.
                En lugar de decir: "Necesito ayuda con la casa", intenta:
                - "Juan, ¿podrías encargarte de llevar a papá al médico los martes?"
                - "María, ¿podrías hacernos la compra del supermercado esta semana?"
                
                ## Aceptando la imperfección
                Cuando delegas tareas, debes aceptar que las otras personas no las harán exactamente igual que tú. Eso está bien. Lo importante es que la tarea se complete y que tú ganes un respiro fundamental para tu salud mental.
            """.trimIndent()
        ),

        // CATEGORÍA: COMUNICACIÓN
        LibraryArticle(
            id = "comunicacion_01",
            title = "Comunicación Asertiva y Empática",
            summary = "Cómo expresar tus necesidades y emociones sin crear conflicto ni lastimar al otro.",
            category = "comunicacion",
            readTimeMinutes = 5,
            author = "Psic. Fernando Silva",
            coverImageUrl = "https://picsum.photos/id/201/800/600",
            targetRole = "both",
            content = """
                ## El desafío de comunicarse bajo estrés
                El estrés, la ansiedad y el cansancio dificultan enormemente la comunicación. A menudo, las palabras salen cargadas de frustración o, por el contrario, se guardan hasta generar resentimiento profundo.
                
                ## Mensajes "Yo" en lugar de "Tú"
                Cuando hables de un problema, enfócate en cómo te sientes en lugar de culpar al otro.
                - **Evita:** "Tú siempre me exiges demasiado y no valoras lo que hago."
                - **Prefiere:** "Yo me siento abrumado cuando hay muchas tareas juntas y necesito un momento de descanso."
                Esto reduce inmediatamente la actitud defensiva en la otra persona.
                
                ## La validación emocional
                Validar no significa estar de acuerdo, significa entender que la emoción del otro es real.
                - "Entiendo por qué te sientes frustrado por esta situación, tiene mucho sentido."
                - A veces, las personas no buscan soluciones, solo necesitan saber que alguien las comprende.
                
                ## El momento correcto
                No intentes tener conversaciones importantes cuando alguna de las partes está hambrienta, enfadada, solitaria o cansada (HALT por sus siglas en inglés). Pospón la charla diciendo: "Esto es muy importante para mí, hablemos después de cenar cuando estemos más tranquilos."
            """.trimIndent()
        ),
        LibraryArticle(
            id = "comunicacion_02",
            title = "Cómo Escuchar Verdaderamente",
            summary = "La escucha activa es el puente más fuerte para conectar con quien sufre.",
            category = "comunicacion",
            readTimeMinutes = 4,
            author = "Psi. Laura Martínez",
            coverImageUrl = "https://picsum.photos/id/212/800/600",
            targetRole = "both",
            content = """
                ## Oír vs. Escuchar
                Oír es un proceso biológico pasivo. Escuchar es un acto intencional que requiere toda tu concentración y empatía. La mayoría de las veces, escuchamos solo para preparar nuestra respuesta.
                
                ## Los elementos de la Escucha Activa
                - **Contacto visual:** Demuestra que estás presente y enfocado únicamente en la persona.
                - **Postura abierta:** Evita cruzar los brazos. Inclínate ligeramente hacia adelante.
                - **No interrumpas:** Deja que el otro termine su idea completa, incluso si hay pausas largas o silencio.
                
                ## El peligro de dar consejos no pedidos
                Cuando alguien comparte un problema, nuestro instinto es "arreglarlo". Sin embargo, dar un consejo no solicitado puede hacer sentir a la otra persona invalidada o incomprendida.
                - Prueba preguntar: **"¿Necesitas que te escuche, o estás buscando un consejo para solucionar esto?"**
                
                ## Parafrasear
                Para asegurar que entendiste bien, repite la idea principal: "Lo que escucho es que te sientes muy estresado por los cambios recientes de medicación, ¿es correcto?". Esto confirma el entendimiento y hace sentir valorado al hablante.
            """.trimIndent()
        ),

        // CATEGORÍA: GENERAL
        LibraryArticle(
            id = "general_01",
            title = "Los Beneficios del Mindfulness",
            summary = "Por qué la atención plena está transformando el bienestar psicológico moderno.",
            category = "general",
            readTimeMinutes = 4,
            author = "Revista RelaxMind",
            coverImageUrl = "https://picsum.photos/id/240/800/600",
            targetRole = "both",
            content = """
                ## ¿Qué es el Mindfulness?
                Es la práctica de estar intencionalmente presente en el momento actual, sin juzgar la experiencia. Consiste en observar tus pensamientos y sentimientos desde la distancia, sin dejarte arrastrar por ellos.
                
                ## Beneficios demostrados por la ciencia
                Estudios recientes confirman que la práctica regular del mindfulness:
                - **Reduce el cortisol:** Disminuye la hormona del estrés.
                - **Mejora la concentración:** Al entrenar la mente para volver al presente.
                - **Aumenta la empatía:** Fomenta la comprensión hacia uno mismo y hacia los demás.
                - **Mejora la calidad del sueño:** Al ayudar a "apagar" los pensamientos repetitivos antes de dormir.
                
                ## Un ejercicio rápido para hoy
                Prueba el "Mindfulness del café o té":
                - Mañana, al tomar tu primera bebida caliente, no revises tu teléfono.
                - Siente el calor de la taza en tus manos.
                - Inhala el aroma profundamente.
                - Siente el sabor y la temperatura al dar el primer sorbo.
                Si tu mente se va a los pendientes del día, tráela suavemente de vuelta a la taza.
            """.trimIndent()
        ),
        LibraryArticle(
            id = "general_02",
            title = "Nutrición y Salud Mental",
            summary = "La conexión directa entre lo que comes y cómo te sientes emocionalmente cada día.",
            category = "general",
            readTimeMinutes = 6,
            author = "Dra. Carolina Castro",
            coverImageUrl = "https://picsum.photos/id/244/800/600",
            targetRole = "both",
            content = """
                ## El segundo cerebro
                Tu sistema gastrointestinal alberga millones de neuronas y produce aproximadamente el 90% de la serotonina de tu cuerpo (el neurotransmisor de la felicidad). Por lo tanto, tu dieta influye directamente en tu estado de ánimo.
                
                ## Alimentos que nutren la mente
                - **Ácidos grasos Omega-3:** Presentes en el salmón, las nueces y semillas de chía. Son esenciales para el funcionamiento cerebral y pueden reducir los síntomas de depresión.
                - **Probióticos:** Alimentos como el yogur natural, kéfir y chucrut mantienen el intestino sano, favoreciendo una buena producción de serotonina.
                - **Carbohidratos complejos:** Avena, quinoa y granos enteros liberan energía de forma constante, evitando los picos y caídas de azúcar que generan irritabilidad y ansiedad.
                
                ## Los enemigos del ánimo estable
                - **Azúcar refinada:** Genera un pico rápido de energía seguido de un colapso que se siente como fatiga severa y ansiedad.
                - **Exceso de cafeína:** Puede simular ataques de pánico, causar temblores y arruinar la calidad del sueño, empeorando el estrés.
                
                ## Hidratación
                Incluso una deshidratación leve (apenas el 1-2% del peso corporal) puede afectar el estado de ánimo y la capacidad de concentración. Beber suficiente agua es la intervención de salud mental más rápida y barata a tu disposición.
            """.trimIndent()
        )
    )
}
