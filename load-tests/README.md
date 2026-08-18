# Pruebas de carga (k6)

Scripts para probar `GET`/`POST /api/solicitudes` bajo carga. Requieren [k6](https://k6.io)
instalado (`brew install k6`).

## Uso

```bash
cd load-tests/k6

# GET (solo lectura, seguro correrlo varias veces)
VUS=100 TEST_DURATION=20s k6 run get-solicitudes.js

# POST (crea VUS filas reales en la base de datos, etiquetadas para poder borrarlas despues)
VUS=100 k6 run post-solicitudes.js
```

Por defecto apuntan a `https://reconstruyecol-production.up.railway.app`. Para apuntar a otro
ambiente: `BASE_URL=http://localhost:8080 k6 run get-solicitudes.js`.

## Limpiar los datos de prueba de POST

Cada solicitud creada por `post-solicitudes.js` queda con la descripción prefijada
`[PRUEBA-CARGA k6]`. Para borrarlas (SQL Editor de Supabase):

```sql
DELETE FROM solicitudes WHERE descripcion LIKE '[PRUEBA-CARGA k6]%';
```

## Resultados de la corrida del 2026-08-17 contra producción

Ver `docs/03-despliegue.md` sección "Capacidad y pruebas de carga" para el resumen y las
recomendaciones. Resumen rápido: falla notablemente desde ~100 VUs concurrentes (6% de error
en GET, 37% en POST), y ~44% a 500 VUs en GET — antes de llegar a los 1.000 solicitados. El
cuello de botella más probable es `DB_POOL_SIZE=5` (HikariCP), ajustado a propósito para el
límite del plan gratuito de Supabase.
