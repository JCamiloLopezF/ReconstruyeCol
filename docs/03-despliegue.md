# 03 — Despliegue

Basado en la sección 10 de `02-diseno-tecnico.md` (Vercel + Fly.io/Railway + Supabase), con un ajuste: **Fly.io eliminó su capa gratuita en 2024 y ahora exige tarjeta desde el registro**, así que el backend se despliega en **Render.com** (plan Free, sin tarjeta, soporta Docker directo). Stack final: **Vercel** (frontend), **Render.com** (backend, contenedor Docker) y **Supabase** (Postgres + PostGIS).

## 1. Supabase (base de datos)

1. Crear proyecto en [supabase.com](https://supabase.com) (capa gratuita).
2. En el **SQL Editor**, habilitar PostGIS una sola vez:
   ```sql
   create extension if not exists postgis;
   ```
3. En el dashboard del proyecto, botón **"Connect"** (arriba, junto al nombre del proyecto) → pestaña de cadena de conexión → tipo **URI**, modo **Direct connection** (puerto `5432`, no el pooler `6543` — Render corre un contenedor persistente, no funciones serverless, así que no necesita PgBouncer). El password viene como placeholder `[YOUR-PASSWORD]`: reemplazarlo por el definido al crear el proyecto (o resetearlo desde Project Settings → Database si no se recuerda).
4. Armar `DB_URL` agregando `sslmode=require` (Supabase lo exige):
   ```
   jdbc:postgresql://db.<ref-proyecto>.supabase.co:5432/postgres?sslmode=require
   ```
5. Flyway (`spring.flyway.enabled: true`) corre las migraciones de `backend/src/main/resources/db/migration` automáticamente al arrancar — no hace falta correrlas a mano.

## 2. Backend en Render.com

No requiere CLI ni tarjeta. Todo se hace desde el dashboard usando el Blueprint ya generado (`backend/render.yaml`):

1. Crear cuenta en [render.com](https://render.com) (login con GitHub es lo más rápido).
2. Dashboard → **New +** → **Blueprint** → conectar el repo `ReconstruyeCol`.
3. En **Blueprint Path**, escribir `backend/render.yaml` (el Blueprint no está en la raíz del repo porque es un monorepo).
4. Render lee el archivo, detecta el servicio `reconstruyecol-backend` (Docker, plan Free, healthcheck en `/actuator/health`) y pide valores para las variables marcadas como secretas:
   - `DB_URL` → `jdbc:postgresql://db.<ref-proyecto>.supabase.co:5432/postgres?sslmode=require` (ver sección 1)
   - `DB_USERNAME` → `postgres`
   - `DB_PASSWORD` → la contraseña de Supabase
5. Confirmar y esperar el primer build (~3-5 min: build de Docker + arranque de Spring Boot + migraciones de Flyway).
6. La URL pública queda con forma `https://reconstruyecol-backend.onrender.com`.

Notas:
- `PORT` no se configura a mano: Render lo inyecta automáticamente y `application.yml` ya lo lee (`server.port: ${PORT:8080}`).
- El plan Free se duerme tras 15 minutos sin tráfico entrante (cold start en la siguiente petición) — mismo tipo de advertencia que ya menciona la sección 10 del diseño técnico para cualquier capa gratuita.
- Región `virginia` (no hay región en Sudamérica en el plan Free); cambiar en `render.yaml` si se prefiere otra de las disponibles (`oregon`, `ohio`, `frankfurt`, `singapore`).
- Cada push a `main` que toque `backend/**` dispara un redeploy automático una vez conectado el Blueprint (comportamiento por defecto de Render, no requiere workflow de GitHub Actions aparte).

## 3. Frontend en Vercel

El workflow `.github/workflows/deploy-frontend.yml` despliega automáticamente a producción en cada push a `main` que toque `frontend/**`.

Configurar una sola vez:

1. `cd frontend && npx vercel link` (crea el proyecto en Vercel y genera `.vercel/project.json` con los IDs).
2. En GitHub → Settings → Environments → crear/usar el environment `production` y agregar estos **secrets**:
   | Secret | De dónde sale |
   |---|---|
   | `VERCEL_TOKEN` | vercel.com → Account Settings → Tokens |
   | `VERCEL_ORG_ID` | `.vercel/project.json` tras el `vercel link` |
   | `VERCEL_PROJECT_ID` | `.vercel/project.json` tras el `vercel link` |
   | `PUBLIC_API_URL` | URL pública del backend en Render (ej. `https://reconstruyecol-backend.onrender.com`) |
   | `PUBLIC_CONTACTO_ADMIN_EMAIL` | correo del equipo administrador |

No commitear `.vercel/` (ya debería quedar fuera vía `.gitignore` de Vercel) ni los valores de estos secrets.

## 4. Variables de entorno — resumen

| Variable | Dónde se usa | Ejemplo |
|---|---|---|
| `DB_URL` | Env var secreta en Render (backend) | `jdbc:postgresql://db.xxxx.supabase.co:5432/postgres?sslmode=require` |
| `DB_USERNAME` | Env var secreta en Render (backend) | `postgres` |
| `DB_PASSWORD` | Env var secreta en Render (backend) | — |
| `DB_POOL_SIZE` | Env var en Render (backend) | `5` (ya viene seteado en `render.yaml`, seguro para el plan gratuito de Supabase) |
| `PUBLIC_API_URL` | Build de Vercel (frontend) | `https://reconstruyecol-backend.onrender.com` |
| `PUBLIC_CONTACTO_ADMIN_EMAIL` | Build de Vercel (frontend) | `equipo@reconstruyecol.org` |

## Pendiente (no incluido en esta tarea)

- CI/CD explícito del backend: Render ya redeploya solo en cada push a `main` una vez conectado el Blueprint (no hace falta workflow de GitHub Actions aparte, a diferencia de Fly.io que sí lo hubiera necesitado).
