-- Migra pagos historicos con el estado eliminado REEMBOLSADO a PENDIENTE.
-- Es idempotente: en bases nuevas no afecta ninguna fila.
UPDATE pagos SET estado = 'PENDIENTE' WHERE estado = 'REEMBOLSADO';

-- Pagos de ejemplo. venta_id es unico, por lo que no se duplican.
INSERT INTO pagos (venta_id, monto, estado, fecha_creacion, fecha_actualizacion)
SELECT 1, 85000, 'PAGADO', NOW(6), NOW(6) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM pagos WHERE venta_id = 1);

INSERT INTO pagos (venta_id, monto, estado, fecha_creacion, fecha_actualizacion)
SELECT 2, 25000, 'PENDIENTE', NOW(6), NOW(6) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM pagos WHERE venta_id = 2);

INSERT INTO pagos (venta_id, monto, estado, fecha_creacion, fecha_actualizacion)
SELECT 3, 120000, 'RECHAZADO', NOW(6), NOW(6) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM pagos WHERE venta_id = 3);
