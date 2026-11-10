-- datos iniciales

INSERT IGNORE INTO roles (nombre, descripcion) VALUES
('ADMIN', 'Administrador del marketplace'),
('USUARIO', 'Usuario que compra y publica productos'),
('REPARTIDOR', 'Usuario encargado de entregar los pedidos');
