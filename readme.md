
1. ejecutar el comando (teniendo la app docker desktop inicializada) 
```console
docker compose up -d
```
2. ejecutar para levantar los microservicios y sus bases de datos

``` 
iniciar-todo.bat
 ```

3. url peticiones http:
``` 
http://localhost:8081/usuarios
http://localhost:8082/roles
http://localhost:8083/items
http://localhost:8084/carritos
http://localhost:8085/categorias
http://localhost:8086/chats
http://localhost:8087/ventas
http://localhost:8088/valoraciones
http://localhost:8089/pagos
http://localhost:8090/envios
http://localhost:8080  (API Gateway y Swagger centralizado)
``` 
3.1. datos de ejemplo iniciales

Cada microservicio con base de datos crea registros de ejemplo al iniciar. Las inserciones son idempotentes: si el registro ya existe no se vuelve a crear, por lo que reiniciar los servicios no duplica la informacion.

- `ms-usuario`: 3 usuarios demo (`ana.demo@marketplace.cl`, `beto.demo@marketplace.cl` y `carla.demo@marketplace.cl`). La contrasena de todos es `123456`.
- `ms-rol`, `ms-categoria` y `ms-item` mantienen sus registros base de roles, categorias e items.
- `ms-carrito`: 3 carritos de ejemplo con sus items.
- `ms-chat`: 3 conversaciones y 3 mensajes.
- `ms-venta`: 3 ventas de ejemplo.
- `ms-valoracion`: 3 valoraciones de ejemplo.
- `ms-pago`: 3 pagos simulados con estados `PENDIENTE`, `PAGADO` y `RECHAZADO`.
- `ms-envio`: 3 envios de ejemplo.
- `api-gateway` no tiene base de datos, por lo que no crea registros.

4. formato para los post
``` 
    "nombre" : "admin",
    "descripcion" : "rol con permisos de admin"
}
``` 
5. ver en dbeaver las bases de datos y tablas 

6. microservicio de chat (ms-chat, puerto 8086)
Se comunica con `ms-usuario` para validar que los participantes existan y para mostrar sus nombres.

Endpoints:
``` 
POST   /chats                              crear (o reutilizar) una conversación
GET    /chats                              listar conversaciones
GET    /chats/usuario/{usuarioId}          conversaciones de un usuario
GET    /chats/{id}                         ver una conversación
GET    /chats/{id}/mensajes                listar mensajes
POST   /chats/{id}/mensajes                enviar un mensaje
PUT    /chats/{id}/mensajes/leer?usuarioId= marcar mensajes como leídos
DELETE /chats/{id}                         eliminar una conversación
``` 

Los mensajes no se pueden editar: una vez enviados, solo se pueden listar y marcar como leídos.

Formato para crear una conversación (itemId es opcional):
```json
{
  "usuario1Id": 1,
  "usuario2Id": 2,
  "itemId": 10
}
``` 

Formato para enviar un mensaje:
```json
{
  "emisorId": 1,
  "contenido": "Hola, ¿el manga sigue disponible?"
}
``` 

7. microservicio de ventas (ms-venta, puerto 8087)
Administra el estado de cada item. Estados disponibles: `PENDIENTE`, `EN_VENTA` y `VENDIDO`.
Se comunica con `ms-item` para obtener el vendedor y con `ms-usuario` para validar al comprador.

Endpoints:
``` 
POST   /ventas                          registrar el estado inicial de un item (idempotente)
GET    /ventas                          listar ventas
GET    /ventas/item/{itemId}            ver el estado de un item
GET    /ventas/estado/{estado}          filtrar por estado
GET    /ventas/comprador/{compradorId}  compras de un usuario
GET    /ventas/vendedor/{vendedorId}    ventas de un vendedor
PUT    /ventas/{itemId}/estado          cambiar estado
PUT    /ventas/{itemId}/vender          marcar como VENDIDO
DELETE /ventas/{itemId}                 eliminar el registro de venta
``` 

Registrar un item (si no se indica estado queda `EN_VENTA`):
```json
{
  "itemId": 10,
  "estado": "EN_VENTA"
}
``` 

Marcar como vendido:
```json
{
  "compradorId": 2
}
``` 

Importante: `ms-carrito` consulta a `ms-venta` al agregar un item y solo permite agregar items en estado `EN_VENTA`. Si el item está `VENDIDO` o `PENDIENTE`, responde con conflicto.

8. microservicio de valoracion (ms-valoracion, puerto 8088)
Permite reseñar únicamente items en estado `VENDIDO`. Antes de guardar la reseña verifica con `ms-venta` que el usuario de la reseña sea el comprador, y con `ms-item` que el vendedor de la venta coincida con el vendedor del item.

Endpoints:
``` 
POST   /valoraciones                        crear una reseña
GET    /valoraciones                        listar reseñas
GET    /valoraciones/{id}                   ver una reseña
GET    /valoraciones/item/{itemId}          reseñas de un item
GET    /valoraciones/item/{itemId}/promedio promedio y total de reseñas
GET    /valoraciones/usuario/{usuarioId}    reseñas hechas por un usuario
GET    /valoraciones/vendedor/{vendedorId}  reseñas recibidas por un vendedor
DELETE /valoraciones/{id}                   eliminar una reseña
``` 

Formato para crear una reseña:
```json
{
  "itemId": 10,
  "usuarioId": 2,
  "puntuacion": 5,
  "comentario": "Llegó en excelente estado"
}
``` 

Flujo de prueba:
``` 
1. POST /ventas con el itemId (queda EN_VENTA)
2. POST /carritos/items (ms-carrito valida que el item no esté vendido)
3. PUT /ventas/{itemId}/vender con el compradorId
4. POST /valoraciones usando el comprador como usuarioId
``` 

9. microservicio de pago (ms-pago, puerto 8089)
Solo simula los estados de pago, no se conecta a ninguna pasarela real. Estados: `PENDIENTE`, `PAGADO` y `RECHAZADO`.
Cada venta tiene un único pago y todas las transiciones quedan registradas con fecha de actualización.

Endpoints:
``` 
POST   /pagos                       crear (o devolver) el pago de una venta
GET    /pagos                       listar pagos
GET    /pagos/{id}                  ver un pago
GET    /pagos/venta/{ventaId}       ver el pago de una venta
GET    /pagos/estado/{estado}       filtrar por estado
PUT    /pagos/{id}/estado           cambiar estado
PUT    /pagos/{id}/pagar            pasar a PAGADO
PUT    /pagos/{id}/rechazar         pasar a RECHAZADO
DELETE /pagos/{id}                  eliminar el pago
``` 

Crear un pago (siempre inicia en `PENDIENTE`):
```json
{
  "ventaId": 1,
  "monto": 25000
}
``` 

Transiciones permitidas:
``` 
PENDIENTE -> PAGADO | RECHAZADO
RECHAZADO -> PENDIENTE
PAGADO    -> estado final (no cambia)
``` 

10. microservicio de envio (ms-envio, puerto 8090)
Simula los estados de entrega de un pedido. Estados: `EN_PREPARACION`, `EN_CAMINO`, `LLEGANDO` y `ENTREGADO`.
`LLEGANDO` se asigna únicamente al siguiente pedido pendiente de la ruta del repartidor; al entregar un pedido, el siguiente se promueve automáticamente a `LLEGANDO`.
Valida contra `ms-usuario` que el usuario asignado exista y tenga el rol `REPARTIDOR`.

Endpoints:
``` 
POST   /envios                                  crear un envio
GET    /envios                                  listar envios
GET    /envios/{id}                             ver un envio
GET    /envios/venta/{ventaId}                  ver el envio de una venta
GET    /envios/estado/{estado}                  filtrar por estado
GET    /envios/repartidor/{repartidorId}        ruta completa del repartidor
GET    /envios/repartidor/{repartidorId}/resumen panel del repartidor
GET    /envios/repartidor/{repartidorId}/pendientes pedidos por entregar
GET    /envios/repartidor/{repartidorId}/entregados pedidos entregados
GET    /envios/repartidor/{repartidorId}/siguiente  siguiente pedido
PUT    /envios/{id}/estado                      cambiar estado
PUT    /envios/{id}/iniciar                     EN_PREPARACION -> EN_CAMINO
PUT    /envios/{id}/avanzar                     avanzar al siguiente estado
PUT    /envios/{id}/entregar                    marcar ENTREGADO y promover el siguiente
PUT    /envios/{id}/repartidor                  reasignar repartidor
DELETE /envios/{id}                             eliminar un envio
``` 

Crear un envio:
```json
{
  "ventaId": 1,
  "compradorId": 2,
  "repartidorId": 3,
  "direccion": "Calle 10 # 20-30"
}
``` 

El panel `GET /envios/repartidor/{repartidorId}/resumen` devuelve:
```json
{
  "repartidorId": 3,
  "repartidorNombre": "Carlos",
  "totalPedidos": 4,
  "totalEntregados": 1,
  "totalPendientes": 3,
  "siguiente": { "id": 2, "estado": "LLEGANDO", "posicion": 2 },
  "pedidos": [],
  "pendientes": [],
  "entregados": []
}
``` 

Secuencia permitida:
``` 
EN_PREPARACION -> EN_CAMINO -> LLEGANDO -> ENTREGADO
``` 

Flujo completo de prueba (se necesita un usuario con rol `REPARTIDOR`; el rol ya se siembra en `ms-rol`):
``` 
1. POST /ventas con el itemId
2. POST /pagos con el ventaId y el monto
3. PUT  /pagos/{id}/pagar
4. POST /envios con ventaId, compradorId, repartidorId y direccion
5. PUT  /envios/{id}/iniciar
6. PUT  /envios/{id}/avanzar        (pasa a LLEGANDO)
7. PUT  /envios/{id}/entregar       (el siguiente pedido queda en LLEGANDO)
8. GET  /envios/repartidor/{repartidorId}/resumen
``` 

## Documentacion Swagger / OpenAPI

Cada microservicio expone su propia documentacion:

``` 
http://localhost:8081/swagger-ui.html   usuarios
http://localhost:8082/swagger-ui.html   roles
http://localhost:8083/swagger-ui.html   items
http://localhost:8084/swagger-ui.html   carrito
http://localhost:8085/swagger-ui.html   categorias
http://localhost:8086/swagger-ui.html   chat
http://localhost:8087/swagger-ui.html   ventas
http://localhost:8088/swagger-ui.html   valoraciones
http://localhost:8089/swagger-ui.html   pagos
http://localhost:8090/swagger-ui.html   envios
``` 

El gateway tambien expone un unico Swagger UI con todos los servicios en un selector:

``` 
http://localhost:8080/swagger-ui/index.html
``` 

Los documentos OpenAPI individuales se pueden consumir en:

``` 
http://localhost:8080/v3/api-docs/ms-usuario
http://localhost:8080/v3/api-docs/ms-rol
http://localhost:8080/v3/api-docs/ms-item
http://localhost:8080/v3/api-docs/ms-categoria
http://localhost:8080/v3/api-docs/ms-carrito
http://localhost:8080/v3/api-docs/ms-chat
http://localhost:8080/v3/api-docs/ms-venta
http://localhost:8080/v3/api-docs/ms-valoracion
http://localhost:8080/v3/api-docs/ms-pago
http://localhost:8080/v3/api-docs/ms-envio
``` 

Cada operacion documenta sus codigos de respuesta reales con una descripcion:

- `200`, `201` y `204`: peticion exitosa, recurso creado o respuesta sin contenido.
- `400`: faltan campos obligatorios, el formato o el valor es invalido, el estado indicado no existe o se intenta enviar un id autogenerado en la creacion.
- `403`, `404`, `409` y `503`: regla de negocio incumplida, recurso inexistente, conflicto de estado o microservicio dependiente no disponible.
- `500`: error interno del servidor o violacion de una restriccion de la base de datos, por ejemplo un valor unico duplicado.

## API Gateway (api-gateway, puerto 8080)

Centraliza el acceso a todos los microservicios. Se puede usar cualquier endpoint documentado en este readme cambiando `http://localhost:PUERTO` por `http://localhost:8080`.

| Ruta del gateway | Servicio destino |
|---|---|
| `/usuarios/**` | `ms-usuario` (8081) |
| `/roles/**` | `ms-rol` (8082) |
| `/items/**` | `ms-item` (8083) |
| `/carritos/**` | `ms-carrito` (8084) |
| `/categorias/**` | `ms-categoria` (8085) |
| `/chats/**` | `ms-chat` (8086) |
| `/ventas/**` | `ms-venta` (8087) |
| `/valoraciones/**` | `ms-valoracion` (8088) |
| `/pagos/**` | `ms-pago` (8089) |
| `/envios/**` | `ms-envio` (8090) |

Ejemplo usando el gateway:

``` 
GET  http://localhost:8080/pagos
POST http://localhost:8080/envios
GET  http://localhost:8080/envios/repartidor/3/resumen
``` 

El gateway se levanta al final con `iniciar-todo.bat`, despues de todos los microservicios. No usa base de datos propia.
```

