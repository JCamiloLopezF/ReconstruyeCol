export const TIPOS_AYUDA = [
  { value: "ALIMENTO", label: "Alimentos" },
  { value: "AGUA", label: "Agua potable" },
  { value: "ROPA_ABRIGO", label: "Ropa y abrigo" },
  { value: "ALOJAMIENTO", label: "Alojamiento" },
  { value: "TRANSPORTE", label: "Transporte" },
  { value: "MAQUINARIA", label: "Maquinaria" },
  { value: "MEDICAMENTOS_SALUD", label: "Medicamentos y salud" },
  { value: "ASESORIA_ESTRUCTURAL", label: "Asesoría estructural" },
  { value: "OTRO", label: "Otro" },
] as const;

export type TipoAyuda = (typeof TIPOS_AYUDA)[number]["value"];

export function etiquetaTipoAyuda(tipo: string): string {
  return TIPOS_AYUDA.find((t) => t.value === tipo)?.label ?? tipo;
}
