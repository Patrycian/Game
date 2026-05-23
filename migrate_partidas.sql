-- ============================================================
--  Migración: añade las columnas que faltaban en 'partidas'
--  Ejecutar UNA SOLA VEZ sobre la BD ya existente.
--  IF NOT EXISTS evita errores si ya se ejecutó antes.
-- ============================================================

ALTER TABLE partidas
    ADD COLUMN IF NOT EXISTS pm_actual    INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS tipo_enemigo VARCHAR(20) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS hp_enemigo   INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS pm_enemigo   INTEGER NOT NULL DEFAULT 0;
