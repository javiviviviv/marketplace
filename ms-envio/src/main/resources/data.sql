-- Envios de ejemplo.
-- Idempotente: venta_id es unico, por lo que cada venta tiene a lo sumo un envio.
-- La ruta del repartidor respeta el orden: el primer pendiente esta en LLEGANDO.

INSERT INTO envios (venta_id, comprador_id, repartidor_id, direccion, estado, posicion, fecha_creacion, fecha_actualizacion, fecha_entrega)
SELECT 1, 1, 3, 'Direccion demo 1', 'LLEGANDO', 1, NOW(6), NOW(6), NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM envios WHERE venta_id = 1);

INSERT INTO envios (venta_id, comprador_id, repartidor_id, direccion, estado, posicion, fecha_creacion, fecha_actualizacion, fecha_entrega)
SELECT 2, 3, 3, 'Direccion demo 2', 'EN_CAMINO', 2, NOW(6), NOW(6), NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM envios WHERE venta_id = 2);

INSERT INTO envios (venta_id, comprador_id, repartidor_id, direccion, estado, posicion, fecha_creacion, fecha_actualizacion, fecha_entrega)
SELECT 3, 1, 3, 'Direccion demo 3', 'EN_PREPARACION', 3, NOW(6), NOW(6), NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM envios WHERE venta_id = 3);
