# Marketplace de microservicios

Proyecto académico desarrollado con **Spring Boot 4**, **Spring Data JPA**, **MySQL**, **OpenFeign**, **Spring Cloud Gateway** y **Swagger/OpenAPI**.

El sistema simula un marketplace en el que un usuario publica productos, otro usuario los agrega a un carrito, los compra, paga, recibe el pedido por medio de un repartidor y finalmente puede dejar una reseña del vendedor. El chat entre comprador y vendedor es una funcionalidad independiente y opcional.

## Acceso a Swagger

Toda la documentación y prueba de endpoints está centralizada en:

```text
http://localhost:8080/swagger-ui/index.html
```

El gateway expone los servicios bajo `http://localhost:8080` y desde su Swagger se puede seleccionar el microservicio que se quiera probar (Usuarios, Roles, Items, Categorías, Carrito, Chat, Ventas, Valoraciones, Pagos y Envíos).

---

## Arquitectura general

Cada microservicio tiene su propia base de datos MySQL. No hay una base de datos compartida: la integración se hace por HTTP con OpenFeign.

| Microservicio | Responsabilidad | Entidad principal | Depende de | Puerto interno |
|---|---|---|---|---|
| `ms-rol` | Administrar los roles del sistema | `Rol` | Ninguno | 8082 |
| `ms-usuario` | Registrar usuarios y asociarles un rol | `Usuario` | `ms-rol` | 8081 |
| `ms-categoria` | Administrar las categorías de productos | `Categoria` | Ninguno | 8085 |
| `ms-item` | Publicar productos de un vendedor en una categoría | `Item` | `ms-categoria` | 8083 |
| `ms-carrito` | Armar el carrito y validar disponibilidad | `Carrito` / `CarritoItem` | `ms-item`, `ms-venta` | 8084 |
| `ms-chat` | Conversaciones y mensajes entre usuarios | `Conversacion` / `Mensaje` | `ms-usuario` | 8086 |
| `ms-venta` | Estado de venta de cada item | `Venta` | `ms-item`, `ms-usuario` | 8087 |
| `ms-pago` | Simular el pago de una venta | `Pago` | Ninguno | 8089 |
| `ms-envio` | Asignar pedidos a repartidores y seguir la entrega | `Envio` | `ms-usuario` | 8090 |
| `ms-valoracion` | Reseñas de items vendidos y promedio de puntuación | `Valoracion` | `ms-item`, `ms-venta` | 8088 |
| `api-gateway` | Enrutar las peticiones y centralizar Swagger | — | Todos | 8080 |

### Comunicación entre microservicios

```text
ms-rol  <──────────────  ms-usuario
ms-categoria  <────────  ms-item
ms-item  <─────────────  ms-carrito
ms-venta  <────────────  ms-carrito
ms-item  <─────────────  ms-venta
ms-usuario  <──────────  ms-venta
ms-item  <─────────────  ms-valoracion
ms-venta  <────────────  ms-valoracion
ms-usuario  <──────────  ms-envio
ms-usuario  <──────────  ms-chat
```

`ms-pago` no llama a ningún otro microservicio: recibe directamente el `ventaId` que debe pagar.

### Entidades principales

| Microservicio | Entidad | Campos relevantes |
|---|---|---|
| `ms-rol` | `Rol` | `id`, `nombre` único, `descripcion` |
| `ms-usuario` | `Usuario` | `id`, `nombre`, `correo` único, `password` (BCrypt), `rolId` |
| `ms-categoria` | `Categoria` | `id`, `nombre` único, `descripcion` |
| `ms-item` | `Item` | `id`, `nombre`, `anio`, `precio`, `vendedorId`, `categoriaId` |
| `ms-carrito` | `Carrito` y `CarritoItem` | `usuarioId`, `total`; el detalle guarda `itemId`, nombre y precio |
| `ms-chat` | `Conversacion` y `Mensaje` | participantes, `itemId` opcional, fechas; mensaje con `emisorId`, `contenido`, `fechaEnvio` y `leido` |
| `ms-venta` | `Venta` | `itemId` único, `vendedorId`, `compradorId`, `estado`, fechas |
| `ms-pago` | `Pago` | `ventaId` único, `monto`, `estado`, fechas |
| `ms-envio` | `Envio` | `id`, `numeroPedido` único, `ventaId` único, `compradorId`, `repartidorId`, `direccion`, `estado`, `posicion`, fechas |
| `ms-valoracion` | `Valoracion` | `itemId`, `usuarioId`, `vendedorId`, `puntuacion` (1 a 5), `comentario`, fecha |

---

## Flujo completo del marketplace

### 1. Un usuario con rol vende un producto de una categoría

1. Se crea un rol en `ms-rol`.
2. Se crea un usuario en `ms-usuario` indicando ese `rolId`. `ms-usuario` consulta `ms-rol` para validar que el rol exista.
3. Se crea una categoría en `ms-categoria`.
4. Se crea un item en `ms-item` indicando `vendedorId` y `categoriaId`. `ms-item` consulta `ms-categoria` para validar que la categoría exista.

El `vendedorId` del item es una referencia lógica al usuario vendedor. En la implementación actual `ms-item` **no** consulta `ms-usuario`, por lo que no valida que el vendedor exista ni que tenga un rol específico; esa validación se asume en el flujo de negocio.

### 2. El item deja de ser comprable cuando no está disponible

La disponibilidad no se guarda en `ms-item`. La fuente de verdad es `ms-venta`, que tiene un registro por item y uno de estos estados:

| Estado | Significado |
|---|---|
| `PENDIENTE` | La venta existe pero todavía no está publicada para la compra |
| `EN_VENTA` | El item está disponible y se puede agregar al carrito |
| `VENDIDO` | El item ya fue comprado y no se puede volver a agregar ni vender |

Cuando `ms-carrito` intenta agregar un item, consulta su venta en `ms-venta`:

- si la venta está `VENDIDO`, responde `409` ("ya fue vendido");
- si está en cualquier estado distinto de `EN_VENTA`, responde `409` ("no está disponible");
- si está `EN_VENTA`, lo agrega al carrito.

Además, un usuario no puede agregar su propio producto ni el mismo item dos veces al mismo carrito.

### 3. Carrito → venta → pago

1. **Carrito:** `POST /carritos/items` agrega el item y, si es la primera vez que se consulta, registra automáticamente la venta en `ms-venta` con estado `EN_VENTA`. Es decir, `ms-carrito` llama a `ms-venta` mediante Feign.
2. **Venta:** cuando el comprador decide comprar, se marca la venta como `VENDIDO` con `PUT /ventas/{itemId}/vender` indicando el `compradorId`. En ese momento el item deja de estar disponible para cualquier otro carrito.
3. **Pago:** se crea el pago con `POST /pagos` usando el `ventaId` y el monto. Nace en `PENDIENTE` y se confirma con `PUT /pagos/{id}/pagar`, pasando a `PAGADO`.

`PAGADO` es un estado final del pago. También se puede pasar de `PENDIENTE` a `RECHAZADO` y de `RECHAZADO` a `PENDIENTE`.

### 4. Después del pago comienza el envío

Una vez que el pago está `PAGADO`, se crea el envío con `POST /envios`, indicando:

- `ventaId`,
- `compradorId`,
- `repartidorId`,
- `direccion`.

En la implementación actual **no existe una llamada automática desde `ms-pago` hacia `ms-envio`**. El envío se crea explícitamente después de confirmar el pago; el flujo esperado es respetar ese orden.

Al crear el envío:

- se valida que la venta no tenga ya un envío (relación 1 a 1),
- se valida que el comprador exista,
- se valida que el repartidor exista y tenga el rol `REPARTIDOR` (consulta a `ms-usuario`),
- se genera automáticamente un número de pedido con el formato `PED-000001`,
- el envío queda en estado `EN_PREPARACION` y al final de la ruta del repartidor.

### 5. Lista ordenada de pedidos del repartidor

Cada envío tiene una `posicion` dentro de la ruta de su repartidor. El repartidor solo puede tener un pedido "en curso" a la vez:

- el primer pedido pendiente de la ruta es el único que puede estar en `LLEGANDO`;
- los pedidos deben entregarse en orden;
- al marcar un pedido como `ENTREGADO`, el siguiente pendiente pasa automáticamente a `LLEGANDO`.

Estados del envío:

```text
EN_PREPARACION → EN_CAMINO → LLEGANDO → ENTREGADO
```

El panel del repartidor se puede consultar con `GET /envios/repartidor/{repartidorId}/resumen`, que devuelve todos los pedidos, los pendientes, los entregados, el siguiente pedido y los conteos.

### 6. Reseña al vendedor

Después de la compra, el comprador puede crear una valoración con `POST /valoraciones`. Las validaciones actuales son:

- el item debe existir;
- el item debe tener una venta registrada;
- la venta debe estar en estado `VENDIDO`;
- el `usuarioId` que valora debe ser el `compradorId` de la venta;
- el vendedor de la venta debe coincidir con el vendedor del item;
- el vendedor no puede valorar su propio item;
- un usuario solo puede valorar una vez cada item.

La valoración guarda el `vendedorId`, por lo que sirve para reseñar al vendedor a través del item comprado. Se puede consultar por item, por usuario y por vendedor, y existe un promedio por item.

**Sobre exigir el estado `ENTREGADO`:** en el código actual la reseña **no** depende de `ms-envio`; basta con que la venta esté `VENDIDO`. Por lo tanto, no es necesario cambiar el estado del envío para poder valorar. Si se quisiera una regla más estricta ("solo se puede reseñar después de entregado"), habría que agregar una consulta desde `ms-valoracion` hacia `ms-envio`. También es válido usar la reseña para calificar la comunicación con el vendedor, de forma similar a una red social, sin esperar la entrega: en ese caso el chat y la valoración son complementarios.

---

## funcionamiento del chat:

- Se crea una conversación entre dos usuarios con `POST /chats` indicando `usuario1Id`, `usuario2Id` y, opcionalmente, `itemId`.
- Si ya existe una conversación entre esos dos usuarios (en cualquier orden) y, cuando se indica, para el mismo item, se devuelve la existente en lugar de duplicarla. Si no se indica `itemId`, se reutiliza cualquier conversación previa entre ambos.
- Los mensajes se envían con `POST /chats/{id}/mensajes` indicando `emisorId` y `contenido`.
- Solo pueden enviar mensajes los dos participantes de la conversación.
- Los mensajes son inmutables: una vez enviados no se editan. Solo se pueden marcar como leídos con `PUT /chats/{id}/mensajes/leer?usuarioId=...`.
- `GET /chats/{id}/mensajes` devuelve los mensajes en orden cronológico.
- `GET /chats/usuario/{usuarioId}` lista las conversaciones de un usuario, ordenadas por última actividad.
- Eliminar una conversación elimina también sus mensajes.

El chat depende de `ms-usuario` para validar que los participantes existan y para mostrar sus nombres y correos. Si `ms-usuario` no está disponible, las conversaciones se pueden seguir consultando por id, pero no se pueden crear conversaciones ni enviar mensajes nuevos.

El `itemId` de la conversación es solo una referencia lógica para saber sobre qué producto se habla; no se valida contra `ms-item`.

---

## ¿Qué hace `iniciar-todo.bat`?

El script automatiza el arranque completo del proyecto en Windows:

1. **Detiene instancias anteriores:** busca procesos `java.exe` cuyos comandos correspondan a los jars `ms-*.jar` o `api-gateway-*.jar` y los cierra. También libera los puertos `8080` a `8090`.
2. **Compila todo el proyecto:** ejecuta `mvn clean package -DskipTests`. Si la compilación falla, muestra el error, hace una pausa y termina.
3. **Levanta MySQL:** ejecuta `docker compose up -d` para iniciar los diez contenedores de base de datos y espera 5 segundos.
4. **Levanta el API Gateway:** abre una ventana para `api-gateway` en el puerto `8080`.
5. **Levanta los servicios en orden de dependencia:** primero `ms-rol` y `ms-categoria`, luego `ms-usuario` y `ms-item`, después `ms-venta`, luego `ms-pago` y `ms-envio`, y finalmente `ms-carrito`, `ms-chat` y `ms-valoracion`. Entre grupos espera unos segundos.
6. **Muestra la URL de Swagger:** `http://localhost:8080/swagger-ui/index.html`.
7. **Deja una pausa final** y una ventana de consola por cada servicio.

Requisitos: tener instalado **Java 17**, **Maven** y **Docker Desktop** en ejecución. El script no inicia Docker Desktop por sí solo; si Docker está apagado, `docker compose up -d` falla.

Para detener todo se pueden cerrar las ventanas de los servicios o volver a ejecutar el script, que primero los detiene. Los contenedores de MySQL siguen activos entre ejecuciones; para apagarlos se usa `docker compose down` (agregar `-v` solo si además se quieren borrar los datos).

---

## Guía de funcionamiento paso a paso

Los ejemplos usan el gateway en `http://localhost:8080`. Los ids son ilustrativos: en cada caso se debe usar el id devuelto por la creación anterior.

### 1. Crear un rol

```http
POST /roles
Content-Type: application/json

{
  "nombre": "VENDEDOR",
  "descripcion": "Usuario que publica productos en el marketplace"
}
```

Respuesta esperada: `201 Created` con el rol y su `id`.

> El sistema ya trae roles de ejemplo como `ADMIN`, `USUARIO` y `REPARTIDOR`. El rol `REPARTIDOR` es el que se valida al crear un envío.

### 2. Crear un usuario

```http
POST /usuarios
Content-Type: application/json

{
  "nombre": "María Pérez",
  "correo": "maria@marketplace.cl",
  "password": "123456",
  "rolId": 1
}
```

Respuesta esperada: `201 Created`. La contraseña se guarda hasheada con BCrypt.

Para el comprador se crea otro usuario con el rol `USUARIO`. Para el repartidor se crea un usuario con el rol `REPARTIDOR`.

> No hay login ni autenticación con token: los endpoints reciben directamente los ids de los usuarios.

### 3. Crear una categoría

```http
POST /categorias
Content-Type: application/json

{
  "nombre": "Tecnología",
  "descripcion": "Computadores, celulares y accesorios"
}
```

Respuesta esperada: `201 Created` con el `id` de la categoría.

### 4. Crear un item

```http
POST /items
Content-Type: application/json

{
  "nombre": "Notebook Lenovo",
  "anio": 2024,
  "precio": 750000,
  "vendedorId": 2,
  "categoriaId": 4
}
```

Respuesta esperada: `201 Created`. `ms-item` valida que la categoría exista.

### 5. Crear un chat (opcional)

```http
POST /chats
Content-Type: application/json

{
  "usuario1Id": 3,
  "usuario2Id": 2,
  "itemId": 10
}
```

Respuesta esperada: `201 Created` si la conversación es nueva, o `200 OK` si ya existía.

Enviar un mensaje:

```http
POST /chats/{id}/mensajes
Content-Type: application/json

{
  "emisorId": 3,
  "contenido": "¿El notebook sigue disponible?"
}
```

Ver los mensajes:

```http
GET /chats/{id}/mensajes
```

Marcar como leídos los mensajes recibidos:

```http
PUT /chats/{id}/mensajes/leer?usuarioId=2
```

### 6. Crear el carrito y agregar el item

```http
POST /carritos/items
Content-Type: application/json

{
  "usuarioId": 3,
  "itemId": 10
}
```

Respuesta esperada: `201 Created` con el carrito y su total.

Internamente:

1. `ms-carrito` consulta el item en `ms-item`.
2. Rechaza la operación si el comprador es el vendedor.
3. Registra o consulta la venta en `ms-venta`.
4. Rechaza si el item está `VENDIDO`, si no está `EN_VENTA` o si ya está en el carrito.
5. Guarda el item y recalcula el total.

Consultar el carrito de un usuario:

```http
GET /carritos/{usuarioId}
```

### 7. Crear/confirmar la venta

Al agregar el item al carrito ya se creó la venta en estado `EN_VENTA`. Para confirmar la compra se marca como `VENDIDO` indicando el comprador:

```http
PUT /ventas/{itemId}/vender
Content-Type: application/json

{
  "compradorId": 3
}
```

Respuesta esperada: `200 OK` con la venta en estado `VENDIDO`.

Validaciones: la venta debe existir, no puede estar ya `VENDIDO`, el vendedor no puede comprar su propio item y el comprador debe existir.

Estados de la venta: `PENDIENTE`, `EN_VENTA`, `VENDIDO`.

También se puede cambiar el estado de forma explícita:

```http
PUT /ventas/{itemId}/estado
Content-Type: application/json

{
  "estado": "EN_VENTA",
  "compradorId": null
}
```

Para pasar a `VENDIDO` se requiere `compradorId`.

> Marcar una venta como `VENDIDO` no vacía automáticamente un carrito que ya tuviera el item. El bloqueo se aplica a los nuevos intentos de agregarlo.

### 8. Crear y pagar

Crear el pago:

```http
POST /pagos
Content-Type: application/json

{
  "ventaId": 4,
  "monto": 750000
}
```

Respuesta esperada: `201 Created` en estado `PENDIENTE`. Si la venta ya tenía un pago, devuelve el existente con `200 OK`.

Confirmar el pago:

```http
PUT /pagos/{id}/pagar
```

Respuesta esperada: `200 OK` con estado `PAGADO`.

Otras operaciones:

```http
PUT /pagos/{id}/rechazar
PUT /pagos/{id}/estado   { "estado": "PENDIENTE" }
```

Transiciones permitidas: `PENDIENTE → PAGADO`, `PENDIENTE → RECHAZADO` y `RECHAZADO → PENDIENTE`. `PAGADO` es final.

> `ms-pago` no valida el estado de la venta ni crea el envío automáticamente. El orden del flujo se controla desde el cliente.

### 9. Crear el envío

Antes se debe tener un usuario con rol `REPARTIDOR`. Luego:

```http
POST /envios
Content-Type: application/json

{
  "ventaId": 4,
  "compradorId": 3,
  "repartidorId": 5,
  "direccion": "Av. Providencia 1234, Santiago"
}
```

Respuesta esperada: `201 Created`:

```json
{
  "id": 4,
  "numeroPedido": "PED-000004",
  "ventaId": 4,
  "compradorId": 3,
  "repartidorId": 5,
  "direccion": "Av. Providencia 1234, Santiago",
  "estado": "EN_PREPARACION",
  "posicion": 1,
  "fechaCreacion": "2026-11-11T22:36:11",
  "fechaActualizacion": "2026-11-11T22:36:11",
  "fechaEntrega": null
}
```

El número de pedido se genera a partir del `ventaId` con el formato `PED-000001`. Como cada venta tiene a lo sumo un envío, el número es único. Además, como cada venta corresponde a un item, el pedido identifica indirectamente al item vendido que se está despachando.

#### Consultar un envío

Por **id del envío** (no por id del repartidor):

```http
GET /envios/{id}
```

Por **número de pedido**:

```http
GET /envios/pedido/{numeroPedido}
```

Ejemplo:

```http
GET /envios/pedido/PED-000004
```

También se puede consultar por venta:

```http
GET /envios/venta/{ventaId}
```

Y listar la ruta completa de un repartidor, ordenada por posición:

```http
GET /envios/repartidor/{repartidorId}
GET /envios/repartidor/{repartidorId}/pendientes
GET /envios/repartidor/{repartidorId}/entregados
GET /envios/repartidor/{repartidorId}/siguiente
GET /envios/repartidor/{repartidorId}/resumen
```

#### Avanzar la entrega

```http
PUT /envios/{id}/iniciar     # EN_PREPARACION → EN_CAMINO
PUT /envios/{id}/avanzar     # avanza al siguiente estado permitido
PUT /envios/{id}/entregar    # marca ENTREGADO y promueve el siguiente pedido
PUT /envios/{id}/estado      # { "estado": "EN_CAMINO" }
```

Reglas:

- solo el siguiente pedido pendiente puede estar en `LLEGANDO`;
- no se puede entregar un pedido si hay pedidos anteriores pendientes;
- al entregar, el siguiente pedido pendiente de la ruta pasa automáticamente a `LLEGANDO`;
- un envío ya `ENTREGADO` no se puede volver a entregar.

Reasignar el pedido a otro repartidor:

```http
PUT /envios/{id}/repartidor
Content-Type: application/json

{
  "repartidorId": 6
}
```

El pedido se mueve al final de la ruta del nuevo repartidor.

### 10. Crear la valoración

La realiza el comprador del item:

```http
POST /valoraciones
Content-Type: application/json

{
  "itemId": 10,
  "usuarioId": 3,
  "puntuacion": 5,
  "comentario": "Excelente comunicación y entrega rápida"
}
```

Respuesta esperada: `201 Created`.

Consultas disponibles:

```http
GET /valoraciones
GET /valoraciones/{id}
GET /valoraciones/item/{itemId}
GET /valoraciones/item/{itemId}/promedio
GET /valoraciones/usuario/{usuarioId}
GET /valoraciones/vendedor/{vendedorId}
```

Recordatorio: la valoración actual requiere que la venta esté `VENDIDO` y que quien valore sea el comprador; **no** exige que el envío esté `ENTREGADO`.

---

## Códigos de respuesta habituales

| Código | Cuándo aparece |
|---|---|
| `200 OK` | Consulta exitosa o actualización exitosa |
| `201 Created` | Recurso creado correctamente |
| `204 No Content` | Eliminación, marcado de mensajes como leídos o repartidor sin pedidos pendientes |
| `400 Bad Request` | Datos inválidos, id enviado en una creación, referencia inexistente o regla de negocio básica incumplida |
| `403 Forbidden` | El usuario no tiene permiso para la acción (por ejemplo, no es el comprador o no tiene rol `REPARTIDOR` en el resumen) |
| `404 Not Found` | El recurso solicitado no existe |
| `409 Conflict` | Conflicto de estado: item ya vendido, transición no permitida, item ya en el carrito, venta con envío o valoración duplicada |
| `500 Internal Server Error` | Error no controlado del servidor |
| `503 Service Unavailable` | No se pudo consultar otro microservicio por Feign |

---

## Datos de ejemplo

Cada microservicio con base de datos carga registros de ejemplo al iniciar mediante `data.sql`. Las inserciones son idempotentes: si el registro ya existe no se vuelve a crear, por lo que reiniciar los servicios no duplica la información.

Usuarios demo (contraseña `123456`):

| Correo | Nombre | Rol |
|---|---|---|
| `ana.demo@marketplace.cl` | Ana Torres | `ADMIN` |
| `beto.demo@marketplace.cl` | Beto Silva | `ADMIN` |
| `carla.demo@marketplace.cl` | Carla Rojas | `USUARIO` |

Además existen roles, categorías, items, carritos, conversaciones, ventas, pagos, envíos y valoraciones de ejemplo. Los envíos de ejemplo ya tienen números de pedido `PED-000001`, `PED-000002` y `PED-000003`.
