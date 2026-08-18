CREATE TABLE reportes (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entidad_id   UUID NOT NULL,
    tipo_entidad VARCHAR(20) NOT NULL,
    motivo       TEXT NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_reportes_entidad ON reportes (entidad_id, tipo_entidad);
