# 03 — Despliegue

Basado en la sección 10 de `02-diseno-tecnico.md` (Vercel + Fly.io/Railway + Supabase), con un ajuste: **Fly.io eliminó su capa gratuita en 2024 y Render también pide verificación de tarjeta** (política anti-abuso que se volvió estándar en la industria en 2026, incluso en planes "gratis"). El backend se despliega en **Railway**, usando el **Trial** de la cuenta nueva: $5 de crédito por 30 días, **sin pedir tarjeta** para crearlo. Stack final: **Vercel** (frontend), **Railway** (backend, contenedor Docker, plan Trial) y **Supabase** (Postgres + PostGIS).

**Importante — esto es temporal, no una solución permanente:** el Trial de Railway dura 30 días o hasta agotar los $5 de crédito, lo que ocurra primero. Antes de esa fecha hay que decidir: agregar tarjeta en Railway (plan Hobby, $5/mes) o mover el backend a otro proveedor. Anotar la fecha de creación de la cuenta y poner un recordatorio.

## 1. Supabase (base de datos)

1. Crear proyecto en [supabase.com](https://supabase.com) (capa gratuita).
2. En el **SQL Editor**, habilitar PostGIS una sola vez:
   ```sql
   create extension if not exists postgis;
   ```
3. En el dashboard del proyecto, botón **"Connect"** (arriba, junto al nombre del proyecto) → pestaña de cadena de conexión → tipo **URI**, modo **Direct connection** (puerto `5432`, no el pooler `6543` — el backend corre como contenedor persistente, no funciones serverless, así que no necesita PgBouncer). El password viene como placeholder `[YOUR-PASSWORD]`: reemplazarlo por el definido al crear el proyecto (o resetearlo desde Project Settings → Database si no se recuerda).
4. Armar `DB_URL` agregando `sslmode=require` (Supabase lo exige):
   ```
   jdbc:postgresql://db.<ref-proyecto>.supabase.co:5432/postgres?sslmode=require
   ```
5. Flyway (`spring.flyway.enabled: true`) corre las migraciones de `backend/src/main/resources/db/migration` automáticamente al arrancar — no hace falta correrlas a mano.

## 2. Backend en Railway (Trial, sin tarjeta)

No requiere CLI (aunque existe y es opcional). Todo se hace desde el dashboard:

1. Crear cuenta en [railway.com](https://railway.com) (login con GitHub). No pide tarjeta — arranca automáticamente en el plan **Trial** con $5 de crédito.
2. Dashboard → **New Project** → **Deploy from GitHub repo** → seleccionar `ReconstruyeCol` (autorizar la GitHub App de Railway si es la primera vez).
3. Railway crea un servicio apuntando a la raíz del repo. Entrar a ese servicio → **Settings**:
   | Campo | Valor |
   |---|---|
   | Source → Root Directory | `backend` |
   | Source → Branch | `main` |
   | Build → Builder | `Dockerfile` (Railway lo detecta solo al ver `backend/Dockerfile` una vez seteado el Root Directory; si no lo detecta, agregar la variable `RAILWAY_DOCKERFILE_PATH=Dockerfile`) |
   | Deploy → Healthcheck Path | `/actuator/health` |
4. En **Variables**, agregar:
   - `DB_URL` → `jdbc:postgresql://db.<ref-proyecto>.supabase.co:5432/postgres?sslmode=require` (ver sección 1)
   - `DB_USERNAME` → `postgres`
   - `DB_PASSWORD` → la contraseña de Supabase
   - `DB_POOL_SIZE` → `5`
5. **Deploy** → esperar el primer build (~3-5 min: build de Docker + arranque de Spring Boot + migraciones de Flyway).
6. En **Settings → Networking**, generar un dominio público (**Generate Domain**) si no se creó solo. Queda con forma `https://reconstruyecol-backend-production.up.railway.app`.
7. Verificar: `https://<tu-dominio>.up.railway.app/actuator/health` → debe responder `{"status":"UP"}`.

Notas:
- `PORT` no se configura a mano: Railway lo inyecta automáticamente y `application.yml` ya lo lee (`server.port: ${PORT:8080}`).
- A diferencia de Fly/Render, el plan Trial de Railway **no duerme el servicio** por inactividad — corre siempre mientras haya crédito, así que no hay cold start que mitigar por ahora.
- Cada push a `main` que toque `backend/**` (por el Root Directory configurado) dispara un redeploy automático.
- No se agregó ningún `railway.json`/`railway.toml` al repo: la configuración se hizo por dashboard para evitar arriesgar un schema incorrecto contra el plazo de 3 días. Si más adelante se quiere Infra-as-Code, se puede migrar a config-as-code sin tocar el Dockerfile.

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
   | `PUBLIC_API_URL` | URL pública del backend en Railway (ej. `https://reconstruyecol-backend-production.up.railway.app`) |
   | `PUBLIC_CONTACTO_ADMIN_EMAIL` | correo del equipo administrador |

No commitear `.vercel/` (ya debería quedar fuera vía `.gitignore` de Vercel) ni los valores de estos secrets.

## 4. Variables de entorno — resumen

| Variable | Dónde se usa | Ejemplo |
|---|---|---|
| `DB_URL` | Variable en Railway (backend) | `jdbc:postgresql://db.xxxx.supabase.co:5432/postgres?sslmode=require` |
| `DB_USERNAME` | Variable en Railway (backend) | `postgres` |
| `DB_PASSWORD` | Variable en Railway (backend) | — |
| `DB_POOL_SIZE` | Variable en Railway (backend) | `5` (default seguro para el plan gratuito de Supabase) |
| `PUBLIC_API_URL` | Build de Vercel (frontend) | `https://reconstruyecol-backend-production.up.railway.app` |
| `PUBLIC_CONTACTO_ADMIN_EMAIL` | Build de Vercel (frontend) | `equipo@reconstruyecol.org` |

## Pendiente (no incluido en esta tarea)

- CI/CD explícito del backend: Railway ya redeploya solo en cada push a `main` que toque `backend/**` una vez conectado el repo (no hace falta workflow de GitHub Actions aparte).
- Decisión sobre qué hacer quedados los 30 días del Trial de Railway (ver advertencia al inicio de este documento) — pendiente de definir con el usuario antes de esa fecha.
