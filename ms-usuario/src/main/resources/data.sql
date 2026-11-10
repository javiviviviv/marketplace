-- Usuarios de ejemplo. La contrasena de todos es 123456.
-- Son idempotentes: si el correo ya existe, no se vuelve a insertar.

INSERT INTO usuarios (nombre, correo, password, rol_id)
SELECT 'Ana Torres', 'ana.demo@marketplace.cl', '$2a$10$yQUbgD9SD1HT8tDifoFB.uDeHAS9gxh2wo8yn9ngRGp2n9pggAmoG', 2 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE correo = 'ana.demo@marketplace.cl');

INSERT INTO usuarios (nombre, correo, password, rol_id)
SELECT 'Beto Silva', 'beto.demo@marketplace.cl', '$2a$10$yQUbgD9SD1HT8tDifoFB.uDeHAS9gxh2wo8yn9ngRGp2n9pggAmoG', 2 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE correo = 'beto.demo@marketplace.cl');

INSERT INTO usuarios (nombre, correo, password, rol_id)
SELECT 'Carla Rojas', 'carla.demo@marketplace.cl', '$2a$10$yQUbgD9SD1HT8tDifoFB.uDeHAS9gxh2wo8yn9ngRGp2n9pggAmoG', 3 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE correo = 'carla.demo@marketplace.cl');
