export function sanitizarNumero(numero: string): string {
  return numero.replace(/[^\d+]/g, "").replace(/^\+/, "");
}

export function enlaceWhatsapp(numero: string, mensaje?: string): string {
  const limpio = sanitizarNumero(numero);
  const base = `https://wa.me/${limpio}`;
  return mensaje ? `${base}?text=${encodeURIComponent(mensaje)}` : base;
}
