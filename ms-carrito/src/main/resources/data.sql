-- Carritos de ejemplo.
-- Idempotente: se crea un carrito por usuario solo si ese usuario aun no tiene uno.

INSERT INTO carritos (usuario_id, total)
SELECT 1, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM carritos WHERE usuario_id = 1);

INSERT INTO carritos (usuario_id, total)
SELECT 2, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM carritos WHERE usuario_id = 2);

INSERT INTO carritos (usuario_id, total)
SELECT 3, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM carritos WHERE usuario_id = 3);

-- Un item de ejemplo por carrito. Se evita repetirlo si ya esta en ese carrito.

INSERT INTO carrito_items (item_id, nombre, precio, carrito_id)
SELECT 4, 'Sobre Base Set sellado', 60000, c.id
FROM carritos c
WHERE c.usuario_id = 1
  AND NOT EXISTS (
      SELECT 1 FROM carrito_items ci WHERE ci.carrito_id = c.id AND ci.item_id = 4
  );

INSERT INTO carrito_items (item_id, nombre, precio, carrito_id)
SELECT 4, 'Sobre Base Set sellado', 60000, c.id
FROM carritos c
WHERE c.usuario_id = 2
  AND NOT EXISTS (
      SELECT 1 FROM carrito_items ci WHERE ci.carrito_id = c.id AND ci.item_id = 4
  );

INSERT INTO carrito_items (item_id, nombre, precio, carrito_id)
SELECT 5, 'Hunter x hunter tomo 1', 15000, c.id
FROM carritos c
WHERE c.usuario_id = 3
  AND NOT EXISTS (
      SELECT 1 FROM carrito_items ci WHERE ci.carrito_id = c.id AND ci.item_id = 5
  );

-- Mantiene el total del carrito coherente con sus items.
UPDATE carritos c
SET total = COALESCE((SELECT SUM(ci.precio) FROM carrito_items ci WHERE ci.carrito_id = c.id), 0)
WHERE c.usuario_id IN (1, 2, 3);
