export type EstadoVerificacion = "PENDIENTE" | "VERIFICADO" | "RECHAZADO";

export interface SesionIngeniero {
  token: string;
  estadoVerificacion: EstadoVerificacion;
}

const CLAVE_STORAGE = "reconstruyecol_ingeniero_sesion";

export function guardarSesion(sesion: SesionIngeniero): void {
  localStorage.setItem(CLAVE_STORAGE, JSON.stringify(sesion));
}

export function obtenerSesion(): SesionIngeniero | null {
  const crudo = localStorage.getItem(CLAVE_STORAGE);
  if (!crudo) return null;
  try {
    return JSON.parse(crudo) as SesionIngeniero;
  } catch {
    return null;
  }
}

export function cerrarSesion(): void {
  localStorage.removeItem(CLAVE_STORAGE);
}

export function etiquetaEstadoVerificacion(estado: EstadoVerificacion): string {
  switch (estado) {
    case "VERIFICADO":
      return "✅ Verificado";
    case "RECHAZADO":
      return "❌ Rechazado";
    default:
      return "⏳ Pendiente de verificación";
  }
}
