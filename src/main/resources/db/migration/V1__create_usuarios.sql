-- V1: esquema inicial de ms-usuarios (P1).
-- Pensada para una base de datos vacía en PostgreSQL 16; no usa IF NOT EXISTS
-- para que falle visiblemente si el esquema ya contiene una tabla "usuarios".

CREATE TABLE usuarios (
    id            BIGINT       GENERATED ALWAYS AS IDENTITY,
    nombre        VARCHAR(120) NOT NULL,
    email         VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    rol           VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_usuarios PRIMARY KEY (id),

    -- nombre obligatorio y no compuesto únicamente por espacios en blanco.
    CONSTRAINT ck_usuarios_nombre_no_blank
        CHECK (nombre ~ '\S'),

    -- La aplicación guarda el correo sin espacios externos y en minúsculas;
    -- la BD lo refuerza y rechaza vacíos o con espacios en blanco.
    CONSTRAINT ck_usuarios_email_normalizado
        CHECK (email <> '' AND email = lower(email) AND email !~ '\s'),

    -- Solo se almacena el hash generado por la aplicación, nunca texto vacío.
    CONSTRAINT ck_usuarios_password_hash_no_blank
        CHECK (password_hash ~ '\S'),

    -- Un único rol por usuario en P1, limitado a los valores conocidos.
    CONSTRAINT ck_usuarios_rol
        CHECK (rol IN ('SOLICITANTE', 'AGENTE', 'ADMIN'))
);

-- Índice 1 (único): unicidad de correo sin distinguir mayúsculas/minúsculas.
-- PostgreSQL garantiza la unicidad y rechaza los duplicados en INSERT y UPDATE
-- con un error de integridad (SQLSTATE 23505), incluso con concurrencia; la
-- aplicación Java es responsable de convertir ese error en HTTP 409.
-- También sirve para búsquedas por lower(email). No se crea otro índice sobre
-- email porque sería redundante. La PK ya posee su índice automático.
CREATE UNIQUE INDEX uq_usuarios_email_lower ON usuarios (lower(email));
