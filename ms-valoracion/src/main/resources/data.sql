INSERT INTO valoraciones (item_id, usuario_id, vendedor_id, puntuacion, comentario, fecha_creacion)
SELECT 1, 1, 2, 5, 'Excelente estado y entrega rapida.', NOW(6) FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM valoraciones WHERE item_id = 1 AND usuario_id = 1
);

INSERT INTO valoraciones (item_id, usuario_id, vendedor_id, puntuacion, comentario, fecha_creacion)
SELECT 2, 3, 2, 4, 'Buen producto, tal como se describia.', NOW(6) FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM valoraciones WHERE item_id = 2 AND usuario_id = 3
);

INSERT INTO valoraciones (item_id, usuario_id, vendedor_id, puntuacion, comentario, fecha_creacion)
SELECT 3, 1, 3, 5, 'Todo perfecto con el vendedor.', NOW(6) FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM valoraciones WHERE item_id = 3 AND usuario_id = 1
);
