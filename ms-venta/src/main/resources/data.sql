INSERT INTO ventas (item_id, vendedor_id, comprador_id, estado, fecha_creacion, fecha_actualizacion)
SELECT 1, 2, 1, 'VENDIDO', NOW(6), NOW(6) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ventas WHERE item_id = 1);

INSERT INTO ventas (item_id, vendedor_id, comprador_id, estado, fecha_creacion, fecha_actualizacion)
SELECT 2, 2, 3, 'VENDIDO', NOW(6), NOW(6) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ventas WHERE item_id = 2);

INSERT INTO ventas (item_id, vendedor_id, comprador_id, estado, fecha_creacion, fecha_actualizacion)
SELECT 3, 3, 1, 'VENDIDO', NOW(6), NOW(6) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ventas WHERE item_id = 3);
