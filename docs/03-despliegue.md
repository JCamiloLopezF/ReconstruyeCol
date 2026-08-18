# 03 — Despliegue

Basado en la sección 10 de `02-diseno-tecnico.md` (Vercel + Fly.io/Railway + Supabase), con un ajuste: **Fly.io eliminó su capa gratuita en 2024 y Render también pide verificación de tarjeta** (política anti-abuso que se volvió estándar en la industria en 2026, incluso en planes "gratis"). El backend se despliega en **Railway**, usando el **Trial** de la cuenta nueva: $5 de crédito por 30 días, **sin pedir tarjeta** para crearlo. Stack final: **Vercel** (frontend), **Railway** (backend, contenedor Docker, plan Trial) y **Supabase** (Postgres + PostGIS).

**Importante — esto es temporal, no una solución permanente:** el Trial de Railway dura 30 días o hasta agotar los $5 de crédito, lo que ocurra primero. Antes de esa fecha hay que decidir: agregar tarjeta en Railway (plan Hobby, $5/mes) o mover el backend a otro proveedor. Anotar la fecha de creación de la cuenta y poner un recordatorio.

## 1. Supabase (base de datos)

1. Crear proyecto en [supabase.com](https://supabase.com) (capa gratuita).
2. En el **SQL Editor**, habilitar PostGIS una sola vez:
   ```sql
   create extension if not exists postgis;
   ```
3. En el dashboard del proyecto, botón **"Connect"** (arriba, junto al nombre del proyecto) → pestaña de cadena de conexión → tipo **URI**, modo **Session pooler** — **no "Direct connection"**. La conexión directa de Supabase (`db.<ref-proyecto>.supabase.co`) solo resuelve por IPv6, y Railway (igual que muchos otros hosts) no tiene salida IPv6, lo que produce `SocketException: Network unreachable`. El Session pooler (Supavisor) sí es IPv4, y a diferencia del Transaction pooler (puerto `6543`) soporta bien los prepared statements que usa Hibernate, por lo que es el correcto para un contenedor persistente como el nuestro. Supabase muestra algo como:
   ```
   postgresql://postgres.<ref-proyecto>:[YOUR-PASSWORD]@aws-0-<region>.pooler.supabase.com:5432/postgres
   ```
   Dos diferencias importantes frente a la conexión directa: el **host** cambia a `aws-0-<region>.pooler.supabase.com`, y el **usuario** ya no es `postgres` solo, sino `postgres.<ref-proyecto>` (con el punto y el ref del proyecto pegado — así el pooler sabe a cuál proyecto rutear la conexión).
4. Armar `DB_URL` **sin usuario ni password adentro** — el driver JDBC de Postgres no soporta el formato `usuario:password@host` que usa la URI de Supabase; si se incluye ahí, Java intenta resolverlo como si fuera parte del hostname y falla con `UnknownHostException`. El usuario y password van en las variables separadas `DB_USERNAME` (con el formato `postgres.<ref-proyecto>` del pooler) y `DB_PASSWORD` (que ya lee `application.yml`). Formato correcto, agregando `sslmode=require` (Supabase lo exige):
   ```
   jdbc:postgresql://aws-0-<region>.pooler.supabase.com:5432/postgres?sslmode=require
   ```
   Por ejemplo, si el pooler queda en `aws-0-us-east-1.pooler.supabase.com`, el `DB_URL` completo es `jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require` — nada más (usuario y password van aparte).
5. Flyway (`spring.flyway.enabled: true`) corre las migraciones de `backend/src/main/resources/db/migration` automáticamente al arrancar — no hace falta correrlas a mano.
6. **Bucket privado para soportes de ingenieros** (una sola vez, antes de probar el registro de ingenieros): menú lateral → **Storage** → **New bucket** → nombre `soportes-ingenieros` (o el que se le pase a `SUPABASE_STORAGE_BUCKET`) → dejarlo **Private** (NO marcar "Public bucket" — el soporte de un ingeniero solo debe ser visible para administradores, regla del `CLAUDE.md` raíz). El backend sube ahí vía la API de Storage con la `Secret key` (header `apikey`, no `Authorization: Bearer` — Supabase cambió esto con el nuevo sistema de keys), no hace falta configurar políticas RLS para que el backend suba archivos (la secret key las salta), pero sí para que un futuro panel admin pueda leerlos con URLs firmadas.

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
   - `DB_URL` → `jdbc:postgresql://aws-0-<region>.pooler.supabase.com:5432/postgres?sslmode=require` (ver sección 1 — usar el Session pooler, no la conexión directa)
   - `DB_USERNAME` → `postgres.<ref-proyecto>` (con el punto y el ref, formato del pooler)
   - `DB_PASSWORD` → la contraseña de Supabase
   - `DB_POOL_SIZE` → `5`
   - `JWT_SECRET` → una clave aleatoria de al menos 32 caracteres, ej. generada con `openssl rand -base64 32` (usada para firmar los JWT del login de ingenieros/administradores — **sin esto en producción, el login sigue "funcionando" pero con la clave insegura por defecto del código**, hay que sobreescribirla siempre)
   - `SUPABASE_URL` → `https://<ref-proyecto>.supabase.co` (URL del **proyecto/API**, ojo que es distinta al host de la base de datos `db.<ref-proyecto>.supabase.co` o al del pooler — se ve en el botón **"Connect"** del dashboard, o en Integrations → Data API)
   - `SUPABASE_SECRET_KEY` → en **Settings → API Keys**, la **Secret key** (formato `sb_secret_...`), no la `Publishable key` — necesaria para subir archivos a un bucket privado desde el backend. *(Supabase migró de `anon`/`service_role` a `Publishable`/`Secret` — si tu proyecto es nuevo solo vas a ver estas dos opciones.)*
   - `SUPABASE_STORAGE_BUCKET` → opcional, default `soportes-ingenieros` si no se setea
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
| `DB_URL` | Variable en Railway (backend) | `jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require` (Session pooler, no directa — ver sección 1) |
| `DB_USERNAME` | Variable en Railway (backend) | `postgres.xxxx` (con el ref del proyecto, formato del pooler) |
| `DB_PASSWORD` | Variable en Railway (backend) | — |
| `DB_POOL_SIZE` | Variable en Railway (backend) | `5` (default seguro para el plan gratuito de Supabase) |
| `JWT_SECRET` | Variable en Railway (backend) | clave aleatoria ≥32 caracteres (`openssl rand -base64 32`) — firma los JWT de login |
| `SUPABASE_URL` | Variable en Railway (backend) | `https://xxxx.supabase.co` (URL del proyecto/API, no la de la base de datos) |
| `SUPABASE_SECRET_KEY` | Variable en Railway (backend) | la `Secret key` (`sb_secret_...`) de Settings → API Keys |
| `SUPABASE_STORAGE_BUCKET` | Variable en Railway (backend, opcional) | `soportes-ingenieros` (default si no se setea) |
| `PUBLIC_API_URL` | Build de Vercel (frontend) | `https://reconstruyecol-backend-production.up.railway.app` |
| `PUBLIC_CONTACTO_ADMIN_EMAIL` | Build de Vercel (frontend) | `equipo@reconstruyecol.org` |

## Pendiente (no incluido en esta tarea)

- CI/CD explícito del backend: Railway ya redeploya solo en cada push a `main` que toque `backend/**` una vez conectado el repo (no hace falta workflow de GitHub Actions aparte).
- Decisión sobre qué hacer quedados los 30 días del Trial de Railway (ver advertencia al inicio de este documento) — pendiente de definir con el usuario antes de esa fecha.
