import type { TipoAyuda } from "./tipos-ayuda";
import type { TipoOrganizacion } from "./tipos-organizacion";

const API_URL = import.meta.env.PUBLIC_API_URL || "http://localhost:8080";

export interface CrearPublicacionPayload {
  tipoAyuda: TipoAyuda;
  descripcion: string;
  lat: number;
  lng: number;
  contactoWhatsapp?: string;
  contactoEmail?: string;
}

export interface CrearPublicacionResponse {
  id: string;
  tokenGestion: string;
}

interface PublicacionBase {
  id: string;
  tipoAyuda: TipoAyuda;
  descripcion: string;
  lat: number;
  lng: number;
  contactoWhatsapp: string | null;
  contactoEmail: string | null;
  createdAt: string;
}

export interface SolicitudPublica extends PublicacionBase {
  urgente: boolean;
  solicitudesAgrupadas: number;
}

export type OfertaPublica = PublicacionBase;

export interface BusquedaCercaniaParams {
  lat: number;
  lng: number;
  radio: number;
  tipo?: TipoAyuda;
}

export class ApiError extends Error {}

async function extraerMensajeError(res: Response): Promise<string> {
  try {
    const cuerpo: unknown = await res.json();
    if (cuerpo && typeof cuerpo === "object") {
      const valores = Object.values(cuerpo as Record<string, unknown>).filter(
        (v): v is string => typeof v === "string",
      );
      if (valores.length > 0) {
        return valores.join(" ");
      }
    }
  } catch {
    // el cuerpo no era JSON: se usa el mensaje por defecto
  }
  return "Ocurrió un error inesperado. Intenta de nuevo en unos minutos.";
}

async function manejarRespuesta<T>(res: Response): Promise<T> {
  if (res.ok) {
    return (await res.json()) as T;
  }
  throw new ApiError(await extraerMensajeError(res));
}

async function manejarRespuestaVacia(res: Response): Promise<void> {
  if (res.ok) return;
  throw new ApiError(await extraerMensajeError(res));
}

function encabezadosAuth(token: string): HeadersInit {
  return { Authorization: `Bearer ${token}` };
}

function construirQueryString(params: BusquedaCercaniaParams): string {
  const query = new URLSearchParams({
    lat: String(params.lat),
    lng: String(params.lng),
    radio: String(params.radio),
  });
  if (params.tipo) {
    query.set("tipo", params.tipo);
  }
  return query.toString();
}

export function crearSolicitud(payload: CrearPublicacionPayload): Promise<CrearPublicacionResponse> {
  return fetch(`${API_URL}/api/solicitudes`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  }).then((res) => manejarRespuesta<CrearPublicacionResponse>(res));
}

export function crearOferta(payload: CrearPublicacionPayload): Promise<CrearPublicacionResponse> {
  return fetch(`${API_URL}/api/ofertas`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  }).then((res) => manejarRespuesta<CrearPublicacionResponse>(res));
}

export function buscarSolicitudes(params: BusquedaCercaniaParams): Promise<SolicitudPublica[]> {
  return fetch(`${API_URL}/api/solicitudes?${construirQueryString(params)}`).then((res) =>
    manejarRespuesta<SolicitudPublica[]>(res),
  );
}

export function buscarOfertas(params: BusquedaCercaniaParams): Promise<OfertaPublica[]> {
  return fetch(`${API_URL}/api/ofertas?${construirQueryString(params)}`).then((res) =>
    manejarRespuesta<OfertaPublica[]>(res),
  );
}

export interface IngenieroRegistroPayload {
  nombre: string;
  email: string;
  password: string;
  documentoIdentidad: string;
  universidad: string;
  fechaGraduacion: string;
  soporte: File;
}

export interface IngenieroRegistroResponse {
  id: string;
  estadoVerificacion: "PENDIENTE" | "VERIFICADO" | "RECHAZADO";
}

export function registrarIngeniero(
  payload: IngenieroRegistroPayload,
): Promise<IngenieroRegistroResponse> {
  const form = new FormData();
  form.set("nombre", payload.nombre);
  form.set("email", payload.email);
  form.set("password", payload.password);
  form.set("documentoIdentidad", payload.documentoIdentidad);
  form.set("universidad", payload.universidad);
  form.set("fechaGraduacion", payload.fechaGraduacion);
  form.set("soporte", payload.soporte);

  // Sin header Content-Type manual: el navegador arma el boundary del multipart solo.
  return fetch(`${API_URL}/api/ingenieros/registro`, {
    method: "POST",
    body: form,
  }).then((res) => manejarRespuesta<IngenieroRegistroResponse>(res));
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  rol: string;
  estadoVerificacion: "PENDIENTE" | "VERIFICADO" | "RECHAZADO" | null;
}

export function login(payload: LoginPayload): Promise<LoginResponse> {
  return fetch(`${API_URL}/api/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  }).then((res) => manejarRespuesta<LoginResponse>(res));
}

export type TipoEntidadReportada = "SOLICITUD" | "OFERTA";

export interface ReporteCrearPayload {
  entidadId: string;
  tipoEntidad: TipoEntidadReportada;
  motivo: string;
}

export function crearReporte(payload: ReporteCrearPayload): Promise<void> {
  return fetch(`${API_URL}/api/reportes`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  }).then((res) => manejarRespuestaVacia(res));
}

export interface IngenieroPendiente {
  id: string;
  nombre: string;
  email: string;
  universidad: string;
  fechaGraduacion: string;
  urlSoporte: string;
  estadoVerificacion: "PENDIENTE" | "VERIFICADO" | "RECHAZADO";
  createdAt: string;
}

export function listarIngenierosPendientes(token: string): Promise<IngenieroPendiente[]> {
  return fetch(`${API_URL}/api/admin/ingenieros/pendientes`, {
    headers: encabezadosAuth(token),
  }).then((res) => manejarRespuesta<IngenieroPendiente[]>(res));
}

export function actualizarEstadoIngeniero(
  id: string,
  estado: "VERIFICADO" | "RECHAZADO",
  token: string,
): Promise<void> {
  return fetch(`${API_URL}/api/admin/ingenieros/${id}/estado`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json", ...encabezadosAuth(token) },
    body: JSON.stringify({ estado }),
  }).then((res) => manejarRespuestaVacia(res));
}

export interface ReporteAdmin {
  id: string;
  entidadId: string;
  tipoEntidad: TipoEntidadReportada;
  motivo: string;
  createdAt: string;
  descripcionPublicacion: string | null;
  tipoAyudaPublicacion: TipoAyuda | null;
}

export function listarReportesAdmin(token: string): Promise<ReporteAdmin[]> {
  return fetch(`${API_URL}/api/admin/reportes`, {
    headers: encabezadosAuth(token),
  }).then((res) => manejarRespuesta<ReporteAdmin[]>(res));
}

export function eliminarPublicacionAdmin(
  id: string,
  tipo: TipoEntidadReportada,
  token: string,
): Promise<void> {
  return fetch(`${API_URL}/api/admin/publicaciones/${id}?tipo=${tipo}`, {
    method: "DELETE",
    headers: encabezadosAuth(token),
  }).then((res) => manejarRespuestaVacia(res));
}

export interface ConteoPorTipo {
  tipoAyuda: TipoAyuda;
  activas: number;
  atendidas: number;
}

export interface EstadisticasPublicas {
  solicitudes: ConteoPorTipo[];
  ofertas: ConteoPorTipo[];
}

export function obtenerEstadisticasPublicas(): Promise<EstadisticasPublicas> {
  return fetch(`${API_URL}/api/estadisticas/publicas`).then((res) =>
    manejarRespuesta<EstadisticasPublicas>(res),
  );
}

export interface OrganizacionRegistroPayload {
  nombre: string;
  tipo: TipoOrganizacion;
  lat: number;
  lng: number;
  contacto: string;
}

export interface OrganizacionRegistroResponse {
  id: string;
  tipo: TipoOrganizacion;
  verificada: boolean;
}

export function registrarOrganizacion(
  payload: OrganizacionRegistroPayload,
): Promise<OrganizacionRegistroResponse> {
  return fetch(`${API_URL}/api/organizaciones`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  }).then((res) => manejarRespuesta<OrganizacionRegistroResponse>(res));
}
