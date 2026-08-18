import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = __ENV.BASE_URL || "https://reconstruyecol-production.up.railway.app";

// Quibdo, Choco (mismo punto de referencia usado en los tests de integracion del backend).
const LAT = 5.6947;
const LNG = -76.6584;
const RADIO = 5000;

export const options = {
  vus: Number(__ENV.VUS || 10),
  duration: __ENV.TEST_DURATION || "30s",
  thresholds: {
    // No abortamos la corrida por el threshold, solo queda marcado en el resumen final.
    http_req_failed: ["rate<1"],
  },
};

export default function () {
  const url = `${BASE_URL}/api/solicitudes?lat=${LAT}&lng=${LNG}&radio=${RADIO}`;
  const res = http.get(url, { tags: { name: "GET /api/solicitudes" } });
  check(res, { "status 200": (r) => r.status === 200 });
  sleep(1);
}
