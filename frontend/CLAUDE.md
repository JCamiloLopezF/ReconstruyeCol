# CLAUDE.md — Frontend (Astro + TypeScript)

> Colocar este archivo en `frontend/CLAUDE.md`. Se lee junto con el `CLAUDE.md` de la raíz del repositorio.

Contexto general del producto: `../CLAUDE.md`. Diseño técnico detallado: `../docs/02-diseno-tecnico.md`.

## Stack

- Astro + TypeScript
- Leaflet + tiles de OpenStreetMap para el mapa (sin API key, sin costo)
- Evitar frameworks de UI pesados adicionales salvo que el equipo ya tenga uno definido — priorizar HTML/CSS simple y liviano de cargar en móvil con datos limitados

## Comandos

> Actualizar esta sección en cuanto exista el proyecto real generado.

- Instalar dependencias: `npm install`
- Modo desarrollo: `npm run dev`
- Build de producción: `npm run build`

## Reglas específicas del frontend

- **Mobile-first siempre**: diseñar y probar primero en viewport móvil, no al revés — la mayoría de usuarios entrará desde el celular, posiblemente con conexión lenta.
- El mapa (Leaflet) se carga de forma diferida (lazy), nunca bloqueando el contenido principal ni los formularios.
- Los formularios de publicación (solicitud/oferta) son el flujo más crítico del producto: deben cargar rápido y validarse en el cliente sin depender de librerías pesadas.
- Todos los textos visibles al usuario van en español.
- El enlace de contacto por WhatsApp se construye como `https://wa.me/<numero_sin_espacios_ni_simbolos>` — sanitizar el número (quitar espacios, guiones, símbolos) antes de generar el enlace.
- El `token_gestion` que devuelve el backend al crear una solicitud/oferta debe mostrarse claramente en pantalla al usuario (y, si dejó correo, enviárselo también) — es la única forma que tiene de marcarla como atendida después, ya que no hay login.
- Mostrar de forma visible la diferencia entre una oferta de ingeniero "verificada" y "pendiente de verificación" (no solo con color, también con texto/ícono).

## Accesibilidad mínima aunque no sea el foco del MVP

- `label` correctamente asociado a cada input de los formularios.
- Contraste suficiente en los estados "urgente" del mapa — no depender solo del color rojo para comunicar urgencia, agregar también texto o ícono.
