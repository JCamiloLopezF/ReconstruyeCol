export type EstadoVerificacion = "PENDIENTE" | "VERIFICADO" | "RECHAZADO";
export type Rol = "INGENIERO" | "ADMIN";

export interface Sesion {
  token: string;
  rol: Rol;
  estadoVerificacion: EstadoVerificacion | null;
}

const CLAVE_STORAGE = "reconstruyecol_sesion";

export function guardarSesion(sesion: Sesion): void {
  localStorage.setItem(CLAVE_STORAGE, JSON.stringify(sesion));
}

export function obtenerSesion(): Sesion | null {
  const crudo = localStorage.getItem(CLAVE_STORAGE);
  if (!crudo) return null;
  try {
    return JSON.parse(crudo) as Sesion;
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
