INSERT INTO items (nombre, anio, precio, vendedor_id, categoria_id)
SELECT 'Nintendo 3DS XL azul', 2013, 85000, 2, 1 FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM items
    WHERE nombre = 'Nintendo 3DS XL azul' AND vendedor_id = 2 AND categoria_id = 1
);

INSERT INTO items (nombre, anio, precio, vendedor_id, categoria_id)
SELECT 'Pokémon X', 2013, 25000, 2, 2 FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM items
    WHERE nombre = 'Pokémon X' AND vendedor_id = 2 AND categoria_id = 2
);

INSERT INTO items (nombre, anio, precio, vendedor_id, categoria_id)
SELECT 'Charizard holográfico', 1999, 120000, 3, 3 FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM items
    WHERE nombre = 'Charizard holográfico' AND vendedor_id = 3 AND categoria_id = 3
);

INSERT INTO items (nombre, anio, precio, vendedor_id, categoria_id)
SELECT 'Sobre Base Set sellado', 1999, 60000, 3, 4 FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM items
    WHERE nombre = 'Sobre Base Set sellado' AND vendedor_id = 3 AND categoria_id = 4
);

INSERT INTO items (nombre, anio, precio, vendedor_id, categoria_id)
SELECT 'Hunter x hunter tomo 1', 1998, 15000, 2, 5 FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM items
    WHERE nombre = 'Hunter x hunter tomo 1' AND vendedor_id = 2 AND categoria_id = 5
);
