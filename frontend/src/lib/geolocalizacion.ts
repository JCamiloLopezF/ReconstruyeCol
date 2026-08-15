export interface Coordenadas {
  lat: number;
  lng: number;
}

const MENSAJES_ERROR: Record<number, string> = {
  1: "No diste permiso para usar tu ubicación. Actívalo en la configuración de tu navegador e intenta de nuevo.",
  2: "No se pudo determinar tu ubicación en este momento. Intenta de nuevo.",
  3: "Se agotó el tiempo para obtener tu ubicación. Intenta de nuevo.",
};

export function obtenerUbicacionActual(): Promise<Coordenadas> {
  return new Promise((resolve, reject) => {
    if (!("geolocation" in navigator)) {
      reject(new Error("Tu navegador no soporta geolocalización."));
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (posicion) => {
        resolve({
          lat: posicion.coords.latitude,
          lng: posicion.coords.longitude,
        });
      },
      (error) => {
        reject(new Error(MENSAJES_ERROR[error.code] ?? "No se pudo obtener tu ubicación."));
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 60000 },
    );
  });
}
