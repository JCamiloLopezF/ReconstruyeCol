# 03 — Despliegue

Sigue lo definido en la sección 10 de `02-diseno-tecnico.md`: **Vercel** (frontend), **Fly.io** (backend, contenedor Docker) y **Supabase** (Postgres + PostGIS).

## 1. Supabase (base de datos)

1. Crear proyecto en [supabase.com](https://supabase.com) (capa gratuita).
2. En el **SQL Editor**, habilitar PostGIS una sola vez:
   ```sql
   create extension if not exists postgis;
   ```
3. En **Project Settings → Database**, copiar la cadena de conexión directa (puerto `5432`, no el pooler `6543` — Fly corre un contenedor persistente, no funciones serverless, así que no necesita PgBouncer).
4. Armar `DB_URL` agregando `sslmode=require` (Supabase lo exige):
   ```
   jdbc:postgresql://db.<ref-proyecto>.supabase.co:5432/postgres?sslmode=require
   ```
5. Flyway (`spring.flyway.enabled: true`) corre las migraciones de `backend/src/main/resources/db/migration` automáticamente al arrancar — no hace falta correrlas a mano.

## 2. Backend en Fly.io

Requiere `flyctl` instalado (`brew install flyctl`) y sesión iniciada (`fly auth login`).

```bash
cd backend
fly launch --no-deploy --copy-config --name reconstruyecol-backend   # usa el fly.toml ya generado
fly secrets set \
  DB_URL="jdbc:postgresql://db.<ref-proyecto>.supabase.co:5432/postgres?sslmode=require" \
  DB_USERNAME="postgres" \
  DB_PASSWORD="<password-supabase>"
fly deploy
```

- El healthcheck de `fly.toml` apunta a `/actuator/health` (Spring Boot Actuator, agregado solo para esto — sin detalles expuestos).
- `min_machines_running = 0`: la máquina se duerme sin tráfico (capa gratuita) y despierta con la primera petición — ver la advertencia de cold start en la sección 10 del diseño técnico. Si el cold start es un problema el día del lanzamiento, subir `min_machines_running` a `1` (deja de ser gratis) o agregar un ping periódico desde un cron externo.
- Region `bog` (Bogotá) por cercanía a los usuarios; cambiar en `fly.toml` si Fly no tiene capacidad ahí.

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
   | `PUBLIC_API_URL` | URL pública del backend en Fly (ej. `https://reconstruyecol-backend.fly.dev`) |
   | `PUBLIC_CONTACTO_ADMIN_EMAIL` | correo del equipo administrador |

No commitear `.vercel/` (ya debería quedar fuera vía `.gitignore` de Vercel) ni los valores de estos secrets.

## 4. Variables de entorno — resumen

| Variable | Dónde se usa | Ejemplo |
|---|---|---|
| `DB_URL` | Fly secret (backend) | `jdbc:postgresql://db.xxxx.supabase.co:5432/postgres?sslmode=require` |
| `DB_USERNAME` | Fly secret (backend) | `postgres` |
| `DB_PASSWORD` | Fly secret (backend) | — |
| `DB_POOL_SIZE` | Fly secret opcional (backend) | `5` (default ya seguro para el plan gratuito de Supabase) |
| `PUBLIC_API_URL` | Build de Vercel (frontend) | `https://reconstruyecol-backend.fly.dev` |
| `PUBLIC_CONTACTO_ADMIN_EMAIL` | Build de Vercel (frontend) | `equipo@reconstruyecol.org` |

## Pendiente (no incluido en esta tarea)

- CI/CD del backend hacia Fly.io: por ahora `fly deploy` es manual. Si se quiere automatizar igual que el frontend, se puede agregar un workflow con `FLY_API_TOKEN` — preguntar antes de agregarlo.
