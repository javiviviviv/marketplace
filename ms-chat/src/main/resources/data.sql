-- Conversaciones y mensajes de ejemplo.
-- Idempotente: una conversacion se inserta solo si no existe la misma combinacion.

INSERT INTO conversaciones (usuario1_id, usuario2_id, item_id, fecha_creacion, ultima_actividad)
SELECT 1, 2, 1, NOW(6), NOW(6) FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM conversaciones WHERE usuario1_id = 1 AND usuario2_id = 2 AND item_id = 1
);

INSERT INTO conversaciones (usuario1_id, usuario2_id, item_id, fecha_creacion, ultima_actividad)
SELECT 1, 3, 3, NOW(6), NOW(6) FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM conversaciones WHERE usuario1_id = 1 AND usuario2_id = 3 AND item_id = 3
);

INSERT INTO conversaciones (usuario1_id, usuario2_id, item_id, fecha_creacion, ultima_actividad)
SELECT 2, 3, 5, NOW(6), NOW(6) FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM conversaciones WHERE usuario1_id = 2 AND usuario2_id = 3 AND item_id = 5
);

-- Un mensaje de ejemplo por conversacion. El contenido no se edita.

INSERT INTO mensajes (conversacion_id, emisor_id, contenido, fecha_envio, leido)
SELECT c.id, 1, 'Hola, sigue disponible el item?', NOW(6), 1
FROM conversaciones c
WHERE c.usuario1_id = 1 AND c.usuario2_id = 2 AND c.item_id = 1
  AND NOT EXISTS (
      SELECT 1 FROM mensajes m
      WHERE m.conversacion_id = c.id AND m.emisor_id = 1
        AND m.contenido = 'Hola, sigue disponible el item?'
  )
ORDER BY c.id
LIMIT 1;

INSERT INTO mensajes (conversacion_id, emisor_id, contenido, fecha_envio, leido)
SELECT c.id, 1, 'Me interesa el producto, aceptas oferta?', NOW(6), 0
FROM conversaciones c
WHERE c.usuario1_id = 1 AND c.usuario2_id = 3 AND c.item_id = 3
  AND NOT EXISTS (
      SELECT 1 FROM mensajes m
      WHERE m.conversacion_id = c.id AND m.emisor_id = 1
        AND m.contenido = 'Me interesa el producto, aceptas oferta?'
  )
ORDER BY c.id
LIMIT 1;

INSERT INTO mensajes (conversacion_id, emisor_id, contenido, fecha_envio, leido)
SELECT c.id, 2, 'Si, esta disponible. Podemos coordinar la entrega.', NOW(6), 0
FROM conversaciones c
WHERE c.usuario1_id = 2 AND c.usuario2_id = 3 AND c.item_id = 5
  AND NOT EXISTS (
      SELECT 1 FROM mensajes m
      WHERE m.conversacion_id = c.id AND m.emisor_id = 2
        AND m.contenido = 'Si, esta disponible. Podemos coordinar la entrega.'
  )
ORDER BY c.id
LIMIT 1;
