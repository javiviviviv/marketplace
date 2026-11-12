-- completa el numero de pedido de los envios que ya existian
UPDATE envios
SET numero_pedido = CONCAT('PED-', LPAD(venta_id, 6, '0'))
WHERE numero_pedido IS NULL;

INSERT INTO envios (numero_pedido, venta_id, comprador_id, repartidor_id, direccion, estado, posicion, fecha_creacion, fecha_actualizacion, fecha_entrega)
SELECT 'PED-000001', 1, 1, 3, 'Direccion demo 1', 'LLEGANDO', 1, NOW(6), NOW(6), NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM envios WHERE venta_id = 1);

INSERT INTO envios (numero_pedido, venta_id, comprador_id, repartidor_id, direccion, estado, posicion, fecha_creacion, fecha_actualizacion, fecha_entrega)
SELECT 'PED-000002', 2, 3, 3, 'Direccion demo 2', 'EN_CAMINO', 2, NOW(6), NOW(6), NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM envios WHERE venta_id = 2);

INSERT INTO envios (numero_pedido, venta_id, comprador_id, repartidor_id, direccion, estado, posicion, fecha_creacion, fecha_actualizacion, fecha_entrega)
SELECT 'PED-000003', 3, 1, 3, 'Direccion demo 3', 'EN_PREPARACION', 3, NOW(6), NOW(6), NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM envios WHERE venta_id = 3);
