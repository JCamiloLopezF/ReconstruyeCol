import http from "k6/http";
import { check } from "k6";

const BASE_URL = __ENV.BASE_URL || "https://reconstruyecol-production.up.railway.app";
const N = Number(__ENV.VUS || 10);
const LAT_BASE = 5.6947;
const LNG_BASE = -76.6584;

// Dispersa los puntos alrededor de Quibdo (+/- ~0.25 grados, ~25km) para no disparar en
// cadena la logica de agrupacion/urgencia (radio de 100m) entre las filas de la prueba.
function coordenadaAleatoria(base) {
  return base + (Math.random() - 0.5) * 0.5;
}

// shared-iterations con vus == iterations == N: garantiza EXACTAMENTE N solicitudes creadas
// en esta corrida (no N-por-VU-por-segundo), para poder limpiar despues con certeza.
export const options = {
  scenarios: {
    carga_post: {
      executor: "shared-iterations",
      vus: N,
      iterations: N,
      maxDuration: __ENV.TEST_DURATION || "120s",
    },
  },
};

export default function () {
  const payload = JSON.stringify({
    tipoAyuda: "OTRO",
    descripcion: `[PRUEBA-CARGA k6] Registro sintetico de prueba de carga (VU ${__VU}, iter ${__ITER})`,
    lat: coordenadaAleatoria(LAT_BASE),
    lng: coordenadaAleatoria(LNG_BASE),
    contactoEmail: "prueba-carga@example.com",
  });

  const res = http.post(`${BASE_URL}/api/solicitudes`, payload, {
    headers: { "Content-Type": "application/json" },
    tags: { name: "POST /api/solicitudes" },
  });

  check(res, { "status 201": (r) => r.status === 201 });
}
