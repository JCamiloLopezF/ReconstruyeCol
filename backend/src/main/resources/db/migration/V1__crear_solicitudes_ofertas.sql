CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE solicitudes (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo_ayuda             VARCHAR(50) NOT NULL,
    descripcion            TEXT NOT NULL,
    ubicacion              GEOMETRY(Point, 4326) NOT NULL,
    urgente                BOOLEAN NOT NULL DEFAULT FALSE,
    solicitudes_agrupadas  INT NOT NULL DEFAULT 1,
    estado                 VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    contacto_whatsapp      VARCHAR(30),
    contacto_email         VARCHAR(255),
    token_gestion          VARCHAR(64) NOT NULL UNIQUE,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_solicitudes_ubicacion ON solicitudes USING GIST (ubicacion);
CREATE INDEX idx_solicitudes_estado_tipo ON solicitudes (estado, tipo_ayuda);

CREATE TABLE ofertas (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo_ayuda        VARCHAR(50) NOT NULL,
    descripcion       TEXT NOT NULL,
    ubicacion         GEOMETRY(Point, 4326) NOT NULL,
    estado            VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    contacto_whatsapp VARCHAR(30),
    contacto_email    VARCHAR(255),
    token_gestion     VARCHAR(64) NOT NULL UNIQUE,
    -- Sin FK todavia: las tablas ingenieros/organizaciones se crean en una migracion posterior.
    ingeniero_id      UUID,
    organizacion_id   UUID,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_ofertas_ubicacion ON ofertas USING GIST (ubicacion);
CREATE INDEX idx_ofertas_estado_tipo ON ofertas (estado, tipo_ayuda);
