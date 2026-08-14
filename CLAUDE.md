# CLAUDE.md — Plataforma de Ayuda Humanitaria (Terremoto Colombia, agosto 2026)

> Colocar este archivo en la **raíz del repositorio**. Claude Code lo lee automáticamente al empezar cada sesión.

## Qué es este proyecto

Plataforma web que conecta a personas afectadas por el terremoto del 10 de agosto de 2026 (epicentro en Chocó) con voluntarios y organizaciones que pueden ayudarles. **Sin registro** para el flujo principal, **sin manejo de dinero**. Plazo de lanzamiento: 3 días.

Contexto y alcance completo: `docs/01-requerimientos.md`
Diseño técnico completo (arquitectura, modelo de datos, algoritmos, API): `docs/02-diseno-tecnico.md`

**Si tienes cualquier duda de alcance o de una decisión técnica ya tomada, consulta esos dos documentos antes de improvisar una solución.**

## Prioridad #1: velocidad con un mínimo de calidad, no perfección

Hay un plazo de lanzamiento de 3 días.
- Prefiere siempre la solución más simple que cumpla el requerimiento sobre la más "elegante" o "escalable a futuro".
- No optimices prematuramente ni agregues abstracciones que no se necesitan hoy.
- No implementes nada fuera de lo listado en la sección "Alcance del MVP" de `docs/01-requerimientos.md` sin preguntar primero — cada feature extra le resta tiempo al lanzamiento.

## Estructura del repositorio

```
/backend    → Spring Boot (Java), API REST — ver backend/CLAUDE.md
/frontend   → Astro + TypeScript — ver frontend/CLAUDE.md
/docs       → documentos de requerimientos y diseño técnico
```

## Reglas de negocio que NUNCA se deben romper

1. No hay registro ni login para damnificados ni voluntarios. Solo se autentican **ingenieros estructurales** y **administradores**.
2. La plataforma nunca procesa pagos ni maneja dinero — es puramente de intermediación de contacto.
3. Toda ubicación se guarda como `geometry(Point, 4326)` (PostGIS), no como columnas sueltas de lat/lng.
4. El documento de identidad de un ingeniero se guarda **hasheado**, nunca en texto plano. Su foto de soporte va en almacenamiento restringido, solo visible para administradores.
5. La agrupación de solicitudes cercanas (100 m, mismo tipo de ayuda) **agrupa y cuenta, nunca descarta** los datos de las solicitudes individuales (ver sección 5 de `docs/02-diseno-tecnico.md`).
6. Toda publicación pública debe tener botón de "Reportar".
7. Un ingeniero puede publicar aunque esté "pendiente de verificación", pero su oferta debe mostrarse claramente etiquetada como tal hasta que un administrador la apruebe.

## Convenciones generales

- Nombres de variables, funciones, clases: **inglés**.
- Textos de interfaz y mensajes al usuario final: **español**.
- Commits en español, formato `tipo: descripción corta` (ej: `feat: endpoint de creacion de solicitudes`, `fix: validacion de radio en busqueda`).
- Nunca subir secretos ni credenciales al repo — todo va en variables de entorno (`.env`, incluido en `.gitignore` desde el primer commit).

## Definición de "terminado" para cualquier tarea

- El build pasa sin errores.
- Si se tocó el backend: existe al menos una prueba de integración para el endpoint nuevo o modificado.
- Si se agregó o modificó un campo con dato personal: se revisó si aplica la sección 8 (privacidad / Ley 1581) de `docs/02-diseno-tecnico.md`.
- No queda código muerto, `TODO` sin explicación, ni `console.log` / `System.out.println` de depuración.

## Cuándo preguntar en vez de asumir

- Cualquier cambio de alcance frente a `docs/01-requerimientos.md`.
- Cualquier decisión que implique costo (proveedor de pago en vez de capa gratuita).
- Cualquier cambio a la lógica de agrupación/urgencia o al flujo de verificación de ingenieros — son las dos reglas más sensibles del producto (riesgo de fraude y riesgo físico real, respectivamente).
