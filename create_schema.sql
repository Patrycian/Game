-- ============================================================
--  Realm of Shadows – Esquema de base de datos PostgreSQL
--  Ejecutar una sola vez para crear todas las tablas.
-- ============================================================

-- Eliminar tablas si existen (orden inverso por las FK)
DROP TABLE IF EXISTS combates   CASCADE;
DROP TABLE IF EXISTS partidas   CASCADE;
DROP TABLE IF EXISTS personajes CASCADE;
DROP TABLE IF EXISTS jugadores  CASCADE;
DROP TABLE IF EXISTS enemigos   CASCADE;

-- ── 1. Jugadores ─────────────────────────────────────────────
CREATE TABLE jugadores (
    id          SERIAL       PRIMARY KEY,
    nick        VARCHAR(50)  NOT NULL UNIQUE,
    puntuacion  INTEGER      NOT NULL DEFAULT 0,
    fecha_alta  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ── 2. Personajes (héroes que pertenecen a un jugador) ───────
--  tipo: 'MAGO' | 'GUERRERO' | 'CLERIGO'
CREATE TABLE personajes (
    id           SERIAL       PRIMARY KEY,
    nombre       VARCHAR(80)  NOT NULL,
    tipo         VARCHAR(20)  NOT NULL CHECK (tipo IN ('MAGO','GUERRERO','CLERIGO')),
    puntos_golpe INTEGER      NOT NULL CHECK (puntos_golpe >= 0),
    defensa      INTEGER      NOT NULL CHECK (defensa >= 0),
    poder        INTEGER      NOT NULL CHECK (poder >= 0),
    id_jugador   INTEGER      NOT NULL REFERENCES jugadores(id) ON DELETE CASCADE
);

-- ── 3. Partidas (estado guardado de una run en curso) ────────
--  estado: 'EN_CURSO' | 'COMPLETADA' | 'DERROTA'
CREATE TABLE partidas (
    id             SERIAL      PRIMARY KEY,
    id_jugador     INTEGER     NOT NULL REFERENCES jugadores(id)  ON DELETE CASCADE,
    id_personaje   INTEGER     NOT NULL REFERENCES personajes(id) ON DELETE CASCADE,
    fase_actual    INTEGER     NOT NULL DEFAULT 1 CHECK (fase_actual BETWEEN 1 AND 4),
    hp_actual      INTEGER     NOT NULL CHECK (hp_actual >= 0),
    estado         VARCHAR(15) NOT NULL DEFAULT 'EN_CURSO'
                               CHECK (estado IN ('EN_CURSO','COMPLETADA','DERROTA')),
    fecha_guardado TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- ── 4. Combates (historial de cada enfrentamiento) ───────────
--  resultado: 'VICTORIA' | 'DERROTA'
--  tipo_enemigo: 'OGRO' | 'GOBLIN' | 'SAGA' | 'DRAGON'
CREATE TABLE combates (
    id           SERIAL      PRIMARY KEY,
    id_partida   INTEGER     NOT NULL REFERENCES partidas(id) ON DELETE CASCADE,
    fase         INTEGER     NOT NULL CHECK (fase BETWEEN 1 AND 4),
    tipo_enemigo VARCHAR(20) NOT NULL CHECK (tipo_enemigo IN ('OGRO','GOBLIN','SAGA','DRAGON')),
    resultado    VARCHAR(10) NOT NULL CHECK (resultado IN ('VICTORIA','DERROTA')),
    turnos       INTEGER     NOT NULL DEFAULT 0,
    fecha        TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- ── 5. Enemigos (catálogo de tipos de enemigo con sus stats base) ───────────
--  tipo: 'GOBLIN' | 'OGRO' | 'SAGA' | 'DRAGON'
--  es_jefe: true solo para el Dragón (fase 4)
CREATE TABLE enemigos (
    id           SERIAL       PRIMARY KEY,
    tipo         VARCHAR(20)  NOT NULL UNIQUE,
    nombre       VARCHAR(80)  NOT NULL,
    puntos_golpe INTEGER      NOT NULL CHECK (puntos_golpe > 0),
    defensa      INTEGER      NOT NULL CHECK (defensa >= 0),
    poder        INTEGER      NOT NULL CHECK (poder >= 0),
    icono        VARCHAR(10)  NOT NULL,
    es_jefe      BOOLEAN      NOT NULL DEFAULT FALSE
);

-- Datos semilla: stats extraídos de las clases Java
INSERT INTO enemigos (tipo, nombre, puntos_golpe, defensa, poder, icono, es_jefe) VALUES
    ('GOBLIN', 'Goblin Astuto',               60,  5, 15, '👺', FALSE),
    ('OGRO',   'Ogro Brutal',                100, 12, 20, '👹', FALSE),
    ('SAGA',   'Saga Oscura',                 70,  6, 22, '🧟', FALSE),
    ('DRAGON', 'Ignaroth, el Dragón Eterno', 200, 18, 28, '🐉', TRUE);

-- ── Índices de apoyo ─────────────────────────────────────────
CREATE INDEX idx_personajes_jugador ON personajes(id_jugador);
CREATE INDEX idx_partidas_jugador   ON partidas(id_jugador);
CREATE INDEX idx_combates_partida   ON combates(id_partida);
