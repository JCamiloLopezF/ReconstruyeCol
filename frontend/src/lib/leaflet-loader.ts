// Carga Leaflet de forma diferida desde CDN: nada se descarga hasta que el
// usuario pide ver el mapa (frontend/CLAUDE.md exige que el mapa nunca
// bloquee el contenido principal ni los formularios).
const LEAFLET_CSS = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.css";
const LEAFLET_JS = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.js";

declare global {
  interface Window {
    L?: unknown;
  }
}

let cargando: Promise<unknown> | null = null;

export function cargarLeaflet(): Promise<unknown> {
  if (window.L) {
    return Promise.resolve(window.L);
  }
  if (cargando) {
    return cargando;
  }

  cargando = new Promise((resolve, reject) => {
    if (!document.querySelector(`link[href="${LEAFLET_CSS}"]`)) {
      const link = document.createElement("link");
      link.rel = "stylesheet";
      link.href = LEAFLET_CSS;
      document.head.appendChild(link);
    }

    const script = document.createElement("script");
    script.src = LEAFLET_JS;
    script.async = true;
    script.onload = () => resolve(window.L);
    script.onerror = () => reject(new Error("No se pudo cargar el mapa. Revisa tu conexión e intenta de nuevo."));
    document.body.appendChild(script);
  });

  return cargando;
}
