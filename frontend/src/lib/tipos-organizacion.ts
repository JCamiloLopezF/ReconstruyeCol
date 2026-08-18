export const TIPOS_ORGANIZACION = [
  { value: "CENTRO_ACOPIO", label: "Centro de acopio" },
  { value: "ONG", label: "ONG / Fundación" },
  { value: "OTRO", label: "Otro" },
] as const;

export type TipoOrganizacion = (typeof TIPOS_ORGANIZACION)[number]["value"];
