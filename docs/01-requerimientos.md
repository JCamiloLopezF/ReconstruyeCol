# Documento de Requerimientos
## Plataforma de Conexión de Ayuda Humanitaria — Terremoto Colombia (Chocó, agosto 2026)

**Versión:** 1.0 — MVP
**Contexto:** Terremoto de magnitud 7,4 del 10 de agosto de 2026 (epicentro cerca de San José del Palmar, Chocó), con afectaciones graves en Pereira, Cali, Valle del Cauca, Manizales, Armenia y Quibdó, y cortes de energía/telecomunicaciones en varias zonas.

---

## 1. Objetivo del producto

Construir una plataforma web que **conecte directamente** a personas y organizaciones afectadas por el terremoto con personas y organizaciones que puedan brindar ayuda (donaciones en especie, transporte, maquinaria, conocimiento técnico), **sin intermediar dinero** y con la menor fricción posible para quien solicita ayuda.

La plataforma nace para esta emergencia puntual, pero debe quedar diseñada para **reactivarse en futuros desastres**.

---

## 2. Alcance

### 2.1 Incluido en el MVP (lanzamiento en máximo 3 días)

- Landing con selección "Ayudar" / "Solicitar ayuda", sin registro obligatorio.
- Publicación de solicitudes de ayuda (damnificados, organizaciones/centros de acopio).
- Publicación de ofertas de ayuda (voluntarios: donantes, transportistas, maquinaria, conocimiento general).
- Búsqueda y visualización en mapa por cercanía geográfica.
- Marcado automático de zonas urgentes por concentración de solicitudes.
- Registro y verificación manual de ingenieros/asesores estructurales (único flujo con autenticación).
- Autoregistro de organizaciones y centros de acopio.
- Contacto externo vía WhatsApp o correo (sin chat interno).
- Panel de administración interno (solo el equipo del proyecto).
- Estadísticas públicas básicas (transparencia).

### 2.2 Explícitamente fuera de alcance del MVP

- Aplicación móvil nativa.
- Modo offline / funcionamiento sin conectividad.
- Procesamiento de pagos o donaciones en dinero.
- Integración oficial con UNGRD, alcaldías o Cruz Roja.
- Verificación automatizada de credenciales profesionales.
- Soporte multi-idioma y accesibilidad avanzada (lector de pantalla, alto contraste, etc.).
- Canal alterno por SMS/USSD para zonas sin internet.

> Estos puntos quedan como roadmap post-MVP (ver sección 12 del documento de diseño técnico) porque el contexto real muestra zonas sin señal ni electricidad, y en algún momento valdrá la pena revisarlos — pero no entran en la ventana de 3 días.

---

## 3. Actores y roles

| Rol | ¿Requiere registro? | Descripción | Puede hacer |
|---|---|---|---|
| **Damnificado** | No | Persona afectada que necesita ayuda | Publicar solicitud, ver ofertas cercanas, marcar solicitud como atendida |
| **Voluntario** | No | Persona que dona alimentos, ropa, transporte, maquinaria o conocimiento general | Publicar oferta, ver solicitudes cercanas, contactar |
| **Ingeniero/Asesor estructural** | **Sí** (con verificación manual) | Profesional que ofrece asesoría de daños estructurales | Publicar oferta de asesoría con insignia de verificación |
| **Organización / Centro de acopio** | Autoregistro simple (sin contraseña obligatoria) | Cruz Roja, fundaciones, iglesias, centros de acopio comunitarios | Publicar solicitudes u ofertas a nombre de la organización |
| **Administrador** | Sí (interno) | Equipo del proyecto | Moderar publicaciones, verificar ingenieros, ver estadísticas, exportar reportes |

---

## 4. Historias de usuario

**Damnificado**
- Como damnificado, quiero publicar qué necesito sin crear una cuenta, para pedir ayuda lo más rápido posible.
- Como damnificado, quiero ver en un mapa quién cerca de mí ofrece lo que necesito, para contactarlo directamente.
- Como damnificado, quiero marcar mi solicitud como "atendida" cuando ya recibí ayuda, para que no me sigan contactando.

**Voluntario**
- Como voluntario, quiero publicar qué puedo ofrecer (comida, transporte, maquinaria, tiempo), para que alguien cercano me encuentre.
- Como voluntario, quiero ver qué se necesita cerca de mí, para decidir a quién ayudar.

**Ingeniero estructural**
- Como ingeniero, quiero registrarme y subir mi soporte profesional, para que mi oferta de asesoría se muestre como verificada.
- Como damnificado, quiero distinguir claramente a un ingeniero verificado de uno sin verificar, para confiar en la recomendación sobre mi vivienda.

**Organización**
- Como centro de acopio, quiero registrar mi punto físico, para aparecer en el mapa como lugar de entrega.

**Administrador**
- Como administrador, quiero revisar y aprobar/rechazar el soporte de ingenieros, para evitar suplantación de profesionales.
- Como administrador, quiero eliminar publicaciones fraudulentas o duplicadas, para mantener la confianza en la plataforma.
- Como administrador, quiero exportar estadísticas agregadas, para publicarlas como reporte de transparencia.

---

## 5. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | La landing debe presentar dos opciones claras: "Necesito ayuda" / "Quiero ayudar", sin exigir login. |
| RF-02 | Cualquier persona puede crear una solicitud de ayuda indicando: tipo de ayuda, descripción corta, ubicación (geolocalización o selección manual en mapa), y forma de contacto (WhatsApp y/o correo, al menos una). |
| RF-03 | Cualquier persona puede crear una oferta de ayuda con los mismos campos que una solicitud. |
| RF-04 | El sistema debe mostrar en un mapa las solicitudes/ofertas activas cercanas a la ubicación del usuario, filtrables por tipo de ayuda. |
| RF-05 | El sistema debe agrupar automáticamente solicitudes del mismo tipo en un radio de 100 metros; al superar 3 solicitudes agrupadas, la zona se marca visualmente como "urgente". |
| RF-06 | Los ingenieros/asesores estructurales deben registrarse con: nombre, documento de identidad, universidad, fecha de graduación y soporte (foto de tarjeta profesional o diploma). Su oferta queda en estado "pendiente" hasta aprobación de un administrador. |
| RF-07 | Las organizaciones y centros de acopio pueden autoregistrarse indicando nombre, tipo, ubicación y contacto, sin contraseña obligatoria. |
| RF-08 | El contacto entre las partes se realiza fuera de la plataforma: botón de WhatsApp (enlace directo `wa.me`) y/o correo, según lo que el publicador haya decidido compartir. |
| RF-09 | Toda solicitud/oferta debe poder marcarse como "atendida" por su creador (mediante un enlace/token enviado al momento de la publicación, ya que no hay login). |
| RF-10 | Debe existir un botón de "Reportar" en cada publicación visible al público. |
| RF-11 | El panel de administración permite: ver publicaciones reportadas, eliminarlas, aprobar/rechazar ingenieros, y ver estadísticas agregadas. |
| RF-12 | Debe existir una página pública de estadísticas (número de solicitudes activas/atendidas por tipo y zona), sin exponer datos personales. |

---

## 6. Requerimientos no funcionales

| ID | Requerimiento |
|---|---|
| RNF-01 | La plataforma debe soportar al menos 1.000 usuarios concurrentes en el MVP. |
| RNF-02 | Aplicación web (no nativa), optimizada para uso móvil (mobile-first), asumiendo que la mayoría entrará desde el celular. |
| RNF-03 | El sistema solo se garantiza operativo cuando hay conectividad; no se requiere soporte offline en el MVP. |
| RNF-04 | Debe cumplir con la Ley 1581 de 2012 (protección de datos personales) de Colombia, especialmente para los datos sensibles de los ingenieros verificados. |
| RNF-05 | Los tiempos de respuesta de búsqueda por geolocalización no deben superar ~1-2 segundos bajo carga normal. |
| RNF-06 | La infraestructura debe operar inicialmente sobre servicios gratuitos o de bajo costo, con posibilidad de migrar a infraestructura más robusta después del MVP. |
| RNF-07 | Idioma único: español, para el MVP. |
| RNF-08 | El código y la arquitectura deben quedar preparados para reutilizarse en futuros desastres (configuración por "evento" en vez de datos hardcodeados). |

---

## 7. Reglas de negocio clave

1. **No se maneja dinero.** La plataforma es puramente de intermediación de contacto e información.
2. **No se valida la veracidad de una solicitud o donación.** La verificación queda en manos de quien recibe (centro de acopio, persona). Excepción: los ingenieros estructurales sí pasan por verificación manual de credenciales, dado el riesgo de un mal consejo estructural.
3. **Urgencia por concentración:** más de 3 solicitudes del mismo tipo en un radio de 100 m elevan la zona a "urgente" (ver detalle de algoritmo en el documento técnico).
4. **Moderación reactiva:** el equipo administrador actúa sobre publicaciones reportadas o detectadas como fraudulentas/duplicadas; no hay validación previa de cada publicación (sería imposible de operar con el volumen esperado).

---

## 8. Supuestos y restricciones

- El equipo tiene experiencia previa en Java/Spring Boot, Astro, TypeScript y bases de datos SQL (PostgreSQL), pero poca experiencia desplegando en infraestructura gratuita.
- Plazo máximo de lanzamiento: **3 días**, con una primera versión funcional esperada al día siguiente.
- Presupuesto inicial: **$0** (infraestructura gratuita), con plan de migración posterior.
- La plataforma es independiente; no depende de ninguna integración externa para funcionar en el MVP.

---

## 9. Riesgos

| Riesgo | Impacto | Mitigación |
|---|---|---|
| Publicaciones falsas o duplicadas saturando el mapa | Alto — pierde confianza de los usuarios | Botón de reporte + agrupación automática por cercanía + moderación diaria del equipo |
| Suplantación de ingenieros estructurales | Alto — riesgo físico real si alguien confía en un consejo estructural falso | Verificación manual obligatoria antes de publicar; insignia visible de "verificado" vs "pendiente" |
| Caída del servicio por picos de tráfico en infraestructura gratuita | Medio-Alto | Elegir proveedores con auto-scaling básico incluido en capa gratuita; monitoreo activo los primeros días |
| Zonas sin conectividad no pueden usar la plataforma | Alto (dato real del sismo: hay zonas sin señal/energía) | Aceptado como limitación del MVP; documentar como primer punto del roadmap post-MVP |
| Alcance demasiado ambicioso para 3 días | Alto | Priorizar estrictamente lo listado en 2.1; cualquier idea nueva se anota para después del lanzamiento |
| Exposición de datos personales sensibles (ingenieros) | Medio | Cifrado en reposo, acceso restringido solo a administradores, cumplimiento Ley 1581 |

---

## 10. Criterios de éxito del MVP

- Una persona puede publicar una solicitud de ayuda en menos de 1 minuto, sin registrarse.
- Un voluntario puede encontrar solicitudes cercanas en un mapa en menos de 30 segundos.
- Un ingeniero puede registrarse y quedar en estado "pendiente de verificación" sin bloquear el uso general de la plataforma.
- El equipo administrador puede aprobar/rechazar un ingeniero y eliminar una publicación reportada desde el panel, sin tocar la base de datos manualmente.
- La plataforma soporta el tráfico esperado sin caídas durante el primer fin de semana de lanzamiento.
