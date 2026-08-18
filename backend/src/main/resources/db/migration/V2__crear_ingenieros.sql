CREATE TABLE ingenieros (
    id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre                    VARCHAR(255) NOT NULL,
    email                     VARCHAR(255) NOT NULL UNIQUE,
    password_hash             VARCHAR(255) NOT NULL,
    documento_identidad_hash  VARCHAR(64) NOT NULL UNIQUE,
    universidad               VARCHAR(255) NOT NULL,
    fecha_graduacion          DATE NOT NULL,
    url_soporte               VARCHAR(500) NOT NULL,
    estado_verificacion       VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    created_at                TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_ingenieros_estado ON ingenieros (estado_verificacion);
