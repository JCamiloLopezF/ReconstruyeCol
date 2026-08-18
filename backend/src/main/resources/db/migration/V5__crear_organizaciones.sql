CREATE TABLE organizaciones (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      VARCHAR(255) NOT NULL,
    tipo        VARCHAR(30) NOT NULL,
    ubicacion   GEOMETRY(Point, 4326) NOT NULL,
    contacto    VARCHAR(255) NOT NULL,
    verificada  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_organizaciones_ubicacion ON organizaciones USING GIST (ubicacion);

-- Solicitudes no tenia organizacion_id todavia (ofertas si, pero sin FK -- ver V1).
ALTER TABLE solicitudes ADD COLUMN organizacion_id UUID;

ALTER TABLE solicitudes
    ADD CONSTRAINT fk_solicitudes_organizacion FOREIGN KEY (organizacion_id) REFERENCES organizaciones (id);

ALTER TABLE ofertas
    ADD CONSTRAINT fk_ofertas_organizacion FOREIGN KEY (organizacion_id) REFERENCES organizaciones (id);
